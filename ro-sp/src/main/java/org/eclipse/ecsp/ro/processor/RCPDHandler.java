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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.analytics.stream.base.idgen.internal.GlobalMessageIdGenerator;
import org.eclipse.ecsp.cache.IgniteCache;
import org.eclipse.ecsp.cache.PutStringRequest;
import org.eclipse.ecsp.domain.DeviceMessageFailureEventDataV1_0;
import org.eclipse.ecsp.domain.EventID;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.RCPD;
import org.eclipse.ecsp.domain.ro.RCPDRequestV1_0;
import org.eclipse.ecsp.domain.ro.RCPDResponseV1_0;
import org.eclipse.ecsp.domain.ro.RCPDResponseV1_0.Response;
import org.eclipse.ecsp.domain.ro.RemoteOperationResponseV1_1;
import org.eclipse.ecsp.domain.ro.dao.RCPDDAOMongoImpl;
import org.eclipse.ecsp.entities.IgniteEntity;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.entities.UserContext;
import org.eclipse.ecsp.entities.dma.DeviceMessageErrorCode;
import org.eclipse.ecsp.events.scheduler.CreateScheduleEventData;
import org.eclipse.ecsp.events.scheduler.DeleteScheduleEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleNotificationEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleOpStatusEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleOpStatusEventData.ScheduleOpStatusErrorCode;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.nosqldao.Updates;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.ro.domains.RCPDGenericNotificationEventDataV1_0;
import org.eclipse.ecsp.ro.utils.CacheUtil;
import org.eclipse.ecsp.ro.utils.Utils;
import org.eclipse.ecsp.services.constants.EventAttribute;
import org.eclipse.ecsp.services.utils.ServiceUtil;
import org.eclipse.ecsp.services.utils.VehicleProfileChangedNotificationEventUtil;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import static org.eclipse.ecsp.domain.Constants.MAP_KEY_USER_ID_PATH;
import static org.eclipse.ecsp.domain.Constants.MAP_KEY_VEHICLE_ID_PATH;

/**
 * Handler class for RCPD request.
 */
@Component
public class RCPDHandler {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(RCPDHandler.class);
    private static final String RCPD_REQUEST_VEHICLE_ID = "rcpdEvent.vehicleId";
    private static final String RCPD_REQUEST_USER_ID = "rcpdEvent.userContextInfo.userId";
    private static final String SCHEDULE_TIMEOUT = "SCHEDULE_TIMEOUT";

    @Value("${rcpd.request.validity.duration}")
    long scheduleDuration;

    @Autowired
    private GlobalMessageIdGenerator globalMessageIdGenerator;

    @Value("${rcpd.service.name}")
    private String serviceName;

    @Value("${sink.topic.name}")
    private String[] sinkTopics;

    @Value("${source.topic.name}")
    private String[] sourceTopics;

    @Value("${scheduler.recurrence.delay}")
    private long recurrenceDelay;

    @Value("${kafka.sink.scheduler.topic}")
    private String schedulertopic;

    @Value("${dma.service.max.retry}")
    private long retryCount;

    @Value("${dma.service.retry.interval.millis}")
    private long retryInterval;

    @Value("${dma.service.ttl.buffer}")
    private long ttlBuffer;

    @Value("#{${rcpd.notificationId.mapping}}")
    private Map<String, String> rcpdNotificationIdMappings;

    @Autowired
    private IgniteCache igniteCache;
    @Autowired
    private RCPDDAOMongoImpl rcpdDAOMongoImpl;

    private List<
            ImmutablePair<IgniteBaseDAOMongoImpl<?, ? extends IgniteEntity>, Map<String, String>>>
            daoAndPathMapPairList;

    @Autowired
    private CacheUtil cacheUtil;

