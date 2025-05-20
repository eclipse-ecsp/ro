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
import org.eclipse.ecsp.domain.ro.RemoteOperationGloveBoxV2_0;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.platform.services.ro.constant.EventIdConstants;
import org.eclipse.ecsp.platform.services.ro.constant.ResponseMsgConstants;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationGloveBoxReq;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationResponse;
import org.eclipse.ecsp.utils.ApiUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutionException;

/**
 * Service Class for Ro Glove Box operation.
 *
 * @author Arnold
 */
@Service
public class ROGloveBoxService {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ROGloveBoxService.class);

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private ApiUtils apiUtils;

    @Autowired
    private Utils utils;

    /**
     * Create remote glove box request .
     *
     * @param userId                     the user id
     * @param vehicleId                  the vehicle id
     * @param remoteOperationGloveBoxReq the remote glove box request
     * @param sessionId                  the session id
     * @param origin                     the origin
     * @param partnerId                  the partner id
     * @param ecuType                    the ecu type
     * @param vehicleArchType            the vehicle arch type
     * @return the remote operation response
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    public RemoteOperationResponse createRemoteGloveBoxRequest(
            String userId, String vehicleId,
            RemoteOperationGloveBoxReq remoteOperationGloveBoxReq,
            String sessionId, String origin,
            String partnerId, String ecuType,
            String vehicleArchType)
            throws InterruptedException, ExecutionException {
        LOGGER.info("Publishing remote glove box command to Kafka for vehicleId:{}, userid :{}",
                Utils.logForging(vehicleId), Utils.logForging(userId));
        // create data
        RemoteOperationGloveBoxV2_0 roGloveBoxData = new RemoteOperationGloveBoxV2_0();
        roGloveBoxData.setState(RemoteOperationGloveBoxV2_0.State.valueOf(
                remoteOperationGloveBoxReq.getState().name())
        );
        roGloveBoxData.setRoRequestId(remoteOperationGloveBoxReq.getRoRequestId());
        roGloveBoxData.setOrigin(origin);
        roGloveBoxData.setUserId(userId);
        roGloveBoxData.setVehicleArchType(vehicleArchType);

        if (ObjectUtils.isNotEmpty(partnerId)) {
            roGloveBoxData.setPartnerId(partnerId);
        }

        IgniteEvent igniteEventImpl = new IgniteEventImplBuilder()
                .withEventId(EventIdConstants.EVENT_ID_GLOVEBOX.getValue())
                .withVersion(Version.V2_0)
                .withVehicleId(vehicleId)
                .withEventData(roGloveBoxData)
                .withRequestId(remoteOperationGloveBoxReq.getRoRequestId())
                .withBizTransactionId(sessionId)
                .withUserContextInfo(apiUtils.getUserContext(userId))
                .withTimestamp(System.currentTimeMillis())
                .withEcuType(ecuType)
                .build();

        LOGGER.debug("Publish remote glove box command to Kafka for IgniteEvent: {}",
                Utils.logForging(igniteEventImpl));

        kafkaService.sendIgniteEvent(igniteEventImpl);
        return new RemoteOperationResponse(ResponseMsgConstants.RO_COMMAND_SUCCESS,
                remoteOperationGloveBoxReq.getRoRequestId());
    }
}
