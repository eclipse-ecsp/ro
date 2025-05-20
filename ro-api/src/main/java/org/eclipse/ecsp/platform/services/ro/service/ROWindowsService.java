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

import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.RemoteOperationDriverWindowV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationWindowsV1_1;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.platform.services.ro.constant.EventIdConstants;
import org.eclipse.ecsp.platform.services.ro.constant.ResponseMsgConstants;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationDriverWindowReq;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationResponse;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationWindowsReq;
import org.eclipse.ecsp.utils.ApiUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutionException;

/**
 * Controller for Ro windows services.
 *
 * @author midnani
 */
@Service
public class ROWindowsService {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ROWindowsService.class);

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private ApiUtils apiUtils;

    @Autowired
    private Utils utils;

    /**
     * Create remote windows request.
     *
     * @param userId              the user id
     * @param vehicleId           the vehicle id
     * @param remoteWindowRequest the remote window request
     * @param sessionId           the session id
     * @param origin              the origin
     * @param ecuType             the ecu type
     * @param vehicleArchType     the vehicle arch type
     * @return the remote operation response
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    public RemoteOperationResponse createRemoteWindowsRequest(
            String userId, String vehicleId,
            RemoteOperationWindowsReq remoteWindowRequest,
            String sessionId, String origin,
            String ecuType, String vehicleArchType)
            throws InterruptedException, ExecutionException {
        LOGGER.info("Publishing remote window command to Kafka for vehicleId:{}, userid :{}",
                Utils.logForging(vehicleId), Utils.logForging(userId));
        RemoteOperationWindowsV1_1 roWindowsData = new RemoteOperationWindowsV1_1();
        roWindowsData.setState(RemoteOperationWindowsV1_1.State.valueOf(remoteWindowRequest.getState().name()));
        roWindowsData.setPercent(remoteWindowRequest.getPercent());
        roWindowsData.setDuration(remoteWindowRequest.getDuration());
        roWindowsData.setRoRequestId(remoteWindowRequest.getRoRequestId());
        roWindowsData.setOrigin(origin);
        roWindowsData.setUserId(userId);
        roWindowsData.setVehicleArchType(vehicleArchType);

        IgniteEvent igniteEventImpl = new IgniteEventImplBuilder()
                .withEventId(EventIdConstants.EVENT_ID_WINDOWS.getValue())
                .withVersion(Version.V1_1)
                .withVehicleId(vehicleId)
                .withEventData(roWindowsData)
                .withRequestId(remoteWindowRequest.getRoRequestId())
                .withBizTransactionId(sessionId)
                .withUserContextInfo(apiUtils.getUserContext(userId))
                .withTimestamp(System.currentTimeMillis())
                .withEcuType(ecuType)
                .build();
        LOGGER.debug("Publish remote cargo box command to Kafka for IgniteEvent: {}",
                Utils.logForging(igniteEventImpl));
        kafkaService.sendIgniteEvent(igniteEventImpl);
        return new RemoteOperationResponse(ResponseMsgConstants.RO_COMMAND_SUCCESS,
                remoteWindowRequest.getRoRequestId());
    }

    /**
     * Create remote driver window request.
     *
     * @param userId                         the user id
     * @param vehicleId                      the vehicle id
     * @param remoteOperationDriverWindowReq the remote driver window request
     * @param sessionId                      the session id
     * @param origin                         the origin
     * @param ecuType                        the ecu type
     * @param vehicleArchType                the vehicle arch type
     * @return the remote operation response
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    public RemoteOperationResponse createRemoteDriverWindowRequest(
            String userId, String vehicleId,
            RemoteOperationDriverWindowReq remoteOperationDriverWindowReq,
            String sessionId, String origin,
            String ecuType, String vehicleArchType)
            throws InterruptedException, ExecutionException {
        LOGGER.info("Publishing remote window command to Kafka for vehicleId:{}, userid :{}",
                Utils.logForging(vehicleId), Utils.logForging(userId));
        RemoteOperationDriverWindowV1_1 roDriverWindowsData = new RemoteOperationDriverWindowV1_1();
        roDriverWindowsData.setState(RemoteOperationDriverWindowV1_1.State.valueOf(
                remoteOperationDriverWindowReq.getState().name()));
        roDriverWindowsData.setPercent(remoteOperationDriverWindowReq.getPercent());
        roDriverWindowsData.setDuration(remoteOperationDriverWindowReq.getDuration());
        roDriverWindowsData.setRoRequestId(remoteOperationDriverWindowReq.getRoRequestId());
        roDriverWindowsData.setOrigin(origin);
        roDriverWindowsData.setUserId(userId);
        roDriverWindowsData.setVehicleArchType(vehicleArchType);

        IgniteEvent igniteEventImpl = new IgniteEventImplBuilder()
                .withEventId(EventIdConstants.EVENT_ID_DRIVER_WINDOW.getValue())
                .withVersion(Version.V1_1)
                .withVehicleId(vehicleId)
                .withEventData(roDriverWindowsData)
                .withRequestId(roDriverWindowsData.getRoRequestId())
                .withBizTransactionId(sessionId)
                .withUserContextInfo(apiUtils.getUserContext(userId))
                .withTimestamp(System.currentTimeMillis())
                .withEcuType(ecuType)
                .build();
        LOGGER.debug("Publish remote door command to Kafka for IgniteEvent: {}", Utils.logForging(igniteEventImpl));
        kafkaService.sendIgniteEvent(igniteEventImpl);
        return new RemoteOperationResponse(ResponseMsgConstants.RO_COMMAND_SUCCESS,
                remoteOperationDriverWindowReq.getRoRequestId());
    }
}
