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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.RemoteOperationClimateV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationClimateV2_0;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.platform.services.ro.constant.EventIdConstants;
import org.eclipse.ecsp.platform.services.ro.constant.ResponseMsgConstants;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationClimateReq;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationClimateReqV2_0;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationResponse;
import org.eclipse.ecsp.utils.ApiUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutionException;

/**
 * Service class for RO Climate.
 *
 * @author midnani
 */
@Service
public class ROClimateService {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ROClimateService.class);

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private ApiUtils apiUtils;

    @Autowired
    private Utils utils;

    /**
     * Create remote climate request.
     *
     * @param userId                    the user id
     * @param vehicleId                 the vehicle id
     * @param remoteOperationClimateReq the remote climate request
     * @param sessionId                 the session id
     * @param originId                  the origin id
     * @param partnerId                 the partner id
     * @param ecuType                   the ecu type
     * @param vehicleArchType           the vehicle arch type
     * @return the remote operation response
     * @throws ExecutionException the execution exception
     */
    public RemoteOperationResponse createRemoteClimateRequest(
            String userId,
            String vehicleId,
            RemoteOperationClimateReq remoteOperationClimateReq,
            String sessionId,
            String originId,
            String partnerId,
            String ecuType,
            String vehicleArchType)
            throws
            ExecutionException {

        LOGGER.info("Publishing remote climate command to Kafka for vehicleId:{},"
                        + " userid :{}",
                Utils.logForging(vehicleId),
                Utils.logForging(userId));

        RemoteOperationClimateV1_1 roClimateData = new RemoteOperationClimateV1_1();

        roClimateData.setState(RemoteOperationClimateV1_1.State.valueOf(remoteOperationClimateReq.getState().name()));
        roClimateData.setRoRequestId(remoteOperationClimateReq.getRoRequestId());
        roClimateData.setOrigin(originId);
        roClimateData.setUserId(userId);
        roClimateData.setVehicleArchType(vehicleArchType);

        if (remoteOperationClimateReq.getAcState() != null) {
            roClimateData.setAcState(RemoteOperationClimateV1_1.AcState.valueOf(remoteOperationClimateReq
                    .getAcState().name()));
        }

        if (remoteOperationClimateReq.getDuration() != null) {
            roClimateData.setDuration(remoteOperationClimateReq.getDuration());
        }

        if (remoteOperationClimateReq.getTemperature() != null) {
            roClimateData.setTemperature(remoteOperationClimateReq.getTemperature());
        }

        if (remoteOperationClimateReq.getTemperatureUnit() != null) {
            roClimateData.setTemperatureUnit(
                    RemoteOperationClimateV1_1.TemperatureUnit.valueOf(
                            remoteOperationClimateReq.getTemperatureUnit().name())
            );
        }

        if (remoteOperationClimateReq.getFanSpeed() != null) {
            roClimateData.setFanSpeed(remoteOperationClimateReq.getFanSpeed());
        }

        if (ObjectUtils.isNotEmpty(partnerId)) {
            roClimateData.setPartnerId(partnerId);
        }

        IgniteEvent igniteEventImpl = new IgniteEventImplBuilder()
                .withEventId(EventIdConstants.EVENT_ID_CLIMATE.getValue())
                .withVersion(Version.V1_1)
                .withVehicleId(vehicleId)
                .withEventData(roClimateData)
                .withRequestId(remoteOperationClimateReq.getRoRequestId())
                .withBizTransactionId(sessionId)
                .withUserContextInfo(apiUtils.getUserContext(userId))
                .withTimestamp(System.currentTimeMillis())
                .withEcuType(ecuType)
                .build();

        LOGGER.debug("Publish remote climate command to Kafka for IgniteEvent: {}", Utils.logForging(igniteEventImpl));
        kafkaService.sendIgniteEvent(igniteEventImpl);

        return new RemoteOperationResponse(
                ResponseMsgConstants.RO_COMMAND_SUCCESS,
                remoteOperationClimateReq.getRoRequestId());
    }

    /**
     * Create remote hvac request.
     *
     * @param userId               the user id
     * @param vehicleId            the vehicle id
     * @param remoteClimateRequest the remote climate request
     * @param sessionId            the session id
     * @param originId             the origin id
     * @param partnerId            the partner id
     * @param ecuType              the ecu type
     * @param vehicleArchType      the vehicle arch type
     * @return the remote operation response
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws JsonProcessingException the json processing exception
     */
    public RemoteOperationResponse createRemoteHVACRequest(
            String userId,
            String vehicleId,
            RemoteOperationClimateReqV2_0 remoteClimateRequest,
            String sessionId,
            String originId,
            String partnerId,
            String ecuType,
            String vehicleArchType)
            throws
            InterruptedException,
            ExecutionException,
            JsonProcessingException {

        LOGGER.info("Publishing remote climate command to Kafka for vehicleId:{}"
                        + ", userid: {}",
                Utils.logForging(vehicleId),
                Utils.logForging(userId));

        RemoteOperationClimateV2_0 roClimateData = new RemoteOperationClimateV2_0();

        roClimateData.setState(RemoteOperationClimateV2_0.State.valueOf(remoteClimateRequest.getState().name()));
        roClimateData.setRoRequestId(remoteClimateRequest.getRoRequestId());
        roClimateData.setOrigin(originId);
        roClimateData.setUserId(userId);
        roClimateData.setVehicleArchType(vehicleArchType);

        if (null != remoteClimateRequest.getTemperature()) {
            roClimateData.setTemperature(remoteClimateRequest.getTemperature());
        }

        if (null != remoteClimateRequest.getTemperatureUnit()) {
            roClimateData.setTemperatureUnit(
                    RemoteOperationClimateV2_0.TemperatureUnit.valueOf(
                            remoteClimateRequest.getTemperatureUnit().name()));
        }

        if (null != remoteClimateRequest.getFanSpeed()) {
            roClimateData.setFanSpeed(remoteClimateRequest.getFanSpeed());
        }

        if (null != remoteClimateRequest.getTargetTemperature()) {
            roClimateData.setTargetTemperature(remoteClimateRequest.getTargetTemperature());
        }

        if (null != remoteClimateRequest.getTimeoutForAfterTemperature()) {
            roClimateData.setTimeoutForAfterTemperature(remoteClimateRequest.getTimeoutForAfterTemperature());
        }

        if (null != remoteClimateRequest.getTimeOutPreTrip()) {
            roClimateData.setTimeOutPreTrip(remoteClimateRequest.getTimeOutPreTrip());
        }

        if (ObjectUtils.isNotEmpty(partnerId)) {
            roClimateData.setPartnerId(partnerId);
        }

        IgniteEvent igniteEventImpl = new IgniteEventImplBuilder()
                .withEventId(EventIdConstants.EVENT_ID_CLIMATE.getValue())
                .withVersion(Version.V2_0)
                .withVehicleId(vehicleId)
                .withEventData(roClimateData)
                .withRequestId(remoteClimateRequest.getRoRequestId())
                .withBizTransactionId(sessionId)
                .withUserContextInfo(apiUtils.getUserContext(userId))
                .withTimestamp(System.currentTimeMillis())
                .withEcuType(ecuType)
                .build();

        LOGGER.debug("Publish remote climate command to Kafka for IgniteEvent: {}", Utils.logForging(igniteEventImpl));
        kafkaService.sendIgniteEvent(igniteEventImpl);

        return new RemoteOperationResponse(
                ResponseMsgConstants.RO_COMMAND_SUCCESS,
                remoteClimateRequest.getRoRequestId());
    }

}
