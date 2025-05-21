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
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.cache.IgniteCache;
import org.eclipse.ecsp.cache.PutStringRequest;
import org.eclipse.ecsp.domain.AcknowledgementV1_1;
import org.eclipse.ecsp.domain.ro.AbstractRoEventData;
import org.eclipse.ecsp.domain.ro.RemoteOperationEngineV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationResponseV1_1;
import org.eclipse.ecsp.domain.ro.Ro;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.entities.UserContext;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteOrderBy;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.nosqldao.Updates;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.ro.notification.identifier.NotificationArchAndECUTypeResolver;
import org.eclipse.ecsp.ro.queue.QueueHandler;
import org.eclipse.ecsp.ro.utils.CachedKeyUtil;
import org.eclipse.ecsp.ro.utils.Utils;
import org.eclipse.ecsp.services.constants.EventAttribute;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * RoResponseStreamProcessor.
 */
@Component(Constants.REMOTE_OPERATION_RESPONSE_EVENT_ID)
public class RoResponseStreamProcessor extends AbstractStreamProcessor {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(RoResponseStreamProcessor.class);

    private static final String RO_REQUEST_ID = "roEvent.eventData.roRequestId";

    private static final String RO_REQUEST_TIMESTAMP = "roEvent.timestamp";

    private static final String RO_REQUEST_VEHICLE_ID = "roEvent.vehicleId";
    private static final Logger log = LoggerFactory.getLogger(RoResponseStreamProcessor.class);

    @Autowired
    private IgniteCache cache;

    @Value("${engine.status.ttl.ms:900000}")
    private long ttl;

    @Autowired
    @Qualifier("responseQueueHandler")
    private QueueHandler queueHandler;

    @Autowired
    private NotificationArchAndECUTypeResolver notificationArchAndECUTypeResolver;

    private static Map<String, String> getStringStringMap(
            Element roRequestCacheElement, Map<String, String> roRequestEntityMap) {
        if (Objects.nonNull(roRequestCacheElement) && !roRequestCacheElement.isExpired()) {
            roRequestEntityMap = (Map<String, String>) roRequestCacheElement.getObjectValue();
            LOGGER.debug("load data from cache. roRequestEntityMap: {}", roRequestEntityMap);
        }
        return roRequestEntityMap;
    }

