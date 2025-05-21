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

package org.eclipse.ecsp.platform.services.ro.service;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.RemoteOperationAlarmV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationHornV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationLightsV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationLightsV1_2;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.platform.services.ro.constant.EventIdConstants;
import org.eclipse.ecsp.platform.services.ro.constant.ResponseMsgConstants;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationAlarmReq;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationHornReq;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationLightsReq;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationResponse;
import org.eclipse.ecsp.utils.ApiUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutionException;

/**
 * Service Class for ROHornLightsAlarm Service Operations.
 *
 * @author midnani
 */
@Service
public class ROHornLightsAlarmService {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ROHornLightsAlarmService.class);

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private ApiUtils apiUtils;

    @Autowired
    private Utils utils;

    /**
     * Create remote horn request remote operation response.
     *
     * @param userId            the user id
     * @param vehicleId         the vehicle id
     * @param remoteHornRequest the remote horn request
     * @param sessionId         the session id
     * @param origin            the origin
     * @param ecuType           the ecu type
     * @param vehicleArchType   the vehicle arch type
     * @return the remote operation response
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    public RemoteOperationResponse createRemoteHornRequest(
            String userId, String vehicleId,
            RemoteOperationHornReq remoteHornRequest,
            String sessionId, String origin,
            String ecuType, String vehicleArchType)
            throws InterruptedException, ExecutionException {
        LOGGER.info("Publishing remote Horn command to Kafka for vehicleId:{}, userid :{}",
                Utils.logForging(vehicleId), Utils.logForging(userId));
        RemoteOperationHornV1_1 roHornData = new RemoteOperationHornV1_1();
        roHornData.setState(RemoteOperationHornV1_1.State.valueOf(remoteHornRequest.getState()
                .name()));
        roHornData.setDuration(remoteHornRequest.getDuration());
        roHornData.setRoRequestId(remoteHornRequest.getRoRequestId());
        roHornData.setOrigin(origin);
        roHornData.setUserId(userId);
        roHornData.setVehicleArchType(vehicleArchType);

        IgniteEvent igniteEventImpl = new IgniteEventImplBuilder()
                .withEventId(EventIdConstants.EVENT_ID_HORN.getValue())
                .withVersion(Version.V1_1)
                .withVehicleId(vehicleId)
                .withEventData(roHornData)
                .withRequestId(remoteHornRequest.getRoRequestId())
                .withBizTransactionId(sessionId)
                .withUserContextInfo(apiUtils.getUserContext(userId))
                .withTimestamp(System.currentTimeMillis())
                .withEcuType(ecuType)
                .build();
        LOGGER.debug("Publish remote horn command to Kafka for IgniteEvent: {}", Utils.logForging(igniteEventImpl));
        kafkaService.sendIgniteEvent(igniteEventImpl);
        return new RemoteOperationResponse(ResponseMsgConstants.RO_COMMAND_SUCCESS,
                remoteHornRequest.getRoRequestId());
    }

    /**
     * Create remote lights request remote operation response.
     *
     * @param userId              the user id
     * @param vehicleId           the vehicle id
     * @param remoteLightsRequest the remote lights request
     * @param sessionId           the session id
     * @param origin              the origin
     * @param partnerId           the partner id
     * @param ecuType             the ecu type
     * @param vehicleArchType     the vehicle arch type
     * @return the remote operation response
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    public RemoteOperationResponse createRemoteLightsRequest(
            String userId, String vehicleId,
            RemoteOperationLightsReq remoteLightsRequest,
            String sessionId, String origin,
            String partnerId, String ecuType,
            String vehicleArchType)
            throws InterruptedException, ExecutionException {
        LOGGER.info("Publising remote Lights command to Kafka for vehicleId:{}, userid :{}",
                Utils.logForging(vehicleId), Utils.logForging(userId));
        RemoteOperationLightsV1_1 roLightsData = new RemoteOperationLightsV1_1();
        roLightsData.setState(RemoteOperationLightsV1_1.State.valueOf(remoteLightsRequest.getState().name()));
        roLightsData.setDuration(remoteLightsRequest.getDuration());
        roLightsData.setRoRequestId(remoteLightsRequest.getRoRequestId());
        roLightsData.setOrigin(origin);
        roLightsData.setUserId(userId);
        roLightsData.setVehicleArchType(vehicleArchType);

        if (ObjectUtils.isNotEmpty(partnerId)) {
            roLightsData.setPartnerId(partnerId);
        }

        IgniteEvent igniteEventImpl = new IgniteEventImplBuilder()
                .withEventId(EventIdConstants.EVENT_ID_LIGHTS.getValue())
                .withVersion(Version.V1_1)
                .withVehicleId(vehicleId)
                .withEventData(roLightsData)
                .withRequestId(remoteLightsRequest.getRoRequestId())
                .withBizTransactionId(sessionId)
                .withUserContextInfo(apiUtils.getUserContext(userId))
                .withTimestamp(System.currentTimeMillis())
                .withEcuType(ecuType)
                .build();
        LOGGER.debug("Publish remote light command to Kafka for IgniteEvent: {}", Utils.logForging(igniteEventImpl));
        kafkaService.sendIgniteEvent(igniteEventImpl);
        return new RemoteOperationResponse(ResponseMsgConstants.RO_COMMAND_SUCCESS,
                remoteLightsRequest.getRoRequestId());
    }

    /**
     * Create remote lights only remote operation response.
     *
     * @param userId              the user id
     * @param vehicleId           the vehicle id
     * @param remoteLightsRequest the remote lights request
     * @param sessionId           the session id
     * @param origin              the origin
     * @param partnerId           the partner id
     * @param ecuType             the ecu type
     * @param vehicleArchType     the vehicle arch type
     * @return the remote operation response
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    public RemoteOperationResponse createRemoteLightsOnly(
            String userId, String vehicleId,
            RemoteOperationLightsReq remoteLightsRequest,
            String sessionId, String origin,
            String partnerId, String ecuType,
            String vehicleArchType)
            throws InterruptedException, ExecutionException {
        LOGGER.info("Publishing remote Lights command to Kafka for vehicleId:{}, userid :{}",
                Utils.logForging(vehicleId), Utils.logForging(userId));
        RemoteOperationLightsV1_2 roLightsData = new RemoteOperationLightsV1_2();
        roLightsData.setState(RemoteOperationLightsV1_2.State.valueOf(remoteLightsRequest.getState().name()));
        roLightsData.setDuration(remoteLightsRequest.getDuration());
        roLightsData.setRoRequestId(remoteLightsRequest.getRoRequestId());
        roLightsData.setOrigin(origin);
        roLightsData.setUserId(userId);
        roLightsData.setVehicleArchType(vehicleArchType);

        if (ObjectUtils.isNotEmpty(partnerId)) {
            roLightsData.setPartnerId(partnerId);
        }
        IgniteEvent igniteEventImpl = new IgniteEventImplBuilder()
                .withEventId(EventIdConstants.EVENT_ID_LIGHTS_ONLY.getValue())
                .withVersion(Version.V1_2)
                .withVehicleId(vehicleId)
                .withEventData(roLightsData)
                .withRequestId(remoteLightsRequest.getRoRequestId())
                .withBizTransactionId(sessionId)
                .withUserContextInfo(apiUtils.getUserContext(userId))
                .withTimestamp(System.currentTimeMillis())
                .withEcuType(ecuType)
                .build();
        LOGGER.debug("Publish remote light only command to Kafka for IgniteEvent: {}",
                Utils.logForging(igniteEventImpl));
        kafkaService.sendIgniteEvent(igniteEventImpl);
        return new RemoteOperationResponse(ResponseMsgConstants.RO_COMMAND_SUCCESS,
                remoteLightsRequest.getRoRequestId());
    }

    /**
     * Create remote alarm request remote operation response.
     *
     * @param userId                  the user id
     * @param vehicleId               the vehicle id
     * @param remoteOperationAlarmReq the remote alarm request
     * @param sessionId               the session id
     * @param origin                  the origin
     * @param partnerId               the partner id
     * @param ecuType                 the ecu type
     * @param vehicleArchType         the vehicle arch type
     * @return the remote operation response
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    public RemoteOperationResponse createRemoteAlarmRequest(String userId,
                                                            String vehicleId,
                                                            RemoteOperationAlarmReq remoteOperationAlarmReq,
                                                            String sessionId, String origin,
                                                            String partnerId, String ecuType,
                                                            String vehicleArchType)
            throws InterruptedException, ExecutionException {
        LOGGER.info("Publishing remote Alarm command to Kafka for vehicleId:{}, userid :{}",
                Utils.logForging(vehicleId), Utils.logForging(userId));
        RemoteOperationAlarmV1_1 roAlarmData = new RemoteOperationAlarmV1_1();
        roAlarmData.setState(RemoteOperationAlarmV1_1.State.valueOf(remoteOperationAlarmReq.getState().name()));
        roAlarmData.setDuration(remoteOperationAlarmReq.getDuration());
        roAlarmData.setRoRequestId(remoteOperationAlarmReq.getRoRequestId());
        roAlarmData.setOrigin(origin);
        roAlarmData.setUserId(userId);
        roAlarmData.setVehicleArchType(vehicleArchType);

        if (ObjectUtils.isNotEmpty(partnerId)) {
            roAlarmData.setPartnerId(partnerId);
        }
        IgniteEvent igniteEventImpl = new IgniteEventImplBuilder()
                .withEventId(EventIdConstants.EVENT_ID_ALARM.getValue())
                .withVersion(Version.V1_1)
                .withVehicleId(vehicleId)
                .withEventData(roAlarmData)
                .withRequestId(remoteOperationAlarmReq.getRoRequestId())
                .withBizTransactionId(sessionId)
                .withUserContextInfo(apiUtils.getUserContext(userId))
                .withTimestamp(System.currentTimeMillis())
                .withEcuType(ecuType)
                .build();
        LOGGER.debug("Publish remote alarm command to Kafka for IgniteEvent: {}", Utils.logForging(igniteEventImpl));
        kafkaService.sendIgniteEvent(igniteEventImpl);
        return new RemoteOperationResponse(ResponseMsgConstants.RO_COMMAND_SUCCESS,
                remoteOperationAlarmReq.getRoRequestId());
    }
}
