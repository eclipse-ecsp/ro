/*
 *
 * ******************************************************************************
 *
 *  Copyright (c) 2023-24 Harman International
 *
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *
 *  you may not use this file except in compliance with the License.
 *
 *  You may obtain a copy of the License at
 *
 *
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 **
 *  Unless required by applicable law or agreed to in writing, software
 *
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *
 *  limitations under the License.
 *
 *
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  *******************************************************************************
 *
 */

package org.eclipse.ecsp.ro.processor;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.ecsp.analytics.stream.base.idgen.internal.GlobalMessageIdGenerator;
import org.eclipse.ecsp.domain.EventID;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.AbstractRoEventData;
import org.eclipse.ecsp.domain.ro.RemoteOperationEngineV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationType;
import org.eclipse.ecsp.domain.ro.RoScheduleV2;
import org.eclipse.ecsp.domain.ro.Schedule;
import org.eclipse.ecsp.entities.AbstractIgniteEvent;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.entities.UserContext;
import org.eclipse.ecsp.events.scheduler.CreateScheduleEventData;
import org.eclipse.ecsp.events.scheduler.DeleteScheduleEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleNotificationEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleOpStatusEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleOpStatusEventData.ScheduleOpStatusErrorCode;
import org.eclipse.ecsp.events.scheduler.ScheduleStatus;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.ro.RoDAOMongoImpl;
import org.eclipse.ecsp.ro.RoScheduleDAOMongoImpl;
import org.eclipse.ecsp.ro.RoScheduleV2DAOMongoImpl;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.ro.constants.RoSpConstants;
import org.eclipse.ecsp.ro.utils.NotificationUtil;
import org.eclipse.ecsp.ro.utils.OutboundUtil;
import org.eclipse.ecsp.services.utils.JsonMapperUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for handling the scheduling events.
 */
@Component
public class ScheduleEventDataHandler {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(ScheduleEventDataHandler.class);

    @Value("${source.topic.name}")
    protected String[] sourceTopics;

    @Value("${service.name}")
    protected String serviceName;

    @Autowired
    private GlobalMessageIdGenerator globalMessageIdGenerator;

    @Autowired
    private RoScheduleDAOMongoImpl roScheduleDAOMongoImpl;

    @Autowired
    private RoScheduleV2DAOMongoImpl roScheduleV2DAOMongoImpl;

    @Autowired
    private RoDAOMongoImpl roDAOMongo;

    @Autowired
    private NotificationUtil notificationUtil;

    @Autowired
    private OutboundUtil outboundUtil;

    @Value("#{${notification.responsemessage.notificationid.mapping}}")
    private Map<String, String> notificationIdMapping;

    /**
     * Get scheduler event from payload.
     *
     * @param key               ignite key
     * @param value             ignite event
     * @param event             abstract ignite event
     * @param sourceTopics      source kafka topics
     * @param serviceName       service name
     * @param customServiceName custom service name
     * @param firingCount       firing count
     * @return scheduler event
     */
    public IgniteEventImpl createSchedulerEvent(
            IgniteKey key,
            IgniteEvent value,
            AbstractIgniteEvent event,
            String[] sourceTopics,
            String serviceName,
            String customServiceName,
            Integer firingCount) {
        // Create Schedule
        CreateScheduleEventData createEventData = new CreateScheduleEventData();
        createEventData.setNotificationTopic(sourceTopics[0]);
        createEventData.setServiceName(serviceName);
        createEventData.setCustomExtension(customServiceName);
        AbstractRoEventData roe = (AbstractRoEventData) value.getEventData();

        if (null != roe.getSchedule()) {
            createEventData.setInitialDelayMs(roe.getSchedule().getFirstScheduleTs());
        }
        if (null != roe.getSchedule() && null != roe.getSchedule().getRecurrenceType()) {
            createEventData.setRecurrenceType(
                    CreateScheduleEventData.RecurrenceType.valueOf(
                            roe.getSchedule().getRecurrenceType().getValue()));
            createEventData.setFiringCount(firingCount);
        }
        LOGGER.info("### first RO event schedule: {}", createEventData.getInitialDelayMs());

        String roEvent = JsonMapperUtils.getObjectValueAsString(event);
        LOGGER.info("### Schedule Notification payload: {}", roEvent);
        createEventData.setNotificationPayload(roEvent.getBytes(StandardCharsets.UTF_8));
        IgniteStringKey notificationKey = new IgniteStringKey();
        ((IgniteStringKey) notificationKey).setKey(value.getVehicleId());
        createEventData.setNotificationKey(notificationKey);
        IgniteEventImpl createScheduleEvent = new IgniteEventImpl();
        createScheduleEvent.setEventId(EventID.CREATE_SCHEDULE_EVENT);
        createScheduleEvent.setTimestamp(System.currentTimeMillis());
        createScheduleEvent.setRequestId(event.getRequestId());
        createScheduleEvent.setCorrelationId(value.getCorrelationId());
        createScheduleEvent.setBizTransactionId(value.getBizTransactionId());
        createScheduleEvent.setMessageId(
                globalMessageIdGenerator.generateUniqueMsgId(value.getVehicleId()));
        createScheduleEvent.setSourceDeviceId(value.getVehicleId());
        createScheduleEvent.setVehicleId(value.getVehicleId());
        createScheduleEvent.setVersion(Version.V1_0);
        createScheduleEvent.setEventData(createEventData);
        LOGGER.info("### Before sending to scheduler topic: {}", createScheduleEvent);
        return createScheduleEvent;
    }

