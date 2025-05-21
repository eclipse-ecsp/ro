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

package org.eclipse.ecsp.ro.processor.strategy.impl;

import net.sf.ehcache.Element;
import org.apache.commons.lang3.ObjectUtils;
import org.bson.types.ObjectId;
import org.eclipse.ecsp.analytics.stream.base.idgen.internal.GlobalMessageIdGenerator;
import org.eclipse.ecsp.domain.AcknowledgementV1_1;
import org.eclipse.ecsp.domain.EventID;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.AbstractRoEventData;
import org.eclipse.ecsp.domain.ro.ROStatus;
import org.eclipse.ecsp.domain.ro.RemoteOperationAlarmV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationClimateV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationClimateV2_0;
import org.eclipse.ecsp.domain.ro.RemoteOperationDoorsV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationDriverDoorV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationDriverWindowV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationEngineV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationGloveBoxV2_0;
import org.eclipse.ecsp.domain.ro.RemoteOperationHoodV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationHornV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationLiftgateV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationLiftgateV2_0;
import org.eclipse.ecsp.domain.ro.RemoteOperationLightsV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationLightsV1_2;
import org.eclipse.ecsp.domain.ro.RemoteOperationTrunkV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationTrunkV2_0;
import org.eclipse.ecsp.domain.ro.RemoteOperationWindowsV1_1;
import org.eclipse.ecsp.domain.ro.Ro;
import org.eclipse.ecsp.entities.EventData;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.ro.RoDAOMongoImpl;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.ro.processor.strategy.SubStreamProcessor;
import org.eclipse.ecsp.ro.utils.CacheUtil;
import org.eclipse.ecsp.ro.utils.NotificationUtil;
import org.eclipse.ecsp.ro.utils.OutboundUtil;
import org.eclipse.ecsp.ro.utils.Utils;
import org.eclipse.ecsp.services.constants.EventAttribute;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract implementation for Sub-stream processor.
 *
 * @see SubStreamProcessor
 */
public abstract class AbstractStreamProcessor implements SubStreamProcessor {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(AbstractStreamProcessor.class);

    protected static final String RESPONSE = "response";

    @Autowired
    protected GlobalMessageIdGenerator globalMessageIdGenerator;

    @Autowired
    protected RoDAOMongoImpl roDAOMongoImpl;

    @Autowired
    protected CacheUtil cacheUtil;

    @Autowired
    protected NotificationUtil notificationUtil;

    @Autowired
    private OutboundUtil outboundUtil;

    @Value("${service.name}")
    protected String serviceName;

    @Value("${source.topic.name}")
    protected String[] sourceTopics;

    @Value("${sink.topic.name}")
    protected String[] sinkTopics;

    @Value("${engine.start.check.enable:true}")
    protected Boolean engineStartCheckEnable;

    @Value("${ro.queue.enable:true}")
    protected boolean roQueueEnable;

    @Value("#{${notification.responsemessage.notificationid.mapping}}")
    private Map<String, String> notificationIdMapping;

    @SuppressWarnings({"checkstyle:CyclomaticComplexity", "MethodLength"})
    protected String getRoCommand(EventData data) {
        String className = data.getClass().getSimpleName();
        LOGGER.debug("finding state for {}", Utils.logForging(className));
        String state = "";
        switch (className) {
            case Constants.REMOTEOPERATIONALARMV1_1:
                state = ((RemoteOperationAlarmV1_1) data).getState().toString();
                break;
            case Constants.REMOTEOPERATIONCLIMATEV1_1:
                state = ((RemoteOperationClimateV1_1) data).getState().toString();
                break;
            case Constants.REMOTEOPERATIONDOORSV1_1:
                state = ((RemoteOperationDoorsV1_1) data).getState().toString();
                break;
            case Constants.REMOTEOPERATIONDRIVERDOORV1_1:
                state = ((RemoteOperationDriverDoorV1_1) data).getState().toString();
                break;
            case Constants.REMOTEOPERATIONDRIVERWINDOWV1_1:
                state = ((RemoteOperationDriverWindowV1_1) data).getState().toString();
                break;
            case Constants.REMOTEOPERATIONENGINEV1_1:
                state = ((RemoteOperationEngineV1_1) data).getState().toString();
                break;
            case Constants.REMOTEOPERATIONHOODV1_1:
                state = ((RemoteOperationHoodV1_1) data).getState().toString();
                break;
            case Constants.REMOTEOPERATIONHORNV1_1:
                state = ((RemoteOperationHornV1_1) data).getState().toString();
                break;
            case Constants.REMOTEOPERATIONLIFTGATEV1_1:
                state = ((RemoteOperationLiftgateV1_1) data).getState().toString();
                break;
            case Constants.REMOTEOPERATIONLIGHTSV1_1:
                state = ((RemoteOperationLightsV1_1) data).getState().toString();
                break;
            case Constants.REMOTEOPERATIONLIGHTSV1_2:
                state = ((RemoteOperationLightsV1_2) data).getState().toString();
                break;
            case Constants.REMOTEOPERATIONWINDOWSV1_1:
                state = ((RemoteOperationWindowsV1_1) data).getState().toString();
                break;
            case Constants.REMOTEOPERATIONTRUNKV1_1:
                state = ((RemoteOperationTrunkV1_1) data).getState().toString();
                break;
            case Constants.REMOTEOPERATIONTRUNKV2_0:
                state = ((RemoteOperationTrunkV2_0) data).getState().toString();
                break;
            case Constants.REMOTEOPERATIONGLOVEBOXV2_0:
                state = ((RemoteOperationGloveBoxV2_0) data).getState().toString();
                break;
            case Constants.REMOTEOPERATIONLIFTGATEV2_0:
                state = ((RemoteOperationLiftgateV2_0) data).getState().toString();
                break;
            case Constants.REMOTEOPERATIONCLIMATEV2_0:
                state = ((RemoteOperationClimateV2_0) data).getState().toString();
                break;
            default:
                LOGGER.debug("could not find state for {}", Utils.logForging(className));
        }
        LOGGER.debug(
                "state for request: {} is {}", Utils.logForging(className), Utils.logForging(state));
        return state;
    }