    @Override
    public void process(
            Record<IgniteKey<?>, IgniteEvent> kafkaRecordIn, StreamProcessingContext ctxt) {
        IgniteEvent value = kafkaRecordIn.value();
        LOGGER.debug("Processing RemoteOperationResponse event:{}", value);
        RemoteOperationResponseV1_1 response = (RemoteOperationResponseV1_1) value.getEventData();
        updateROResponse(value);
        String cacheKey = response.getRoRequestId() + Constants.UNDER_SCORE + value.getVehicleId();
        Element roRequestCacheElement = cacheUtil.getCache(Constants.RO_CACHE_NAME).get(cacheKey);
        Map<String, String> roRequestEntityMap = null;
        roRequestEntityMap = getStringStringMap(roRequestCacheElement, roRequestEntityMap);
        String notificationId = null;
        String origin = null;
        String userId = null;
        String requestEventId = null;
        String requestState = null;
        String ecuType = null;
        String archType = null;
        IgniteKey<?> key = kafkaRecordIn.key();
        if (Objects.nonNull(roRequestEntityMap) && !roRequestEntityMap.isEmpty()) {
            origin = roRequestEntityMap.get(org.eclipse.ecsp.domain.ro.constant.Constants.RO_REQUEST_ORIGIN);
            userId = roRequestEntityMap.get(org.eclipse.ecsp.domain.ro.constant.Constants.RO_REQUEST_USERID);
            requestEventId = roRequestEntityMap.get(org.eclipse.ecsp.domain.ro.constant.Constants.RO_REQUEST_EVENTID);
            requestState = roRequestEntityMap.get(org.eclipse.ecsp.domain.ro.constant.Constants.RO_REQUEST_STATE);
            ecuType = roRequestEntityMap.get(org.eclipse.ecsp.domain.ro.constant.Constants.RO_VEHICLE_ECU_TYPE);
            archType = roRequestEntityMap.get(org.eclipse.ecsp.domain.ro.constant.Constants.RO_VEHICLE_ARCH_TYPE);
            if (ObjectUtils.isNotEmpty(roRequestEntityMap.get(EventAttribute.PARTNER_ID))) {
                response.setPartnerId(roRequestEntityMap.get(EventAttribute.PARTNER_ID));
            }
            LOGGER.debug("userId:{}, eventId:{}, requestState:{}", userId, requestEventId, requestState);
            validateAndMaintainEngineStatus(value, response, requestEventId, requestState);
        } else {
            Ro dbEntity = getRoEntityByFieldName(response.getRoRequestId(), value.getVehicleId());
            LOGGER.debug("load data from db. roRequestId: {}", response.getRoRequestId());
            LOGGER.debug("dbEntity {}", Utils.logForging(dbEntity));
            if (Objects.nonNull(dbEntity)) {
                AbstractRoEventData roEventData = (AbstractRoEventData) dbEntity.getRoEvent().getEventData();
                origin = roEventData.getOrigin();
                userId = roEventData.getUserId();
                archType = roEventData.getVehicleArchType();
                requestState = getRoCommand(dbEntity.getRoEvent().getEventData());
                requestEventId = dbEntity.getRoEvent().getEventId().toUpperCase();
                ecuType = dbEntity.getRoEvent().getEcuType();
                if (ObjectUtils.isNotEmpty(roEventData.getPartnerId())) {
                    response.setPartnerId(roEventData.getPartnerId());
                }
                validateAndMaintainEngineStatus(value, dbEntity, response);
            }
        }
        if (StringUtils.isNotBlank(origin)) {
            createAndForwardEvent(ctxt, key, value, response, origin, userId);
        }
        notificationId = notificationArchAndECUTypeResolver.getNotification(
                        value, requestState, requestEventId, archType, ecuType);
        LOGGER.debug("sendRONotification: value={}, origin={}, notificationId={}, " + "response={}",
                Utils.logForging(value), Utils.logForging(origin), Utils.logForging(notificationId),
                Utils.logForging(response));
        notificationUtil.sendRONotification(key, value, ctxt, origin, notificationId, response);
        forwardAckEvent(key, value, ctxt);
        forwardQueueHandler(key, value, ctxt);
    }

    private void createAndForwardEvent(
            StreamProcessingContext ctxt,
            IgniteKey<?> key,
            IgniteEvent value,
            RemoteOperationResponseV1_1 response,
            String origin,
            String userId) {
        String qualifier =
                Constants.QUALIFIER
                        + Constants.UNDER_SCORE
                        + value.getEventId().toUpperCase()
                        + Constants.UNDER_SCORE
                        + origin.toUpperCase();
        LOGGER.debug(
                "origin :{} , DFF Qualifier : {}",
                Utils.logForging(origin.toUpperCase()),
                Utils.logForging(qualifier));
        IgniteEventImpl event = (IgniteEventImpl) value;
        UserContext userContext = new UserContext();
        userContext.setUserId(userId);
        event.setUserContextInfo(List.of(userContext));
        event.setDFFQualifier(qualifier);
        response.setUserId(userId);
        forwardEvent(key, value, ctxt);
    }

    private void forwardAckEvent(IgniteKey key, IgniteEvent value, StreamProcessingContext ctxt) {
        LOGGER.debug("Processing RemoteOperationResponse Acknowledgement Event");
        if (Objects.nonNull(value.getCorrelationId())
                || Objects.equals(value.getCorrelationId(), "0")) {
            IgniteEventImpl ackIgniteEvent = generateAckEvent(AcknowledgementV1_1.Status.SUCCESS, value);
            LOGGER.debug(
                    "Key={},ackIgniteEvent={}, sinkTopic={}",
                    key.toString(),
                    ackIgniteEvent.toString(),
                    sinkTopics[0]);
            Record<IgniteKey<?>, IgniteEvent> kafkaRecord =
                    new Record<>(key, ackIgniteEvent, System.currentTimeMillis());
            ctxt.forward(kafkaRecord);
        }
    }

    private void forwardEvent(IgniteKey key, IgniteEvent value, StreamProcessingContext ctxt) {
        if (StringUtils.isNotBlank(value.getDFFQualifier())) {
            LOGGER.debug("DFF Qualifier : {}", value.getDFFQualifier());
            Record<IgniteKey<?>, IgniteEvent> kafkaRecord =
                    new Record<>(key, value, System.currentTimeMillis());
            ctxt.forward(kafkaRecord);
        }
    }