    /**
     * Process the scheduler ack event.
     *
     * @param key   ignite key
     * @param value ignite event
     * @throws IOException          I/O Exception
     * @throws JsonMappingException Json Mapping Exception
     * @throws JsonParseException   JSON Parse Exception
     * @throws JSONException        General JSON exception
     */
    public void processSchedulerAckEvent(IgniteKey key, IgniteEvent value)
            throws JsonParseException, JsonMappingException, IOException, JSONException {
        ScheduleOpStatusEventData eventData = (ScheduleOpStatusEventData) value.getEventData();
        String scheduleId = eventData.getScheduleId();
        ScheduleStatus status = eventData.getStatus();
        boolean valid = eventData.isValid();
        ScheduleOpStatusErrorCode statusErrorCode = eventData.getStatusErrorCode();
        if (ScheduleStatus.CREATE.equals(status)) {
            LOGGER.info("### Schedule CREATE ack revieved ###");
            CreateScheduleEventData createScheduleEventData =
                    (CreateScheduleEventData) eventData.getIgniteEvent().getEventData();
            if (valid && statusErrorCode == null) {
                AbstractIgniteEvent roEvent =
                        getEventFromSchedulerPayload(createScheduleEventData.getNotificationPayload(), 0);
                LOGGER.info("### RO event for Ack {}, requestID: {}", roEvent, roEvent.getRequestId());
                updateSchedulerId(
                        value.getVehicleId(), roEvent.getRequestId(), scheduleId, roEvent.getEventId());
            } else {
                LOGGER.error(
                        "StreamProcessor is unable to create schedule or an already created schedule is invalidated:"
                                + " scheduleId={} valid={} statusErrorCode={}   createScheduleEventData={}",
                        scheduleId,
                        valid,
                        statusErrorCode,
                        createScheduleEventData);
            }
        } else if (ScheduleStatus.DELETE.equals(status)) {
            DeleteScheduleEventData deleteScheduleEventData =
                    (DeleteScheduleEventData) eventData.getIgniteEvent().getEventData();
            LOGGER.info("### Schedule DELETE ack revieved ###");
            if ((valid && statusErrorCode == null)
                    || (!valid && ScheduleOpStatusErrorCode.EXPIRED_SCHEDULE.equals(statusErrorCode))) {
                deleteSchedule(value.getVehicleId(), deleteScheduleEventData.getScheduleId());
            } else {
                LOGGER.error(
                        "StreamProcessor is unable to delete schedule:"
                                + " scheduleId={} valid={} statusErrorCode={} deleteScheduleEventData={}",
                        scheduleId,
                        valid,
                        statusErrorCode,
                        deleteScheduleEventData);
            }
        }
    }

    private void deleteSchedule(String vehicleId, String scheduleId) {
        roScheduleDAOMongoImpl.deleteRoSchedule(vehicleId, scheduleId);
    }

    private void updateSchedulerId(
            String vehicleId, String requestId, String scheduleId, String eventId) {
        roScheduleDAOMongoImpl.updateSchedulerId(vehicleId, requestId, scheduleId, eventId);
    }

    /**
     * Returns schedule notification event from the Ignite event for the cutoff period specified.
     *
     * @param key   Ignite Key
     * @param value Ignite event
     * @return schedule notification event
     * @throws JSONException Exception in payload processing
     */
    public AbstractIgniteEvent processScheduledROEvent(
            IgniteKey key, IgniteEvent value, long deviceDeliveryCutoff) throws JSONException {
        LOGGER.info("### Processing scheduler RO event key:{}, value: {}", key, value);
        ScheduleNotificationEventData scheduleNotificationEventData =
                (ScheduleNotificationEventData) value.getEventData();
        byte[] payload = scheduleNotificationEventData.getPayload();
        AbstractIgniteEvent roEvent = null;
        roEvent = getScheduleNotificationEventFromSchedulerPayload(payload, deviceDeliveryCutoff);
        LOGGER.info("### scheduler RO event : {}", roEvent);
        return roEvent;
    }

