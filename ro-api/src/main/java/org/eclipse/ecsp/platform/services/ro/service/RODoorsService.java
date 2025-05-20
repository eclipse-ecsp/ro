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
import org.eclipse.ecsp.domain.ro.RemoteOperationDoorsV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationDriverDoorV1_1;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.platform.services.ro.constant.EventIdConstants;
import org.eclipse.ecsp.platform.services.ro.constant.ResponseMsgConstants;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationDoorsReq;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationDriverDoorReq;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationResponse;
import org.eclipse.ecsp.utils.ApiUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutionException;

/**
 * Service Class for RO Doors Operation.
 *
 * @author midnani
 */
@Service
public class RODoorsService {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(RODoorsService.class);

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private ApiUtils apiUtils;

    @Autowired
    private Utils utils;

    /**
     * Create remote doors request.
     *
     * @param userId             the user id
     * @param vehicleId          the vehicle id
     * @param remoteDoorsRequest the remote doors request
     * @param sessionId          the session id
     * @param origin             the origin
     * @param partnerId          the partner id
     * @param ecuType            the ecu type
     * @param vehicleArchType    the vehicle arch type
     * @return the remote operation response
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    public RemoteOperationResponse createRemoteDoorsRequest(
            String userId,
            String vehicleId,
            RemoteOperationDoorsReq remoteDoorsRequest,
            String sessionId,
            String origin,
            String partnerId,
            String ecuType,
            String vehicleArchType)
            throws InterruptedException, ExecutionException {
        LOGGER.info(
                "Publishing remote doors command to Kafka for vehicleId:{}," + " userid :{}",
                Utils.logForging(vehicleId),
                Utils.logForging(userId));

        // create data
        RemoteOperationDoorsV1_1 roDoorData = new RemoteOperationDoorsV1_1();
        roDoorData.setState(
                RemoteOperationDoorsV1_1.State.valueOf(remoteDoorsRequest.getState().name()));
        roDoorData.setRoRequestId(remoteDoorsRequest.getRoRequestId());
        roDoorData.setOrigin(origin);
        roDoorData.setUserId(userId);
        roDoorData.setVehicleArchType(vehicleArchType);

        if (ObjectUtils.isNotEmpty(partnerId)) {
            roDoorData.setPartnerId(partnerId);
        }
        IgniteEvent igniteEventImpl =
                new IgniteEventImplBuilder()
                        .withEventId(EventIdConstants.EVENT_ID_DOORS.getValue())
                        .withVersion(Version.V1_1)
                        .withVehicleId(vehicleId)
                        .withEventData(roDoorData)
                        .withRequestId(roDoorData.getRoRequestId())
                        .withBizTransactionId(sessionId)
                        .withUserContextInfo(apiUtils.getUserContext(userId))
                        .withTimestamp(System.currentTimeMillis())
                        .withEcuType(ecuType)
                        .build();
        LOGGER.debug(
                "Publish remote door command to Kafka for IgniteEvent: {}",
                Utils.logForging(igniteEventImpl));
        kafkaService.sendIgniteEvent(igniteEventImpl);
        return new RemoteOperationResponse(
                ResponseMsgConstants.RO_COMMAND_SUCCESS, remoteDoorsRequest.getRoRequestId());
    }

    /**
     * Create remote driver door request remote.
     *
     * @param userId                  the user id
     * @param vehicleId               the vehicle id
     * @param remoteDriverDoorRequest the remote driver door request
     * @param sessionId               the session id
     * @param origin                  the origin
     * @param partnerId               the partner id
     * @param ecuType                 the ecu type
     * @param vehicleArchType         the vehicle arch type
     * @return the remote operation response
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    public RemoteOperationResponse createRemoteDriverDoorRequest(
            String userId,
            String vehicleId,
            RemoteOperationDriverDoorReq remoteDriverDoorRequest,
            String sessionId,
            String origin,
            String partnerId,
            String ecuType,
            String vehicleArchType)
            throws InterruptedException, ExecutionException {
        LOGGER.info(
                "Publish remote doors command to Kafka for vehicleId:{}, userid :{}",
                Utils.logForging(vehicleId),
                Utils.logForging(userId));
        // create data
        RemoteOperationDriverDoorV1_1 roDriverDoorData = new RemoteOperationDriverDoorV1_1();
        roDriverDoorData.setState(
                RemoteOperationDriverDoorV1_1.State.valueOf(remoteDriverDoorRequest.getState().name()));
        roDriverDoorData.setRoRequestId(remoteDriverDoorRequest.getRoRequestId());
        roDriverDoorData.setOrigin(origin);
        roDriverDoorData.setUserId(userId);
        if (ObjectUtils.isNotEmpty(partnerId)) {
            roDriverDoorData.setPartnerId(partnerId);
        }
        roDriverDoorData.setVehicleArchType(vehicleArchType);

        IgniteEvent igniteEventImpl =
                new IgniteEventImplBuilder()
                        .withEventId(EventIdConstants.EVENT_ID_DRIVER_DOOR.getValue())
                        .withVersion(Version.V1_1)
                        .withVehicleId(vehicleId)
                        .withEventData(roDriverDoorData)
                        .withRequestId(remoteDriverDoorRequest.getRoRequestId())
                        .withBizTransactionId(sessionId)
                        .withUserContextInfo(apiUtils.getUserContext(userId))
                        .withTimestamp(System.currentTimeMillis())
                        .withEcuType(ecuType)
                        .build();
        LOGGER.debug(
                "Publish remote door command to Kafka for IgniteEvent: {}",
                Utils.logForging(igniteEventImpl));
        kafkaService.sendIgniteEvent(igniteEventImpl);
        return new RemoteOperationResponse(
                ResponseMsgConstants.RO_COMMAND_SUCCESS, remoteDriverDoorRequest.getRoRequestId());
    }
}