    /**
     * Convert JSON string to map.
     *
     * @param json JSON data
     * @return string representation of JSON payload
     */
    public static Map<String, Object> convertJSonStringToMap(String json) {

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<String, Object>();

        // convert JSON string to Map
        try {
            if (json != null) {
                map = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
                });
            }

        } catch (Exception e) {

            LOGGER.debug("Error occured while converting string to map");
        }

        LOGGER.debug("JSon String as map : " + map);
        return map;
    }

    /**
     * Init method for bean.
     */
    public void init() {
        Map<String, String> pathMapEvent = new HashMap<>();

        pathMapEvent.put(MAP_KEY_VEHICLE_ID_PATH, RCPD_REQUEST_VEHICLE_ID);
        pathMapEvent.put(MAP_KEY_USER_ID_PATH, RCPD_REQUEST_USER_ID);

        ImmutablePair<IgniteBaseDAOMongoImpl<?, ? extends IgniteEntity>, Map<String, String>>
                daoAndPathMapPair = new ImmutablePair<>(rcpdDAOMongoImpl, pathMapEvent);

        daoAndPathMapPairList = Arrays.asList(daoAndPathMapPair);
    }

    /**
     * Process RCPD request.
     *
     * @param key         Ignite Key
     * @param value       Ignite event
     * @param serviceUtil utility service object
     */
    public IgniteEventImpl processRCPDRequest(
            IgniteKey key, IgniteEvent value, ServiceUtil serviceUtil) {
        LOGGER.info("Process  RCPDRequest :{}", value);
        IgniteEventImpl rcpdRequestImpl = (IgniteEventImpl) value;
        rcpdRequestImpl.setMessageId(
                globalMessageIdGenerator.generateUniqueMsgId(value.getVehicleId()));
        rcpdRequestImpl.setTimestamp(System.currentTimeMillis());
        rcpdRequestImpl.setResponseExpected(true);
        rcpdRequestImpl.setDeviceRoutable(true);
        rcpdRequestImpl.setBizTransactionId(value.getBizTransactionId());
        rcpdRequestImpl.setDeviceDeliveryCutoff(
                serviceUtil.getEventTtl(retryCount, retryInterval, ttlBuffer, value.getTimestamp()));
        LOGGER.debug("Key={},apiEvent={}", key, value);
        persistEntity(rcpdRequestImpl);
        return rcpdRequestImpl;
    }

    /**
     * Process RCPD response.
     *
     * @param key   Ignite Key
     * @param value Ignite event
     * @param ctxt  Stream Processing context
     */
    public IgniteEventImpl processRCPDResponse(
            IgniteKey key, IgniteEvent value, StreamProcessingContext ctxt) {
        IgniteEventImpl rcpdResponse = (IgniteEventImpl) value;
        LOGGER.debug("Processing RCPDResponse event: {}", value);
        RCPDResponseV1_0 responseData = (RCPDResponseV1_0) rcpdResponse.getEventData();
        String originFromRequest = null;
        String userIdFromRequest = null;
        String scheuleIdFromRequest = null;
        String cacheKey =
                responseData.getRcpdRequestId() + Constants.UNDER_SCORE + rcpdResponse.getVehicleId();
        Element rcpdRequestCacheElement = cacheUtil.getCache(Constants.RO_CACHE_NAME).get(cacheKey);
        Map<String, String> rcpdRequestEntityMap = null;
        if (null != rcpdRequestCacheElement && !rcpdRequestCacheElement.isExpired()) {
            rcpdRequestEntityMap = (Map<String, String>) rcpdRequestCacheElement.getObjectValue();
        }
        // First check the cache for the Request data
        if (null != rcpdRequestEntityMap && !rcpdRequestEntityMap.isEmpty()) {
            originFromRequest = rcpdRequestEntityMap.get(
                            org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_REQUEST_ORIGIN);
            userIdFromRequest = rcpdRequestEntityMap.get(
                            org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_REQUEST_USERID);
            scheuleIdFromRequest = rcpdRequestEntityMap.get(
                            org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_REQUEST_SCHEDULEID);
        } else {
            Optional<RCPD> dbEntity = rcpdDAOMongoImpl.getRCPDRequest(
                            rcpdResponse.getVehicleId(), responseData.getRcpdRequestId(), null);
            LOGGER.debug("dbEntity::" + Utils.logForging(dbEntity));
            if (dbEntity.isPresent()) {
                originFromRequest = ((RCPDRequestV1_0) dbEntity.get().getRcpdEvent().getEventData()).getOrigin();
                userIdFromRequest = ((RCPDRequestV1_0) dbEntity.get().getRcpdEvent().getEventData()).getUserId();
                scheuleIdFromRequest =
                    ((RCPDRequestV1_0) dbEntity.get().getRcpdEvent().getEventData()).getScheduleRequestId();
            } else {
                LOGGER.info("RCPDResponse from HU soft button not to process, event {}", value);
                persistEntity(value);
                return null;
            }
        }
        setDFFQualifier(key, rcpdResponse, originFromRequest, userIdFromRequest);
        Updates updateRoResponse = new Updates();
        updateRoResponse.addListAppend(
                org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_RESPONSE_LIST, value);
        // Updating RCPD Response list to RCPD Entity
        rcpdDAOMongoImpl.update(rcpdDAOMongoImpl.prepareIgniteQueryForRcpdRequest(
                        responseData.getRcpdRequestId(), rcpdResponse.getVehicleId()),
                updateRoResponse);
        UserContext userContext = new UserContext();
        userContext.setUserId(userIdFromRequest);
        rcpdResponse.setUserContextInfo(Arrays.asList(userContext));
        LOGGER.debug("remoteInhibitResponse: {} ", Utils.logForging(rcpdResponse));
        // delte scheduler
        if (scheuleIdFromRequest != null) {
            deleteSchedulerWithScheduleID(
                    key, scheuleIdFromRequest, rcpdResponse.getVehicleId(), value, ctxt);
        }
        updateRCPDrequestStatus(
                userIdFromRequest, value.getVehicleId(), responseData.getResponse().name(), null);
        createAndSentNotification(key, value, ctxt);
        return rcpdResponse;
    }

    private void updateRCPDrequestStatus(
            String userId, String vehicleId, String vehicleStatus, String offBoardStatus) {

        String json =
                igniteCache.getString(
                        org.eclipse.ecsp.domain.ro.constant.Constants.getRedisKey(
                                org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_SERVICE,
                                vehicleId,
                                org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_STATUS));

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = new HashMap<String, Object>();

            if (json != null) {
                map = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
                });
                if (map == null) {
                    map = new HashMap<String, Object>();
                    map.put(org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_REQUEST_USERID, userId);
                }
            } else {
                map.put(org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_REQUEST_USERID, userId);
            }

            if (vehicleStatus != null) {
                map.put(org.eclipse.ecsp.domain.ro.constant.Constants.VEHICLE_STATUS, vehicleStatus);
            }

            if (offBoardStatus != null) {
                map.put(org.eclipse.ecsp.domain.ro.constant.Constants.OFFBOARD_STATUS, offBoardStatus);
            } else {
                map.remove(org.eclipse.ecsp.domain.ro.constant.Constants.OFFBOARD_STATUS);
            }

            map.put(org.eclipse.ecsp.domain.ro.constant.Constants.TIME_STAMP, System.currentTimeMillis());

            String strValue = mapper.writeValueAsString(map);

            igniteCache.putString(
                    new PutStringRequest()
                            .withKey(
                                    org.eclipse.ecsp.domain.ro.constant.Constants.getRedisKey(
                                            org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_SERVICE,
                                            vehicleId,
                                            org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_STATUS))
                            .withValue(strValue));

        } catch (IOException e) {
            LOGGER.error("Error occurred while converting string to map");
        } catch (Exception e1) {
            LOGGER.error("Error occurred while converting map to json string: {}", e1);
        }
    }

    /**
     * Save RCPD entity in the database.
     *
     * @param value Ignite Event
     */
    public void persistEntity(IgniteEvent value) {
        RCPD rcpdEntity = new RCPD();
        rcpdEntity.setSchemaVersion(value.getSchemaVersion());
        rcpdEntity.setRcpdEvent(value);

        rcpdDAOMongoImpl.save(rcpdEntity);
        LOGGER.debug("Entity inserted into MongoDB ,entity :{}", rcpdEntity);

        Element entityElementToCache = buildElementForCache(value);
        cacheUtil.getCache(Constants.RO_CACHE_NAME).put(entityElementToCache);
        LOGGER.debug(
                "Relevant RCPD Entity fields are cached :{}", entityElementToCache.getObjectValue());
    }

    /**
     * Create Element object for storing in cache.
     *
     * @param value Ignite event
     * @return {@link Element} object to be stored in cache
     */
    public Element buildElementForCache(IgniteEvent value) {

        Map<String, String> rcpdRequestElementsMap = new HashMap<String, String>();
        RCPDRequestV1_0 abstractRoEventData = (RCPDRequestV1_0) value.getEventData();
        rcpdRequestElementsMap.put(
                org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_REQUEST_ORIGIN,
                abstractRoEventData.getOrigin());
        rcpdRequestElementsMap.put(
                org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_REQUEST_USERID,
                abstractRoEventData.getUserId());

        String cachekey = value.getRequestId() + Constants.UNDER_SCORE + value.getVehicleId();
        return new Element(cachekey, rcpdRequestElementsMap);
    }

    /**
     * Sets dff qualifier.
     *
     * @param key    the key
     * @param value  the value
     * @param origin the origin
     * @param userId the user id
     */
    public void setDFFQualifier(IgniteKey key, IgniteEventImpl value, String origin, String userId) {
        // Send DFF message
        if (StringUtils.isNotBlank(origin)) {
            String qualifier =
                    value.getEventId().toUpperCase() + Constants.UNDER_SCORE + origin.toUpperCase();
            LOGGER.debug(
                    "origin :{} , DFF Qualifier : {}",
                    Utils.logForging(origin.toUpperCase()),
                    Utils.logForging(qualifier));
            if (StringUtils.isNotBlank(qualifier)) {
                UserContext userContext = new UserContext();
                userContext.setUserId(userId);
                value.setUserContextInfo(Arrays.asList(userContext));
                value.setDFFQualifier(qualifier);
            }
        }
    }

    /**
     * Method to set DFF qualifier to the ignite event.
     *
     * @param key   Ignite key
     * @param value Ignite event
     * @param ctxt  StreamProcessingContext
     */
    private void createAndSentNotification(
            IgniteKey key, IgniteEvent value, StreamProcessingContext ctxt) {
        String notificationId = getNotificationId((RCPDResponseV1_0) value.getEventData());
        LOGGER.info("notificationId -> {}", notificationId);
        if (notificationId != null) {
            RCPDResponseV1_0 responseData = (RCPDResponseV1_0) value.getEventData();
            IgniteEventImpl notificationEvent =
                    createNotificationEvent(
                            value,
                            responseData.getResponse().name(),
                            responseData.getRcpdRequestId(),
                            notificationId);
            if (sinkTopics.length > 0) {
                ctxt.forwardDirectly(key, notificationEvent, sinkTopics[0]);
            } else {
                LOGGER.error("sink topic is not configured for notification");
            }
        } else {
            LOGGER.error("notificationId not found for RCPD response event: {}", value);
        }
    }

    private String getNotificationId(RCPDResponseV1_0 response) {
        return rcpdNotificationIdMappings.get(response.getResponse().toString());
    }

    private IgniteEventImpl createNotificationEvent(
            IgniteEvent value, String response, String rcpdRequestID, String notificationId) {
        RCPDGenericNotificationEventDataV1_0 notificationData =
                new RCPDGenericNotificationEventDataV1_0(rcpdRequestID, response, notificationId);

        IgniteEventImpl notificationEvent = new IgniteEventImpl();
        notificationEvent.setEventId(Constants.GENERICNOTIFICATIONEVENT);
        notificationEvent.setVersion(Version.V1_0);
        notificationEvent.setTimestamp(System.currentTimeMillis());
        notificationEvent.setVehicleId(value.getVehicleId());
        notificationEvent.setBizTransactionId(value.getBizTransactionId());
        notificationEvent.setRequestId(rcpdRequestID);
        notificationEvent.setEventData(notificationData);
        return notificationEvent;
    }

    /**
     * Handle device message failure event.
     *
     * @param key   Ignite Key
     * @param value Ignite event
     * @param ctxt  stream processing context
     */
    public void handleDeviceMessageFailure(
            IgniteKey key, IgniteEvent value, StreamProcessingContext ctxt) {
        DeviceMessageFailureEventDataV1_0 dmFailureEventData =
                (DeviceMessageFailureEventDataV1_0) value.getEventData();
        IgniteEventImpl failedEvent = (IgniteEventImpl) dmFailureEventData.getFailedIgniteEvent();
        IgniteEventImpl event = new IgniteEventImpl();

        RCPDRequestV1_0 rcpdEventData = (RCPDRequestV1_0) failedEvent.getEventData();
        String origin = rcpdEventData.getOrigin();
        event.setEventId(org.eclipse.ecsp.domain.ro.constant.Constants.RCPDRESPONSE);
        String qualifier = null;
        if (StringUtils.isNotBlank(origin)) {
            qualifier = event.getEventId().toUpperCase() + Constants.UNDER_SCORE + origin.toUpperCase();
        }
        event.setUserContextInfo(failedEvent.getUserContextInfo());
        RCPDResponseV1_0 failureresponse = new RCPDResponseV1_0();
        failureresponse.setRcpdRequestId(rcpdEventData.getRcpdRequestId());
        UserContext userContext = new UserContext();
        String userId = rcpdEventData.getUserId();
        userContext.setUserId(userId);
        failureresponse.setResponse(dmFailureRemoteOperationResponse(dmFailureEventData));
        event.setEventData(failureresponse);
        event.setUserContextInfo(Arrays.asList(userContext));
        event.setVersion(Version.V1_0);
        event.setVehicleId(failedEvent.getVehicleId());
        event.setRequestId(failedEvent.getRequestId());
        event.setBizTransactionId(failedEvent.getBizTransactionId());
        event.setTimestamp(System.currentTimeMillis());

        if (qualifier != null) {
            LOGGER.debug("origin :{} , DFF Qualifier : {}", origin.toUpperCase(), qualifier);
            if (StringUtils.isNotBlank(qualifier)) {
                event.setDFFQualifier(qualifier);
                LOGGER.debug(
                        "requestid:{},deviceMessageFailure error code :{}",
                        failedEvent.getRequestId(),
                        dmFailureEventData.getErrorCode());
                LOGGER.debug("Forwarding Device Message Failure Event :{}", event);
                Record<IgniteKey<?>, IgniteEvent> kafkaRecord =
                        new Record<>(key, event, System.currentTimeMillis());

                ctxt.forward(kafkaRecord);
            }
        }

        createAndSentNotification(key, event, ctxt);

        if (dmFailureEventData
                .getErrorCode()
                .name()
                .equalsIgnoreCase(DeviceMessageErrorCode.DEVICE_STATUS_INACTIVE.name())) {
            updateRCPDrequestStatus(
                    userId, failedEvent.getVehicleId(), null, failureresponse.getResponse().name());
            if (!isSchulderExists(failedEvent.getVehicleId(), rcpdEventData.getRcpdRequestId())) {
                createScheduler(key, event, ctxt);
            }
        } else {
            updateRCPDrequestStatus(
                    userId,
                    failedEvent.getVehicleId(),
                    org.eclipse.ecsp.domain.ro.constant.Constants.FAILED,
                    failureresponse.getResponse().name());
        }
    }

    /**
     * Create schedule event.
     *
     * @param key   Ignite Key
     * @param value Ignite event
     * @param ctxt  stream processing context
     */
    public void createScheduler(IgniteKey key, IgniteEvent value, StreamProcessingContext ctxt) {

        LOGGER.debug(
                "Key={},ackIgniteEvent={}, serviceName={}", key.toString(), value.toString(), serviceName);
        CreateScheduleEventData createEventData = new CreateScheduleEventData();
        createEventData.setNotificationTopic(sourceTopics[0]);
        createEventData.setRecurrenceDelayMs(recurrenceDelay);
        createEventData.setServiceName(serviceName);
        createEventData.setCustomExtension(serviceName);
        if (scheduleDuration > 0) {
            createEventData.setInitialDelayMs(scheduleDuration);
            createEventData.setFiringCount(1);
            HashMap notificationPaylod = new HashMap<String, String>();
            notificationPaylod.put(EventAttribute.REQUEST_ID, value.getRequestId());
            notificationPaylod.put(EventAttribute.BIZTRANSACTION_ID, value.getBizTransactionId());
            notificationPaylod.put(Constants.SERVICE_NAME, serviceName);

            String json = null;
            try {
                json = new ObjectMapper().writeValueAsString(notificationPaylod);
            } catch (JsonProcessingException e) {
                LOGGER.info("createScheduler notificationPaylod= {}", notificationPaylod);
            }
            if (json != null) {
                createEventData.setNotificationPayload(json.getBytes(StandardCharsets.UTF_8));
            }
            IgniteStringKey notificationKey = new IgniteStringKey();
            notificationKey.setKey(value.getVehicleId());
            createEventData.setNotificationKey(notificationKey);
            LOGGER.debug("createScheduler eventdata= {}", createEventData);

            IgniteEventImpl igniteEvent =
                    Utils.createIgniteEvent(
                            value.getVehicleId(), createEventData, EventID.CREATE_SCHEDULE_EVENT, null);
            igniteEvent.setBizTransactionId(value.getBizTransactionId());
            igniteEvent.setRequestId(UUID.randomUUID().toString());
            ctxt.forwardDirectly(key, igniteEvent, schedulertopic);
        }
    }

    /**
     * Process RTN case.
     *
     * @param key                                        Ignite Key
     * @param value                                      Ignite event
     * @param vehicleProfileChangedNotificationEventUtil utility methods class
     */
    public void processRTN(
            IgniteKey key,
            IgniteEvent value,
            VehicleProfileChangedNotificationEventUtil vehicleProfileChangedNotificationEventUtil) {
        LOGGER.debug(
                "received VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT vehicleId: {}", value.getVehicleId());

        vehicleProfileChangedNotificationEventUtil.deleteData(value, daoAndPathMapPairList);

        List<String> listOldeuserIds =
                vehicleProfileChangedNotificationEventUtil.getChangeDescriptionOldUserId(value);
        LOGGER.debug("cleanupOlderUserData  with listOldeuserIds :{}", listOldeuserIds);
        if (listOldeuserIds != null && listOldeuserIds.size() > 0) {
            for (String oldUser : listOldeuserIds) {
                cleanupStatusData(value, oldUser);
            }
        }
    }

    private Response dmFailureRemoteOperationResponse(
            DeviceMessageFailureEventDataV1_0 dmFailureEventData) {
        if (Objects.nonNull(dmFailureEventData) && Objects.nonNull(dmFailureEventData.getErrorCode())) {
            RemoteOperationResponseV1_1.Response errorResponse =
                    Constants.ERROR_RESPONSE.get(dmFailureEventData.getErrorCode());
            if (Objects.nonNull(errorResponse)) {
                return Response.valueOf(errorResponse.name());
            }
        }
        return null;
    }

    private void cleanupStatusData(IgniteEvent value, String olduserId) {
        String json =
                igniteCache.getString(
                        org.eclipse.ecsp.domain.ro.constant.Constants.getRedisKey(
                                org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_SERVICE,
                                value.getVehicleId(),
                                org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_STATUS));
        Map<String, Object> mapOfVehicleStatusResponse = null;
        LOGGER.debug("cleanupStatusData :{}", json);

        if (json != null) {
            mapOfVehicleStatusResponse = convertJSonStringToMap(json);
            String userId =
                    (String)
                            mapOfVehicleStatusResponse.get(
                                    org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_REQUEST_USERID);
            if (userId != null) {
                if (olduserId.equalsIgnoreCase(userId)) {
                    LOGGER.debug("cleanupStatusData deleted");
                    igniteCache.delete(
                            org.eclipse.ecsp.domain.ro.constant.Constants.getRedisKey(
                                    org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_SERVICE,
                                    value.getVehicleId(),
                                    org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_STATUS));
                }
            }
        }
    }

    /**
     * Process scheduler ack event.
     *
     * @param key   Ignite Key
     * @param value Ignite event
     */
    public void processSchedulerAckEvent(IgniteKey key, IgniteEvent value)
            throws JsonParseException, JsonMappingException, IOException, JSONException {
        ScheduleOpStatusEventData eventData = (ScheduleOpStatusEventData) value.getEventData();
        String scheduleId = eventData.getScheduleId();
        boolean valid = eventData.isValid();
        ScheduleOpStatusErrorCode statusErrorCode = eventData.getStatusErrorCode();

        LOGGER.info("### Schedule ack revieved ###");

        if (valid && statusErrorCode == null) {
            if (eventData.getIgniteEvent().getEventId().equals(EventID.CREATE_SCHEDULE_EVENT)) {
                CreateScheduleEventData createEventData =
                        (CreateScheduleEventData) eventData.getIgniteEvent().getEventData();
                Map<String, Object> response = null;
                try {
                    response =
                            new ObjectMapper()
                                    .readValue(
                                            new String(createEventData.getNotificationPayload(), 
                                                    StandardCharsets.UTF_8),
                                            HashMap.class);
                } catch (IOException e) {
                    LOGGER.debug("Error while converting  string to map");
                }
                if (response != null) {
                    updateScheduleId(
                            eventData.getScheduleId(),
                            eventData.getIgniteEvent().getVehicleId(),
                            (String) response.get(EventAttribute.REQUEST_ID),
                            eventData.getScheduleId());
                }
            } else if (eventData.getIgniteEvent().getEventId().equals(EventID.DELETE_SCHEDULE_EVENT)) {
                updateScheduleId(
                        eventData.getScheduleId(), eventData.getIgniteEvent().getVehicleId(), null, null);
            }
        } else {
            LOGGER.error(
                    "StreamProcessor is unable to process schedule scheduleId={} "
                            + "valid={} statusErrorCode={}   createScheduleEventData={}",
                    scheduleId,
                    valid,
                    statusErrorCode,
                    eventData.getIgniteEvent().getEventData());
        }
    }

    private void updateScheduleId(
            String scheduleId, String vehicleId, String rcpdRequestId, String setScheduleID) {

        LOGGER.debug(
                "updateScheduleId ,vehicleId {} , scheduleId {}",
                Utils.logForging(vehicleId),
                Utils.logForging(scheduleId));
        if (scheduleId == null && rcpdRequestId == null) {
            LOGGER.info("updateScheduleId scheduleId not  exists");
            return;
        }
        Optional<RCPD> dbEntity = rcpdDAOMongoImpl.getRCPDRequest(vehicleId, rcpdRequestId, scheduleId);

        LOGGER.debug("processSchedulerAck rcpdEvent {} ", Utils.logForging(dbEntity));
        if (dbEntity.isPresent()) {
            ((RCPDRequestV1_0) dbEntity.get().getRcpdEvent().getEventData())
                    .setScheduleRequestId(setScheduleID);
            LOGGER.debug("updateScheduleId rcpdEvent {} ", Utils.logForging(dbEntity));
            rcpdDAOMongoImpl.update(dbEntity.get());

            String cacheKey =
                    ((RCPDRequestV1_0) dbEntity.get().getRcpdEvent().getEventData()).getRcpdRequestId()
                            + Constants.UNDER_SCORE
                            + vehicleId;
            Element rcpdRequestCacheElement = cacheUtil.getCache(Constants.RO_CACHE_NAME).get(cacheKey);
            Map<String, String> rcpdRequestEntityMap = null;
            if (null != rcpdRequestCacheElement && !rcpdRequestCacheElement.isExpired()) {
                rcpdRequestEntityMap = (Map<String, String>) rcpdRequestCacheElement.getObjectValue();
                rcpdRequestEntityMap.put(
                        org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_REQUEST_SCHEDULEID, setScheduleID);

                Element entityElementToCache = new Element(cacheKey, rcpdRequestEntityMap);
                cacheUtil.getCache(Constants.RO_CACHE_NAME).remove(cacheKey);
                cacheUtil.getCache(Constants.RO_CACHE_NAME).put(entityElementToCache);
            }
        }
    }

    /**
     * Process Schedule Notification event.
     *
     * @param key   Ignite Key
     * @param value Ignite event
     * @param ctxt  stream processing context.
     */
    public void processScheduleNotification(
            IgniteKey key, IgniteEvent value, StreamProcessingContext ctxt) {

        ScheduleNotificationEventData eventData = (ScheduleNotificationEventData) value.getEventData();

        LOGGER.debug("Processing RCPD schedule notification event, data {} ", eventData);

        if (Objects.nonNull(eventData)) {
            Map<String, Object> response = null;

            try {
                response =
                        new ObjectMapper()
                                .readValue(
                                        new String(eventData.getPayload(), StandardCharsets.UTF_8), HashMap.class);
            } catch (IOException e) {
                LOGGER.error("Issue in reading payload for RCPD schedule notification event");
            }

            if (Objects.nonNull(response)) {
                Optional<RCPD> dbEntity =
                        rcpdDAOMongoImpl.getRCPDRequest(
                                value.getVehicleId(), (String) response.get(EventAttribute.REQUEST_ID), null);

                // If the request already exists for the RCPD event then no need to send notification
                if (dbEntity.isPresent()
                        && dbEntity.get().getRcpdResponseList() != null
                        && !dbEntity.get().getRcpdResponseList().isEmpty()) {
                    LOGGER.info(
                            "Response already exists for the RCPD request so not needed to send notification {} ",
                            Utils.logForging(dbEntity.get()));
                    return;
                }

                String userId =
                        ((RCPDRequestV1_0) dbEntity.get().getRcpdEvent().getEventData()).getUserId();

                updateRCPDrequestStatus(
                        userId,
                        dbEntity.get().getRcpdEvent().getVehicleId(),
                        org.eclipse.ecsp.domain.ro.constant.Constants.FAILED,
                        Response.FAIL_MESSAGE_DELIVERY_TIMED_OUT.name());

                updateScheduleId(
                        ((RCPDRequestV1_0) dbEntity.get().getRcpdEvent().getEventData()).getScheduleRequestId(),
                        dbEntity.get().getRcpdEvent().getVehicleId(),
                        null,
                        null);

                String notificationId = rcpdNotificationIdMappings.get(SCHEDULE_TIMEOUT);
                forwardNotificationEvent(key, value, notificationId, dbEntity, ctxt);
            }
        }
    }

    private void forwardNotificationEvent(
            IgniteKey key,
            IgniteEvent value,
            String notificationId,
            Optional<RCPD> dbEntity,
            StreamProcessingContext ctxt) {
        if (Objects.nonNull(notificationId) && dbEntity.isPresent()) {
            IgniteEventImpl notificationEvent =
                    createNotificationEvent(
                            value,
                            Response.TIME_OUT.name(),
                            ((RCPDRequestV1_0) dbEntity.get().getRcpdEvent().getEventData()).getRcpdRequestId(),
                            notificationId);
            if (sinkTopics.length > 0) {
                ctxt.forwardDirectly(key, notificationEvent, sinkTopics[0]);
            } else {
                LOGGER.error("sink topic is not configured for notification");
            }
        } else {
            LOGGER.error("notificationId not found for key:{}", SCHEDULE_TIMEOUT);
        }
    }

    /**
     * Delete scheduler with schedule ID.
     *
     * @param key        Ignite Key
     * @param scheduleId schedule id
     * @param vehicleId  vehicle id
     * @param value      Ignite event
     * @param ctxt       stream processing context
     */
    public void deleteSchedulerWithScheduleID(
            IgniteKey key,
            String scheduleId,
            String vehicleId,
            IgniteEvent value,
            StreamProcessingContext ctxt) {
        DeleteScheduleEventData deleteEventData = new DeleteScheduleEventData();
        deleteEventData.setCustomExtension(serviceName);
        deleteEventData.setScheduleId(scheduleId);
        ctxt.forwardDirectly(
                key,
                Utils.createIgniteEvent(vehicleId, deleteEventData, EventID.DELETE_SCHEDULE_EVENT, null),
                schedulertopic);
    }

    private Boolean isSchulderExists(String vehicleId, String rcpdrequestId) {
        String cacheKey = rcpdrequestId + Constants.UNDER_SCORE + vehicleId;
        Element rcpdRequestCacheElement = cacheUtil.getCache(Constants.RO_CACHE_NAME).get(cacheKey);
        Map<String, String> rcpdRequestEntityMap = null;
        if (null != rcpdRequestCacheElement && !rcpdRequestCacheElement.isExpired()) {
            rcpdRequestEntityMap = (Map<String, String>) rcpdRequestCacheElement.getObjectValue();
        }
        // First check the cache for the Request data
        if (null != rcpdRequestEntityMap && !rcpdRequestEntityMap.isEmpty()) {
            if (rcpdRequestEntityMap.get(
                    org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_REQUEST_SCHEDULEID)
                    != null) {
                return true;
            }
        } else {
            // Then check the MongoDB for the Request data
            Optional<RCPD> dbEntity = rcpdDAOMongoImpl.getRCPDRequest(vehicleId, rcpdrequestId, null);
            LOGGER.debug("dbEntity::" + Utils.logForging(dbEntity));

            if (dbEntity.isPresent()) {
                if (((RCPDRequestV1_0) dbEntity.get().getRcpdEvent().getEventData()).getScheduleRequestId()
                        != null) {
                    return true;
                }
            }
        }
        return false;
    }
}
