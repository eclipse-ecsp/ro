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

package org.eclipse.ecsp.platform.services.ro.rest;

import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationAlarmReq;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationHornReq;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationLightsReq;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationResponse;
import org.eclipse.ecsp.platform.services.ro.service.ROHornLightsAlarmService;
import org.eclipse.ecsp.platform.services.ro.service.Utils;
import org.eclipse.ecsp.security.Security;
import org.eclipse.ecsp.services.constants.EventAttribute;
import org.eclipse.ecsp.utils.ApiUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.ExecutionException;

/**
 * Ro horn lights alarm controller class.
 *
 * @author midnani
 */
@RestController
public class ROHornLightsAlarmController {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ROHornLightsAlarmController.class);

    @Autowired
    private ROHornLightsAlarmService roHornAndLightsService;

    /**
     * Create remote horn request Api.
     *
     * @param clientRequestId   the client request id
     * @param requestId         the request id
     * @param sessionId         the session id
     * @param origin            the origin
     * @param ecuType           the ecu type
     * @param vehicleArchType   the vehicle arch type
     * @param userId            the user id
     * @param vehicleId         the vehicle id
     * @param remoteHornRequest the remote horn request
     * @return the response entity
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    @Operation(summary = "PUT /v1.1/users/{userId}/vehicles/{vehicleId}/ro/horn",
            description = "create Remote Horn Request",
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Accepted",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RemoteOperationResponse.class)))
            }
    )
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR,
            scopes = {"ROSupportOwner, ROSupportThirdParty, SelfManage, ROSupportLawEnforcement, RODelegator"})
    @Parameter(name = "ClientRequestId", description = "ClientRequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "RequestId", description = "RequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "SessionId", description = "SessionId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "OriginId", description = "OriginId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "ecuType", description = "ecuType",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "vehicleArchType", description = "vehicleArchType",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    @PutMapping(value = "/v1.1/users/{userId}/vehicles/{vehicleId}/ro/horn",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RemoteOperationResponse> createRemoteHornRequest(
            @RequestHeader(value = "ClientRequestId", required = false)
            String clientRequestId,
            @RequestHeader(value = "RequestId") String requestId,
            @RequestHeader(value = "SessionId") String sessionId,
            @RequestHeader(value = "OriginId") String origin,
            @RequestHeader(value = "ecuType", required = false) String ecuType,
            @RequestHeader(value = "vehicleArchType", required = false) String vehicleArchType,
            @PathVariable("userId") @Parameter(description = "userId") String userId,
            @PathVariable("vehicleId") @Parameter(description = "vehicleId") String vehicleId,
            @RequestBody RemoteOperationHornReq remoteHornRequest)
            throws InterruptedException, ExecutionException {
        if (!requestId.isEmpty()) {
            remoteHornRequest.setRoRequestId(requestId);
        }
        LOGGER.info("Received  remoteHorn :{}, userId :{} ,vehicleId :{} ,ecuType :{} "
                        + ",vehicleArchType :{}",
                Utils.logForging(remoteHornRequest), Utils.logForging(userId),
                Utils.logForging(vehicleId), Utils.logForging(ecuType),
                Utils.logForging(vehicleArchType));
        RemoteOperationResponse remoteOperationResponse = roHornAndLightsService
                .createRemoteHornRequest(userId, vehicleId,
                        remoteHornRequest, sessionId, origin, ecuType, vehicleArchType);
        return new ResponseEntity<RemoteOperationResponse>(remoteOperationResponse,
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.ACCEPTED);
    }

    /**
     * Create remote lights request Api.
     *
     * @param clientRequestId     the client request id
     * @param requestId           the request id
     * @param sessionId           the session id
     * @param origin              the origin
     * @param ecuType             the ecu type
     * @param vehicleArchType     the vehicle arch type
     * @param partnerId           the partner id
     * @param userId              the user id
     * @param vehicleId           the vehicle id
     * @param remoteLightsRequest the remote lights request
     * @return the response entity
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    @Operation(summary = "PUT /v1.1/users/{userId}/vehicles/{vehicleId}/ro/lights",
            description = "create Remote Lights Request",
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Accepted",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RemoteOperationResponse.class)))
            }
    )
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR,
            scopes = {"ROSupportOwner, ROSupportThirdParty, SelfManage, ROSupportLawEnforcement, RODelegator"})
    @Parameter(name = "ClientRequestId", description = "ClientRequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "RequestId", description = "RequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "SessionId", description = "SessionId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "OriginId", description = "OriginId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "ecuType", description = "ecuType",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "vehicleArchType", description = "vehicleArchType",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "PartnerId", description = "PartnerId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    @PutMapping(value = "/v1.1/users/{userId}/vehicles/{vehicleId}/ro/lights",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RemoteOperationResponse> createRemoteLightsRequest(
            @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
            @RequestHeader(value = "RequestId") String requestId,
            @RequestHeader(value = "SessionId") String sessionId,
            @RequestHeader(value = "OriginId") String origin,
            @RequestHeader(value = "ecuType", required = false) String ecuType,
            @RequestHeader(value = "vehicleArchType", required = false) String vehicleArchType,
            @RequestHeader(value = EventAttribute.PARTNER_ID, required = false) String partnerId,
            @PathVariable("userId") @Parameter(description = "userId") String userId,
            @PathVariable("vehicleId") @Parameter(description = "vehicleId") String vehicleId,
            @RequestBody RemoteOperationLightsReq remoteLightsRequest)
            throws InterruptedException, ExecutionException {
        if (!requestId.isEmpty()) {
            remoteLightsRequest.setRoRequestId(requestId);
        }
        LOGGER.info("Received  remoteLightsRequest :{},"
                        + " userId :{} ,vehicleId :{} ,partnerId :{} ,ecuType :{} ,vehicleArchType :{}",
                Utils.logForging(remoteLightsRequest), Utils.logForging(userId),
                Utils.logForging(vehicleId), Utils.logForging(partnerId),
                Utils.logForging(ecuType), Utils.logForging(vehicleArchType));
        RemoteOperationResponse remoteOperationResponse = roHornAndLightsService
                .createRemoteLightsRequest(userId, vehicleId,
                        remoteLightsRequest, sessionId, origin, partnerId, ecuType, vehicleArchType);
        return new ResponseEntity<RemoteOperationResponse>(remoteOperationResponse,
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.ACCEPTED);
    }

    /**
     * API To Create Remote Lights request only.
     *
     * @param clientRequestId     the client request id
     * @param requestId           the request id
     * @param sessionId           the session id
     * @param origin              the origin
     * @param ecuType             the ecu type
     * @param vehicleArchType     the vehicle arch type
     * @param partnerId           the partner id
     * @param userId              the user id
     * @param vehicleId           the vehicle id
     * @param remoteLightsRequest the remote lights request
     * @return the response entity
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    @Operation(summary = "PUT /v1.2/users/{userId}/vehicles/{vehicleId}/ro/lights",
            description = "create Remote Lights only",
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Accepted",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RemoteOperationResponse.class)))
            }
    )
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR,
            scopes = {"ROSupportOwner, ROSupportThirdParty, SelfManage, ROSupportLawEnforcement, RODelegator"})
    @Parameter(name = "ClientRequestId", description = "ClientRequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "RequestId", description = "RequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "SessionId", description = "SessionId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "OriginId", description = "OriginId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "ecuType", description = "ecuType",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "vehicleArchType", description = "vehicleArchType",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "PartnerId", description = "PartnerId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    @PutMapping(value = "/v1.2/users/{userId}/vehicles/{vehicleId}/ro/lights",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RemoteOperationResponse> createRemoteLightsonly(
            @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
            @RequestHeader(value = "RequestId") String requestId,
            @RequestHeader(value = "SessionId") String sessionId,
            @RequestHeader(value = "OriginId") String origin,
            @RequestHeader(value = "ecuType", required = false) String ecuType,
            @RequestHeader(value = "vehicleArchType", required = false) String vehicleArchType,
            @RequestHeader(value = EventAttribute.PARTNER_ID, required = false) String partnerId,
            @PathVariable("userId") @Parameter(description = "userId") String userId,
            @PathVariable("vehicleId") @Parameter(description = "vehicleId") String vehicleId,
            @RequestBody RemoteOperationLightsReq remoteLightsRequest)
            throws InterruptedException, ExecutionException {
        if (!requestId.isEmpty()) {
            remoteLightsRequest.setRoRequestId(requestId);
        }
        LOGGER.info("Received  remoteLightsRequest :{}, userId :{} ,vehicleId :{} ,"
                        + "partnerId :{} ,ecuType :{} ,vehicleArchType :{}",
                Utils.logForging(remoteLightsRequest), Utils.logForging(userId),
                Utils.logForging(vehicleId), Utils.logForging(partnerId),
                Utils.logForging(ecuType), Utils.logForging(vehicleArchType));
        RemoteOperationResponse remoteOperationResponse = roHornAndLightsService
                .createRemoteLightsOnly(userId, vehicleId,
                        remoteLightsRequest, sessionId, origin, partnerId, ecuType, vehicleArchType);
        return new ResponseEntity<RemoteOperationResponse>(remoteOperationResponse,
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.ACCEPTED);
    }

    /**
     * Create remote alarm request Api.
     *
     * @param clientRequestId       the client request id
     * @param requestId             the request id
     * @param sessionId             the session id
     * @param origin                the origin
     * @param ecuType               the ecu type
     * @param vehicleArchType       the vehicle arch type
     * @param partnerId             the partner id
     * @param userId                the user id
     * @param vehicleId             the vehicle id
     * @param remoteOperationAlarmReq the remote operation alarm request
     * @return the response entity
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    @Operation(summary = "PUT /v1.1/users/{userId}/vehicles/{vehicleId}/ro/alarm",
            description = "create Remote Alarm Request",
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Accepted",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RemoteOperationResponse.class)))
            }
    )
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR,
            scopes = {"ROSupportOwner, ROSupportThirdParty, SelfManage, ROSupportLawEnforcement, RODelegator"})
    @Parameter(name = "ClientRequestId", description = "ClientRequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "RequestId", description = "RequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "SessionId", description = "SessionId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "OriginId", description = "OriginId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "ecuType", description = "ecuType",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "vehicleArchType", description = "vehicleArchType",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "PartnerId", description = "PartnerId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    @PutMapping(value = "/v1.1/users/{userId}/vehicles/{vehicleId}/ro/alarm",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RemoteOperationResponse> createRemoteAlarmRequest(
            @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
            @RequestHeader(value = "RequestId") String requestId,
            @RequestHeader(value = "SessionId") String sessionId,
            @RequestHeader(value = "OriginId") String origin,
            @RequestHeader(value = "ecuType", required = false) String ecuType,
            @RequestHeader(value = "vehicleArchType", required = false) String vehicleArchType,
            @RequestHeader(value = EventAttribute.PARTNER_ID, required = false) String partnerId,
            @PathVariable("userId") @Parameter(description = "userId") String userId,
            @PathVariable("vehicleId") @Parameter(description = "vehicleId") String vehicleId,
            @RequestBody RemoteOperationAlarmReq remoteOperationAlarmReq)
            throws InterruptedException, ExecutionException {
        if (!requestId.isEmpty()) {
            remoteOperationAlarmReq.setRoRequestId(requestId);
        }
        LOGGER.info("Received  remoteHornAndLightsRequest :{} userId :{} ,vehicleId :{} "
                        + ",partnerId :{} ,ecuType :{} ,vehicleArchType :{}",
                Utils.logForging(remoteOperationAlarmReq), Utils.logForging(userId),
                Utils.logForging(vehicleId), Utils.logForging(partnerId),
                Utils.logForging(ecuType), Utils.logForging(vehicleArchType));
        RemoteOperationResponse remoteOperationResponse = roHornAndLightsService
                .createRemoteAlarmRequest(userId, vehicleId,
                        remoteOperationAlarmReq, sessionId, origin, partnerId, ecuType, vehicleArchType);
        return new ResponseEntity<RemoteOperationResponse>(remoteOperationResponse,
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.ACCEPTED);
    }
}