    private void forwardQueueHandler(IgniteKey key, IgniteEvent value, StreamProcessingContext ctxt) {
        if (roQueueEnable) {
            queueHandler.process(key, value, ctxt);
        }
    }

    private void validateAndMaintainEngineStatus(
            IgniteEvent value,
            RemoteOperationResponseV1_1 response,
            String requestEventId,
            String requestState) {
        if (engineStartCheckEnable
                && requestEventId.equals(
                org.eclipse.ecsp.domain.ro.constant.Constants.REMOTEOPERATIONENGINE)) {
            maintainEngineStatus(value, requestState, response);
        }
    }

    private void validateAndMaintainEngineStatus(
            IgniteEvent value, Ro dbEntity, RemoteOperationResponseV1_1 response) {
        if (engineStartCheckEnable
                && dbEntity
                .getRoEvent()
                .getEventId()
                .equals(org.eclipse.ecsp.domain.ro.constant.Constants.REMOTEOPERATIONENGINE)) {
            RemoteOperationEngineV1_1 remoteOperationEngineV1 =
                    (RemoteOperationEngineV1_1) dbEntity.getRoEvent().getEventData();
            maintainEngineStatus(value, remoteOperationEngineV1.getState().name(), response);
        }
    }

    /**
     * Method to update RO Response in mongoDB.
     */
    private void updateROResponse(IgniteEvent value) {
        Updates updateRoResponse = new Updates();
        updateRoResponse.addListAppend(
                org.eclipse.ecsp.domain.ro.constant.Constants.RO_RESPONSE_LIST, value);
        Optional<Ro> roOptional =
                roDAOMongoImpl.getROEntityByFieldNameByBizIdExceptACV(
                        value.getVehicleId(), value.getBizTransactionId());
        if (roOptional.isPresent()) {
            LOGGER.debug(
                    "RO request found for vehicleId:{}, bizTransactionId:{}",
                    value.getVehicleId(),
                    value.getBizTransactionId());
            roDAOMongoImpl.update(roOptional.get().getId(), updateRoResponse);
        } else {
            LOGGER.info(
                    "no RO request found for vehicleId:{}, bizTransactionId:{}, value:{}",
                    value.getVehicleId(),
                    value.getBizTransactionId(),
                    value);
        }
    }

    /**
     * Maintain engine status.
     */
    private void maintainEngineStatus(
            IgniteEvent value, String requestState, RemoteOperationResponseV1_1 response) {
        LOGGER.info(
                "Caching engine status for vehicleId:{}, requestState: {}",
                value.getVehicleId(),
                requestState);
        if (response.getResponse().equals(RemoteOperationResponseV1_1.Response.SUCCESS)) {
            String cacheStateKey = CachedKeyUtil.getEngineStatusKey(value);
            LOGGER.debug("cacheKey:{}", cacheStateKey);
            if (requestState.equals(RemoteOperationEngineV1_1.State.STARTED.name())) {
                PutStringRequest request = new PutStringRequest();
                request.withKey(cacheStateKey);
                request.withValue(RemoteOperationEngineV1_1.State.STARTED.name());
                request.withTtlMs(ttl);
                cache.putString(request);
            } else if (requestState.equals(RemoteOperationEngineV1_1.State.STOPPED.name())) {
                LOGGER.info(
                        "Deleting Cache for engine stopped status for vehicleId:{}", value.getVehicleId());
                cache.delete(cacheStateKey);
            } else {
                LOGGER.debug("unknown engine state:{}", requestState);
            }
        }
    }

    private Ro getRoEntityByFieldName(String roRequestId, String vehicleId) {
        IgniteCriteria criteria = new IgniteCriteria(RO_REQUEST_ID, Operator.EQ, roRequestId);
        IgniteCriteria vehicleIdCriteria =
                new IgniteCriteria(RO_REQUEST_VEHICLE_ID, Operator.EQ, vehicleId);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(criteria);
        criteriaGroup.and(vehicleIdCriteria);
        IgniteQuery igQuery =
                new IgniteQuery(criteriaGroup)
                        .orderBy(new IgniteOrderBy().byfield(RO_REQUEST_TIMESTAMP).desc());
        List<Ro> entities = roDAOMongoImpl.find(igQuery);
        if (!entities.isEmpty()) {
            return entities.get(Constants.ZERO);
        }
        return null;
    }
}