    /**
     * Get delete schedule event.
     *
     * @param key          Ignite Key
     * @param value        Ignite event
     * @param sourceTopics array of source topics
     * @param serviceName  service name
     * @param roRequestId  ro request ID
     * @param scheduleId   schedule ID
     * @param version      Version
     * @return Delete schedule event
     */
    public IgniteEventImpl deleteSchedulerEvent(
            IgniteKey key,
            IgniteEvent value,
            String[] sourceTopics,
            String serviceName,
            String roRequestId,
            String scheduleId,
            Version version) {
        IgniteEventImpl deleteScheduleEvent = new IgniteEventImpl();
        deleteScheduleEvent.setEventId(EventID.DELETE_SCHEDULE_EVENT);
        deleteScheduleEvent.setTimestamp(System.currentTimeMillis());
        deleteScheduleEvent.setRequestId(roRequestId);
        deleteScheduleEvent.setCorrelationId(value.getCorrelationId());
        deleteScheduleEvent.setBizTransactionId(value.getBizTransactionId());
        deleteScheduleEvent.setMessageId(
                globalMessageIdGenerator.generateUniqueMsgId(value.getVehicleId()));
        deleteScheduleEvent.setSourceDeviceId(value.getVehicleId());
        deleteScheduleEvent.setVehicleId(value.getVehicleId());
        deleteScheduleEvent.setVersion(version);
        DeleteScheduleEventData deleteScheduleEventData = new DeleteScheduleEventData();
        deleteScheduleEventData.setScheduleId(scheduleId);
        deleteScheduleEventData.setCustomExtension(serviceName);
        deleteScheduleEvent.setEventData(deleteScheduleEventData);
        return deleteScheduleEvent;
    }

    private AbstractIgniteEvent getEventFromSchedulerPayload(byte[] values, long deviceDeliveryCutoff)
            throws JSONException {
        JSONObject object = new JSONObject(new String(values, StandardCharsets.UTF_8));
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setRequestId(object.getString("RequestId"));
        igniteEvent.setTimezone((short) object.getInt("Timezone"));
        igniteEvent.setBizTransactionId(object.getString("BizTransactionId"));
        igniteEvent.setVersion(Version.V1_1);
        igniteEvent.setDeviceDeliveryCutoff(deviceDeliveryCutoff);
        igniteEvent.setEventId(object.getString("EventID"));
        igniteEvent.setVehicleId(object.getString("VehicleId"));
        igniteEvent.setTimestamp(System.currentTimeMillis());
        igniteEvent.setDeviceRoutable(true);
        igniteEvent.setShoulderTapEnabled(true);
        igniteEvent.setResponseExpected(true);
        igniteEvent.setMessageId(
                globalMessageIdGenerator.generateUniqueMsgId(igniteEvent.getVehicleId()));
        JSONObject dataObject = object.getJSONObject("Data");
        RemoteOperationEngineV1_1 roData = new RemoteOperationEngineV1_1();
        roData.setDuration(dataObject.getInt(RoSpConstants.DURATION.getValue()));
        roData.setOrigin(dataObject.getString(RoSpConstants.ORIGIN.getValue()));
        roData.setRoRequestId(dataObject.getString(RoSpConstants.RO_REQUEST_ID.getValue()));
        roData.setUserId(dataObject.getString(RoSpConstants.USER_ID.getValue()));
        roData.setState(
                RemoteOperationEngineV1_1.State.valueOf(
                        dataObject.getString(RoSpConstants.STATE.getValue())));
        igniteEvent.setEventData(roData);
        List<UserContext> usrContext = new ArrayList<UserContext>();
        JSONArray users = object.getJSONArray("UserContext");
        for (int i = 0; i < users.length(); i++) {
            UserContext usr = new UserContext();
            usr.setRole(users.getJSONObject(i).getString("role"));
            usr.setUserId(users.getJSONObject(i).getString(RoSpConstants.USER_ID.getValue()));
            usrContext.add(usr);
        }
        igniteEvent.setUserContextInfo(usrContext);
        return igniteEvent;
    }

