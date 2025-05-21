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
import org.eclipse.ecsp.domain.Constants;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.remoteInhibit.RemoteInhibitRequestV1_1;
import org.eclipse.ecsp.domain.ro.Ro;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.exceptions.EmptyResponseException;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.platform.services.ro.constant.EventIdConstants;
import org.eclipse.ecsp.platform.services.ro.constant.ResponseMsgConstants;
import org.eclipse.ecsp.platform.services.ro.dao.RoDAOMongoImpl;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteInhibitRequest;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationResponse;
import org.eclipse.ecsp.utils.ApiUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Remote inhibit service class.
 *
 * @author pkumar16
 */
@Service
public class RemoteInhibitService {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(RemoteInhibitService.class);

    @Autowired
    RoDAOMongoImpl riDAOMongoImpl;

    @Autowired
    private KafkaService kafkaService;

    @Value("${ri.kafka.sink.topic}")
    private String riKafkaTopic;

    @Autowired
    private Utils utils;

    @Autowired
    private ApiUtils apiUtils;

    /**
     * This method is used to create RI Request by kafka event with eventId RIRequest.
     *
     * @param userId               the user id
     * @param vehicleId            the vehicle id
     * @param remoteInhibitRequest the remote inhibit request
     * @param sessionId            the session id
     * @param origin               the origin
     * @param ecuType              the ecu type
     * @param vehicleArchType      the vehicle arch type
     * @return {@link RemoteOperationResponse}
     * @throws ExecutionException the execution exception
     */
    // Service to handle Remote Inhibit Requests
    public RemoteOperationResponse createRIRequest(String userId,
                                                   String vehicleId,
                                                   RemoteInhibitRequest remoteInhibitRequest,
                                                   String sessionId,
                                                   String origin,
                                                   String ecuType,
                                                   String vehicleArchType)
            throws
            ExecutionException {

        LOGGER.info("Publishing remote inhibit command to Kafka for origin:{}"
                        + ", vehicleId:{}"
                        + ", userid :{}"
                        + ",RemoteInhibitRequest: {}"
                        + ", ecuType: {}"
                        + ", vehicleArchType: {}",
                Utils.logForging(origin),
                Utils.logForging(vehicleId),
                Utils.logForging(userId),
                Utils.logForging(remoteInhibitRequest),
                Utils.logForging(ecuType),
                Utils.logForging(vehicleArchType));

        RemoteInhibitRequestV1_1 roInhibitData = new RemoteInhibitRequestV1_1();

        if (null != remoteInhibitRequest.getCrankInhibit()) {
            roInhibitData.setCrankInhibit(RemoteInhibitRequestV1_1.CrankInhibit
                    .valueOf(remoteInhibitRequest.getCrankInhibit().name()));
        }

        roInhibitData.setOrigin(origin);
        roInhibitData.setUserId(userId);
        roInhibitData.setRoRequestId(remoteInhibitRequest.getRoRequestId());
        roInhibitData.setVehicleArchType(vehicleArchType);

        IgniteEvent igniteEventImpl = new IgniteEventImplBuilder()
                .withEventId(EventIdConstants.EVENT_ID_REMOTE_INHIBIT_REQUEST.getValue())
                .withVersion(Version.V1_1)
                .withVehicleId(vehicleId)
                .withEventData(roInhibitData)
                .withRequestId(remoteInhibitRequest.getRoRequestId())
                .withBizTransactionId(sessionId)
                .withUserContextInfo(apiUtils.getUserContext(userId))
                .withTimestamp(System.currentTimeMillis())
                .withEcuType(ecuType)
                .build();

        kafkaService.sendIgniteEventonTopic(igniteEventImpl, riKafkaTopic);

        return new RemoteOperationResponse(
                ResponseMsgConstants.RI_COMMAND_SUCCESS,
                remoteInhibitRequest.getRoRequestId());
    }

    /**
     * Gets ri response.
     *
     * @param userId      the user id
     * @param vehicleId   the vehicle id
     * @param roRequestId the ro request id
     * @param requestId   the request id
     * @param sessionId   the session id
     * @param origin      the origin
     * @return the ri response
     * @throws JsonProcessingException the json processing exception
     * @throws EmptyResponseException  the empty response exception
     */
    // Service to handle Remote Inhibit Responses
    public Ro getRIResponse(String userId,
                            String vehicleId,
                            String roRequestId,
                            String requestId,
                            String sessionId,
                            String origin)
            throws
            JsonProcessingException,
            EmptyResponseException {

        Optional<Ro> response = riDAOMongoImpl.getRIResponses(vehicleId, roRequestId);

        if (response.isPresent()) {
            return response.get();
        }

        throw new EmptyResponseException(
                "NO_REMOTE_INHIBIT_RESPONSE",
                "NO Remote Inhibit response found");
    }

    /**
     * Gets ri response partial.
     *
     * @param userId      the user id
     * @param vehicleId   the vehicle id
     * @param roRequestId the ro request id
     * @param requestId   the request id
     * @param sessionId   the session id
     * @param origin      the origin
     * @return the ri response partial
     * @throws JsonProcessingException the json processing exception
     * @throws EmptyResponseException  the empty response exception
     */
    public IgniteEvent getRIResponsePartial(String userId,
                                            String vehicleId,
                                            String roRequestId,
                                            String requestId,
                                            String sessionId,
                                            String origin)
            throws
            JsonProcessingException,
            EmptyResponseException {

        Optional<Ro> response = riDAOMongoImpl.getRIResponses(vehicleId, roRequestId);

        if ((response.isPresent())
                && (response.get().getRoResponseList() != null)
                && (!response.get().getRoResponseList().isEmpty())) {

            for (IgniteEvent event : response.get().getRoResponseList()) {
                if (event.getEventId().equals(Constants.EVENT_ID_REMOTE_INHIBIT_RESPONSE)) {
                    return event;
                }
            }

        }
        throw new EmptyResponseException(
                "NO_REMOTE_INHIBIT_RESPONSE",
                "NO Remote Inhibit response found");
    }
}
