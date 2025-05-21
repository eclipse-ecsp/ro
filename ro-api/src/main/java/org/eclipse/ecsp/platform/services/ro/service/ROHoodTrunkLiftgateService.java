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
import org.eclipse.ecsp.domain.ro.RemoteOperationHoodV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationLiftgateV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationLiftgateV2_0;
import org.eclipse.ecsp.domain.ro.RemoteOperationTrunkV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationTrunkV2_0;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.platform.services.ro.constant.EventIdConstants;
import org.eclipse.ecsp.platform.services.ro.constant.ResponseMsgConstants;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationHoodReq;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationLiftGateRequestV2_0;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationLiftgateRequest;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationResponse;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationTrunkRequest;
import org.eclipse.ecsp.utils.ApiUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutionException;

/**
 * Service class for Ro hood trunk liftgate operations.
 *
 * @author midnani
 */
@Service
public class ROHoodTrunkLiftgateService {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ROHoodTrunkLiftgateService.class);

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private ApiUtils apiUtils;

    @Autowired
    private Utils utils;

    /**
     * Create remote hood request remote operation response.
     *
     * @param userId                 the user id
     * @param vehicleId              the vehicle id
     * @param remoteOperationHoodReq the remote hood request
     * @param sessionId              the session id
     * @param origin                 the origin
     * @param ecuType                the ecu type
     * @param vehicleArchType        the vehicle arch type
     * @return remote operation response
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    public RemoteOperationResponse createRemoteHoodRequest(
            String userId, String vehicleId,
            RemoteOperationHoodReq remoteOperationHoodReq,
            String sessionId, String origin,
            String ecuType, String vehicleArchType)
            throws InterruptedException, ExecutionException {
        LOGGER.info("Publishing remote hood command to Kafka for vehicleId:{}, userid :{}",
                Utils.logForging(vehicleId), Utils.logForging(userId));
        RemoteOperationHoodV1_1 roHoodData = new RemoteOperationHoodV1_1();
        roHoodData.setState(RemoteOperationHoodV1_1.State.valueOf(remoteOperationHoodReq.getState().name()));
        roHoodData.setRoRequestId(remoteOperationHoodReq.getRoRequestId());
        roHoodData.setOrigin(origin);
        roHoodData.setUserId(userId);
        roHoodData.setVehicleArchType(vehicleArchType);

        IgniteEvent igniteEventImpl = new IgniteEventImplBuilder()
                .withEventId(EventIdConstants.EVENT_ID_HOOD.getValue())
                .withVersion(Version.V1_1)
                .withVehicleId(vehicleId)
                .withEventData(roHoodData)
                .withRequestId(remoteOperationHoodReq.getRoRequestId())
                .withBizTransactionId(sessionId)
                .withUserContextInfo(apiUtils.getUserContext(userId))
                .withTimestamp(System.currentTimeMillis())
                .withEcuType(ecuType)
                .build();
        LOGGER.debug("Publish remote hood command to Kafka for IgniteEvent: {}", Utils.logForging(igniteEventImpl));
        kafkaService.sendIgniteEvent(igniteEventImpl);

        return new RemoteOperationResponse(
                ResponseMsgConstants.RO_COMMAND_SUCCESS,
                remoteOperationHoodReq.getRoRequestId());
    }

    /**
     * Create remote trunk request remote operation response.
     *
     * @param userId             the user id
     * @param vehicleId          the vehicle id
     * @param remoteTrunkRequest the remote trunk request
     * @param sessionId          the session id
     * @param origin             the origin
     * @param ecuType            the ecu type
     * @param vehicleArchType    the vehicle arch type
     * @return remote operation response
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    public RemoteOperationResponse createRemoteTrunkRequest(
            String userId, String vehicleId,
            RemoteOperationTrunkRequest remoteTrunkRequest,
            String sessionId, String origin,
            String ecuType, String vehicleArchType)
            throws InterruptedException, ExecutionException {
        LOGGER.info("Publishing remote trunk command to Kafka for vehicleId:{}, userid :{}",
                Utils.logForging(vehicleId), Utils.logForging(userId));
        RemoteOperationTrunkV1_1 roTrunkData = new RemoteOperationTrunkV1_1();
        roTrunkData.setState(RemoteOperationTrunkV1_1.State.valueOf(remoteTrunkRequest.getState().name()));
        roTrunkData.setRoRequestId(remoteTrunkRequest.getRoRequestId());
        roTrunkData.setOrigin(origin);
        roTrunkData.setUserId(userId);
        roTrunkData.setVehicleArchType(vehicleArchType);

        IgniteEvent igniteEventImpl = new IgniteEventImplBuilder()
                .withEventId(EventIdConstants.EVENT_ID_TRUNK.getValue())
                .withVersion(Version.V1_1)
                .withVehicleId(vehicleId)
                .withEventData(roTrunkData)
                .withRequestId(remoteTrunkRequest.getRoRequestId())
                .withBizTransactionId(sessionId)
                .withUserContextInfo(apiUtils.getUserContext(userId))
                .withTimestamp(System.currentTimeMillis())
                .withEcuType(ecuType)
                .build();
        LOGGER.debug("Publish remote trunk command to Kafka for IgniteEvent: {}", Utils.logForging(igniteEventImpl));
        kafkaService.sendIgniteEvent(igniteEventImpl);
        return new RemoteOperationResponse(ResponseMsgConstants.RO_COMMAND_SUCCESS,
                remoteTrunkRequest.getRoRequestId());
    }

    /**
     * Create remote trunk request v 2 remote operation response.
     *
     * @param userId             the user id
     * @param vehicleId          the vehicle id
     * @param remoteTrunkRequest the remote trunk request
     * @param sessionId          the session id
     * @param origin             the origin
     * @param partnerId          the partner id
     * @param ecuType            the ecu type
     * @param vehicleArchType    the vehicle arch type
     * @return remote operation response
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    public RemoteOperationResponse createRemoteTrunkRequest_v2(
            String userId, String vehicleId,
            RemoteOperationTrunkRequest remoteTrunkRequest,
            String sessionId, String origin,
            String partnerId, String ecuType,
            String vehicleArchType)
            throws InterruptedException, ExecutionException {
        LOGGER.info("Publishing remote trunk command to Kafka for vehicleId:{}, userId :{}",
                Utils.logForging(vehicleId), Utils.logForging(userId));
        RemoteOperationTrunkV2_0 roTrunkData = new RemoteOperationTrunkV2_0();
        roTrunkData.setState(RemoteOperationTrunkV2_0.State.valueOf(remoteTrunkRequest.getState().name()));
        roTrunkData.setRoRequestId(remoteTrunkRequest.getRoRequestId());
        roTrunkData.setOrigin(origin);
        roTrunkData.setUserId(userId);
        roTrunkData.setVehicleArchType(vehicleArchType);

        if (ObjectUtils.isNotEmpty(partnerId)) {
            roTrunkData.setPartnerId(partnerId);
        }
        IgniteEvent igniteEventImpl = new IgniteEventImplBuilder()
                .withEventId(EventIdConstants.EVENT_ID_TRUNK.getValue())
                .withVersion(Version.V2_0)
                .withVehicleId(vehicleId)
                .withEventData(roTrunkData)
                .withRequestId(remoteTrunkRequest.getRoRequestId())
                .withBizTransactionId(sessionId)
                .withUserContextInfo(apiUtils.getUserContext(userId))
                .withTimestamp(System.currentTimeMillis())
                .withEcuType(ecuType)
                .build();
        LOGGER.debug("Publish remote trunk command to Kafka for IgniteEvent: {}", Utils.logForging(igniteEventImpl));
        kafkaService.sendIgniteEvent(igniteEventImpl);
        return new RemoteOperationResponse(ResponseMsgConstants.RO_COMMAND_SUCCESS,
                remoteTrunkRequest.getRoRequestId());
    }

    /**
     * Create remote liftgate request remote operation response.
     *
     * @param userId                the user id
     * @param vehicleId             the vehicle id
     * @param remoteLiftgateRequest the remote liftgate request
     * @param sessionId             the session id
     * @param origin                the origin
     * @param ecuType               the ecu type
     * @param vehicleArchType       the vehicle arch type
     * @return remote operation response
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    public RemoteOperationResponse createRemoteLiftgateRequest(
            String userId, String vehicleId,
            RemoteOperationLiftgateRequest remoteLiftgateRequest,
            String sessionId, String origin,
            String ecuType, String vehicleArchType)
            throws InterruptedException, ExecutionException {
        LOGGER.info("Publishing remote liftgate command to Kafka for vehicleId:{}, userid :{}",
                Utils.logForging(vehicleId), Utils.logForging(userId));
        RemoteOperationLiftgateV1_1 roLiftgateData = new RemoteOperationLiftgateV1_1();
        roLiftgateData.setState(RemoteOperationLiftgateV1_1.State.valueOf(remoteLiftgateRequest.getState().name()));
        roLiftgateData.setRoRequestId(remoteLiftgateRequest.getRoRequestId());
        roLiftgateData.setOrigin(origin);
        roLiftgateData.setUserId(userId);
        roLiftgateData.setVehicleArchType(vehicleArchType);

        IgniteEvent igniteEventImpl = new IgniteEventImplBuilder()
                .withEventId(EventIdConstants.EVENT_ID_LIFTGATE.getValue())
                .withVersion(Version.V1_1)
                .withVehicleId(vehicleId)
                .withEventData(roLiftgateData)
                .withRequestId(remoteLiftgateRequest.getRoRequestId())
                .withBizTransactionId(sessionId)
                .withUserContextInfo(apiUtils.getUserContext(userId))
                .withTimestamp(System.currentTimeMillis())
                .withEcuType(ecuType)
                .build();
        LOGGER.debug("Publish remote liftgate command to Kafka for IgniteEvent: {}", Utils.logForging(igniteEventImpl));
        kafkaService.sendIgniteEvent(igniteEventImpl);
        return new RemoteOperationResponse(ResponseMsgConstants.RO_COMMAND_SUCCESS,
                remoteLiftgateRequest.getRoRequestId());
    }

    /**
     * Create remote liftgate request v 2 remote operation response.
     *
     * @param userId                the user id
     * @param vehicleId             the vehicle id
     * @param remoteLiftgateRequest the remote liftgate request
     * @param sessionId             the session id
     * @param origin                the origin
     * @param partnerId             the partner id
     * @param ecuType               the ecu type
     * @param vehicleArchType       the vehicle arch type
     * @return remote operation response
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    public RemoteOperationResponse createRemoteLiftgateRequest_v2(
            String userId, String vehicleId,
            RemoteOperationLiftGateRequestV2_0 remoteLiftgateRequest,
            String sessionId, String origin,
            String partnerId, String ecuType,
            String vehicleArchType)
            throws InterruptedException, ExecutionException {
        LOGGER.info("Publishing remote liftgate command to Kafka for vehicleId:{}, userid :{}",
                Utils.logForging(vehicleId), Utils.logForging(userId));
        RemoteOperationLiftgateV2_0 roLiftgateData = new RemoteOperationLiftgateV2_0();
        roLiftgateData.setState(RemoteOperationLiftgateV2_0.State.valueOf(remoteLiftgateRequest.getState().name()));
        roLiftgateData.setRoRequestId(remoteLiftgateRequest.getRoRequestId());
        roLiftgateData.setOrigin(origin);
        roLiftgateData.setUserId(userId);
        roLiftgateData.setVehicleArchType(vehicleArchType);

        if (ObjectUtils.isNotEmpty(partnerId)) {
            roLiftgateData.setPartnerId(partnerId);
        }
        IgniteEvent igniteEventImpl = new IgniteEventImplBuilder()
                .withEventId(EventIdConstants.EVENT_ID_LIFTGATE.getValue())
                .withVersion(Version.V2_0)
                .withVehicleId(vehicleId)
                .withEventData(roLiftgateData)
                .withRequestId(remoteLiftgateRequest.getRoRequestId())
                .withBizTransactionId(sessionId)
                .withUserContextInfo(apiUtils.getUserContext(userId))
                .withTimestamp(System.currentTimeMillis())
                .withEcuType(ecuType)
                .build();
        LOGGER.debug("Publish remote liftgate command to Kafka for IgniteEvent: {}", Utils.logForging(igniteEventImpl));
        kafkaService.sendIgniteEvent(igniteEventImpl);
        return new RemoteOperationResponse(ResponseMsgConstants.RO_COMMAND_SUCCESS,
                remoteLiftgateRequest.getRoRequestId());
    }
}