    private AbstractIgniteEvent getScheduleNotificationEventFromSchedulerPayload(
            byte[] values, long deviceDeliveryCutoff) throws JSONException {
        JSONObject object = new JSONObject(new String(values, StandardCharsets.UTF_8));
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        if (object.has(Constants.REQUESTID)) {
            igniteEvent.setRequestId(object.getString(Constants.REQUESTID));
        }
        if (object.has(Constants.TIMEZONE)) {
            igniteEvent.setTimezone((short) object.getInt(Constants.TIMEZONE));
        }
        if (object.has(Constants.BIZTRANSACTIONID)) {
            igniteEvent.setBizTransactionId(object.getString(Constants.BIZTRANSACTIONID));
        }
        igniteEvent.setVersion(Version.V1_1);
        igniteEvent.setDeviceDeliveryCutoff(deviceDeliveryCutoff);
        if (object.has(Constants.EVENTID)) {
            igniteEvent.setEventId(object.getString(Constants.EVENTID));
        }

        if (object.has(Constants.VEHICLEID)) {
            igniteEvent.setVehicleId(object.getString(Constants.VEHICLEID));
        }
        igniteEvent.setTimestamp(System.currentTimeMillis());
        igniteEvent.setDeviceRoutable(true);
        igniteEvent.setShoulderTapEnabled(true);
        igniteEvent.setResponseExpected(true);
        igniteEvent.setMessageId(
                globalMessageIdGenerator.generateUniqueMsgId(igniteEvent.getVehicleId()));

        if (object.has(Constants.DATA)) {
            JSONObject dataObject = object.getJSONObject(Constants.DATA);
            createRemoteOperationEngineV1_1(igniteEvent, dataObject);
        }

        if (object.has(Constants.USERCONTEXT)) {
            List<UserContext> usrContext = new ArrayList<>();
            JSONArray users = object.getJSONArray(Constants.USERCONTEXT);
            for (int i = 0; i < users.length(); i++) {
                UserContext usr = new UserContext();
                usr.setRole(users.getJSONObject(i).getString("role"));
                usr.setUserId(users.getJSONObject(i).getString(RoSpConstants.USER_ID.getValue()));
                usrContext.add(usr);
            }
            igniteEvent.setUserContextInfo(usrContext);
        }

        return igniteEvent;
    }

    private void createRemoteOperationEngineV1_1(IgniteEventImpl igniteEvent, JSONObject dataObject) {
        RemoteOperationEngineV1_1 roData = new RemoteOperationEngineV1_1();
        roData.setDuration(dataObject.getInt("duration"));
        roData.setOrigin(dataObject.getString("origin"));
        roData.setRoRequestId(dataObject.getString("roRequestId"));
        roData.setUserId(dataObject.getString(RoSpConstants.USER_ID.getValue()));
        roData.setState(RemoteOperationEngineV1_1.State.valueOf(dataObject.getString("state")));
        igniteEvent.setEventData(roData);
    }

    /**
     * Save RO schedule to database.
     *
     * @param value               Ignite event
     * @param schedulerKey        scheduler key
     * @param status              schedule status
     * @param operationType       RO type
     * @param zoneId              zone ID
     * @param scheduleExecuteTime schedule execution time
     */
    public void persistRoSchedule(
            IgniteEvent value,
            String schedulerKey,
            org.eclipse.ecsp.domain.ro.ScheduleStatus status,
            RemoteOperationType operationType,
            ZoneId zoneId,
            long scheduleExecuteTime) {
        AbstractRoEventData roe = (AbstractRoEventData) value.getEventData();
        if (ObjectUtils.isNotEmpty(roe.getSchedule())) {
            Schedule schedule = roe.getSchedule();
            RoScheduleV2 roScheduleV2 = new RoScheduleV2();
            roScheduleV2.setScheduleTs(scheduleExecuteTime);
            roScheduleV2.setSchedulerKey(schedulerKey);
            roScheduleV2.setStatus(status);
            roScheduleV2.setRecurrenceType(schedule.getRecurrenceType());
            roScheduleV2.setCreatedOn(System.currentTimeMillis());
            roScheduleV2.setUpdatedOn(System.currentTimeMillis());
            if (ObjectUtils.isNotEmpty(zoneId)) {
                roScheduleV2.setZoneId(zoneId.toString());
            }
            roScheduleV2.setVehicleId(value.getVehicleId());
            roScheduleV2.setSchemaVersion(Version.V2_0);
            roScheduleV2.setScheduleType(operationType.getValue());
            roScheduleV2DAOMongoImpl.save(roScheduleV2);
        }
    }
}