    IgniteEventImpl generateAckEvent(AcknowledgementV1_1.Status status, IgniteEvent value) {
        IgniteEventImpl ackIgniteEvent = new IgniteEventImpl();
        AcknowledgementV1_1 ackData = new AcknowledgementV1_1();
        ackData.setStatus(status);
        ackIgniteEvent.setEventData(ackData);
        ackIgniteEvent.setEventId(EventID.ACKNOWLEDGEMENT);
        ackIgniteEvent.setVersion(Version.V1_0);
        ackIgniteEvent.setTimestamp(System.currentTimeMillis());
        ackIgniteEvent.setCorrelationId(value.getMessageId());
        ackIgniteEvent.setMessageId(globalMessageIdGenerator.generateUniqueMsgId(value.getVehicleId()));
        ackIgniteEvent.setBizTransactionId(value.getBizTransactionId());
        ackIgniteEvent.setDeviceRoutable(true);
        ackIgniteEvent.setVehicleId(value.getVehicleId());
        return ackIgniteEvent;
    }

    protected void persistEntity(IgniteEvent value) {

        // save to database
        Ro entity = new Ro();
        entity.setSchemaVersion(value.getSchemaVersion());
        entity.setRoEvent(value);
        entity.setRoStatus(ROStatus.PENDING);
        LOGGER.debug("persisting to mongo ,entity :{}", entity);
        roDAOMongoImpl.save(entity);

        // save to roCache cache with the key: requestId_vehicleId
        Element entityElementToCache = buildElementForCache(value);

        cacheUtil.getCache(Constants.RO_CACHE_NAME).put(entityElementToCache);

        LOGGER.debug("Relevant RO Entity fields are cached :{}", entityElementToCache.toString());
    }

    protected String persistCancelEntity(IgniteEvent value) {
        Ro entity = new Ro();
        entity.setSchemaVersion(value.getSchemaVersion());
        entity.setRoEvent(value);
        entity.setRoStatus(ROStatus.PENDING);
        LOGGER.debug("persisting to mongo ,entity :{}", entity);
        return roDAOMongoImpl.save(entity).getId().toString();
    }

    protected String persistCreateOrUpdateEntity(IgniteEvent value) {
        Ro entity = new Ro();
        entity.setSchemaVersion(value.getSchemaVersion());
        entity.setRoEvent(value);
        entity.setRoStatus(ROStatus.PENDING);
        ObjectId objectId = roDAOMongoImpl.save(entity).getId();
        LOGGER.debug("persisted to mongo ,entity :{}", entity);
        return objectId.toString();
    }

    /**
     * Build Element from Ignite Event.
     *
     * @param value ignite event to convert to Element
     * @return {@link Element} element
     */
    public Element buildElementForCache(IgniteEvent value) {

        Map<String, String> roRequestElementsMap = new HashMap<String, String>();

        // adding event ID value to EventId key in the map
        roRequestElementsMap.put(
                org.eclipse.ecsp.domain.ro.constant.Constants.RO_REQUEST_EVENTID, value.getEventId());

        // adding event's state to the state key in the map
        roRequestElementsMap.put(
                org.eclipse.ecsp.domain.ro.constant.Constants.RO_REQUEST_STATE,
                getRoCommand(value.getEventData()));

        AbstractRoEventData abstractRoEventData = (AbstractRoEventData) value.getEventData();

        // adding event's origin to the origin key in the map
        roRequestElementsMap.put(
                org.eclipse.ecsp.domain.ro.constant.Constants.RO_REQUEST_ORIGIN,
                abstractRoEventData.getOrigin());

        // adding event's user id to the userId key in the map
        roRequestElementsMap.put(
                org.eclipse.ecsp.domain.ro.constant.Constants.RO_REQUEST_USERID,
                abstractRoEventData.getUserId());

        // adding event's partner id to the PartnerId key in the map
        if (ObjectUtils.isNotEmpty(abstractRoEventData.getPartnerId())) {
            roRequestElementsMap.put(EventAttribute.PARTNER_ID, abstractRoEventData.getPartnerId());
        }

        // adding event's ecu type to the ecuType key in the map
        roRequestElementsMap.put(
                org.eclipse.ecsp.domain.ro.constant.Constants.RO_VEHICLE_ECU_TYPE, value.getEcuType());

        // adding event's vehicle arch type id to the archType key in the map
        if (ObjectUtils.isNotEmpty(abstractRoEventData.getVehicleArchType())) {
            roRequestElementsMap.put(
                    org.eclipse.ecsp.domain.ro.constant.Constants.RO_VEHICLE_ARCH_TYPE,
                    abstractRoEventData.getVehicleArchType());
        }

        String cacheKey = value.getRequestId() + Constants.UNDER_SCORE + value.getVehicleId();

        // Cache for each event will have the key as: requestId_vehicleId
        // and the value of the cache will be the map of the event's attributes inclusive
        // of the event's origin, userId, ecuType, archType, eventId and state
        return new Element(cacheKey, roRequestElementsMap);
    }
}
