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
import jakarta.validation.Valid;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationHoodReq;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationLiftGateRequestV2_0;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationLiftgateRequest;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationResponse;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationTrunkRequest;
import org.eclipse.ecsp.platform.services.ro.service.ROHoodTrunkLiftgateService;
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
 * {@link ROHoodTrunkLiftgateController} Controller for Hood Trunk Lift Gate.
 *
 * @author midnani
 */
@RestController
public class ROHoodTrunkLiftgateController {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ROHoodTrunkLiftgateController.class);

    @Autowired
    private ROHoodTrunkLiftgateService roHoodTrunkLiftgateService;

    /**
     * Create remote hood request Api.
     *
     * @param clientRequestId        the client request id
     * @param requestId              the request id
     * @param sessionId              the session id
     * @param origin                 the origin
     * @param ecuType                the ecu type
     * @param vehicleArchType        the vehicle arch type
     * @param userId                 the user id
     * @param vehicleId              the vehicle id
     * @param remoteOperationHoodReq the remote hood request
     * @return the response entity
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    @Operation(summary = "PUT /v1.1/users/{userId}/vehicles/{vehicleId}/ro/hood",
            description = "create Remote Hood Request",
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
    @PutMapping(value = "/v1.1/users/{userId}/vehicles/{vehicleId}/ro/hood",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RemoteOperationResponse> createRemoteHoodRequest(
            @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
            @RequestHeader(value = "RequestId") String requestId,
            @RequestHeader(value = "SessionId") String sessionId,
            @RequestHeader(value = "OriginId") String origin,
            @RequestHeader(value = "ecuType", required = false) String ecuType,
            @RequestHeader(value = "vehicleArchType", required = false) String vehicleArchType,
            @PathVariable("userId") @Parameter(description = "userId") String userId,
            @PathVariable("vehicleId") @Parameter(description = "vehicleId") String vehicleId,
            @RequestBody RemoteOperationHoodReq remoteOperationHoodReq)
            throws InterruptedException, ExecutionException {
        if (!requestId.isEmpty()) {
            remoteOperationHoodReq.setRoRequestId(requestId);
        }
        LOGGER.info("Received  remoteOperationHoodReq :{}, userId :{} ,vehicleId :{} ,"
                        + "ecuType:{} ,vehicleArchType :{}",
                Utils.logForging(remoteOperationHoodReq), Utils.logForging(userId),
                Utils.logForging(vehicleId), Utils.logForging(ecuType),
                Utils.logForging(vehicleArchType));
        RemoteOperationResponse remoteOperationResponse = roHoodTrunkLiftgateService
                .createRemoteHoodRequest(userId, vehicleId,
                        remoteOperationHoodReq, sessionId, origin, ecuType, vehicleArchType);
        return new ResponseEntity<RemoteOperationResponse>(remoteOperationResponse,
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.ACCEPTED);
    }

    /**
     * Create remote trunk request Api.
     *
     * @param clientRequestId    the client request id
     * @param requestId          the request id
     * @param sessionId          the session id
     * @param origin             the origin
     * @param userId             the user id
     * @param vehicleId          the vehicle id
     * @param ecuType            the ecu type
     * @param vehicleArchType    the vehicle arch type
     * @param remoteTrunkRequest the remote trunk request
     * @return the response entity
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    @Operation(summary = "PUT /v1.1/users/{userId}/vehicles/{vehicleId}/ro/trunk",
            description = "create Remote Trunk Request",
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
    @PutMapping(value = "/v1.1/users/{userId}/vehicles/{vehicleId}/ro/trunk",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RemoteOperationResponse> createRemoteTrunkRequest(
            @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
            @RequestHeader(value = "RequestId") String requestId,
            @RequestHeader(value = "SessionId") String sessionId,
            @RequestHeader(value = "OriginId") String origin,
            @PathVariable("userId") @Parameter(description = "userId") String userId,
            @PathVariable("vehicleId") @Parameter(description = "vehicleId") String vehicleId,
            @RequestHeader(value = "ecuType", required = false) String ecuType,
            @RequestHeader(value = "vehicleArchType", required = false) String vehicleArchType,
            @RequestBody RemoteOperationTrunkRequest remoteTrunkRequest)
            throws InterruptedException, ExecutionException {
        if (!requestId.isEmpty()) {
            remoteTrunkRequest.setRoRequestId(requestId);
        }
        LOGGER.info("Received  remoteLiftgateRequest :{}, userId :{} ,vehicleId :{} ,"
                        + "ecuType:{} ,vehicleArchType :{}",
                Utils.logForging(remoteTrunkRequest), Utils.logForging(userId),
                Utils.logForging(vehicleId), Utils.logForging(ecuType),
                Utils.logForging(vehicleArchType));
        RemoteOperationResponse remoteOperationResponse = roHoodTrunkLiftgateService
                .createRemoteTrunkRequest(userId, vehicleId,
                        remoteTrunkRequest, sessionId, origin, ecuType, vehicleArchType);
        return new ResponseEntity<RemoteOperationResponse>(remoteOperationResponse,
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.ACCEPTED);
    }

    /**
     * API(v2)  to Create remote trunk request.
     *
     * @param clientRequestId    the client request id
     * @param requestId          the request id
     * @param sessionId          the session id
     * @param origin             the origin
     * @param userId             the user id
     * @param vehicleId          the vehicle id
     * @param ecuType            the ecu type
     * @param vehicleArchType    the vehicle arch type
     * @param partnerId          the partner id
     * @param remoteTrunkRequest the remote trunk request
     * @return the response entity
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    @Operation(summary = "PUT /v2/ro/trunk",
            description = "create Remote Trunk Request V2",
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
    @PutMapping(value = "/v2/ro/trunk", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RemoteOperationResponse> createRemoteTrunkRequestV2(
            @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
            @RequestHeader(value = "RequestId") String requestId,
            @RequestHeader(value = "SessionId") String sessionId,
            @RequestHeader(value = "OriginId") String origin,
            @RequestHeader(value = "userId") String userId,
            @RequestHeader(value = "vehicleId") String vehicleId,
            @RequestHeader(value = "ecuType", required = false) String ecuType,
            @RequestHeader(value = "vehicleArchType", required = false) String vehicleArchType,
            @RequestHeader(value = EventAttribute.PARTNER_ID, required = false) String partnerId,
            @RequestBody @Valid RemoteOperationTrunkRequest remoteTrunkRequest)
            throws InterruptedException, ExecutionException {
        if (!requestId.isEmpty()) {
            remoteTrunkRequest.setRoRequestId(requestId);
        }
        LOGGER.info("Received remoteTrunkRequest :{}, userId :{} ,vehicleId :{} ,"
                        + "partnerId :{} ,ecuType:{} ,vehicleArchType :{}",
                Utils.logForging(remoteTrunkRequest), Utils.logForging(userId),
                Utils.logForging(vehicleId), Utils.logForging(partnerId),
                Utils.logForging(ecuType), Utils.logForging(vehicleArchType));
        RemoteOperationResponse remoteOperationResponse = roHoodTrunkLiftgateService
                .createRemoteTrunkRequest_v2(userId, vehicleId,
                        remoteTrunkRequest, sessionId, origin, partnerId, ecuType, vehicleArchType);
        return new ResponseEntity<RemoteOperationResponse>(remoteOperationResponse,
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.ACCEPTED);
    }

    /**
     * Create remote liftgate request Api.
     *
     * @param clientRequestId       the client request id
     * @param requestId             the request id
     * @param sessionId             the session id
     * @param origin                the origin
     * @param userId                the user id
     * @param vehicleId             the vehicle id
     * @param ecuType               the ecu type
     * @param vehicleArchType       the vehicle arch type
     * @param remoteLiftgateRequest the remote liftgate request
     * @return the response entity
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    @Operation(summary = "PUT /v1.1/users/{userId}/vehicles/{vehicleId}/ro/liftgate",
            description = "create Remote Lift Gate Request",
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
    @PutMapping(value = "/v1.1/users/{userId}/vehicles/{vehicleId}/ro/liftgate",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RemoteOperationResponse> createRemoteLiftgateRequest(
            @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
            @RequestHeader(value = "RequestId") String requestId,
            @RequestHeader(value = "SessionId") String sessionId,
            @RequestHeader(value = "OriginId") String origin,
            @PathVariable("userId") @Parameter(description = "userId") String userId,
            @PathVariable("vehicleId") @Parameter(description = "vehicleId") String vehicleId,
            @RequestHeader(value = "ecuType", required = false) String ecuType,
            @RequestHeader(value = "vehicleArchType", required = false) String vehicleArchType,
            @RequestBody RemoteOperationLiftgateRequest remoteLiftgateRequest)
            throws InterruptedException, ExecutionException {
        if (!requestId.isEmpty()) {
            remoteLiftgateRequest.setRoRequestId(requestId);
        }
        LOGGER.info("Received  remoteLiftgateRequest :{}, userId :{} ,vehicleId :{}, ecuType :{} ,vehicleArchType: {}",
                Utils.logForging(remoteLiftgateRequest), Utils.logForging(userId),
                Utils.logForging(vehicleId), Utils.logForging(ecuType),
                Utils.logForging(vehicleArchType));
        RemoteOperationResponse remoteOperationResponse = roHoodTrunkLiftgateService
                .createRemoteLiftgateRequest(userId, vehicleId,
                        remoteLiftgateRequest, sessionId, origin, ecuType, vehicleArchType);
        return new ResponseEntity<RemoteOperationResponse>(remoteOperationResponse,
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.ACCEPTED);
    }

    /**
     * API(v2) to Create remote lift gate request.
     *
     * @param clientRequestId       the client request id
     * @param requestId             the request id
     * @param sessionId             the session id
     * @param origin                the origin
     * @param userId                the user id
     * @param vehicleId             the vehicle id
     * @param partnerId             the partner id
     * @param ecuType               the ecu type
     * @param vehicleArchType       the vehicle arch type
     * @param remoteLiftgateRequest the remote liftgate request
     * @return the response entity
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    @Operation(summary = "PUT /v2/ro/liftgate",
            description = "create Remote Lift Gate Request V2",
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
    @Parameter(name = "userId", description = "userId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "vehicleId", description = "vehicleId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    @PutMapping(value = "/v2/ro/liftgate", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RemoteOperationResponse> createRemoteLiftgateRequestV2(
            @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
            @RequestHeader(value = "RequestId") String requestId,
            @RequestHeader(value = "SessionId") String sessionId,
            @RequestHeader(value = "OriginId") String origin,
            @RequestHeader(value = "userId") String userId,
            @RequestHeader(value = "vehicleId") String vehicleId,
            @RequestHeader(value = EventAttribute.PARTNER_ID, required = false) String partnerId,
            @RequestHeader(value = "ecuType", required = false) String ecuType,
            @RequestHeader(value = "vehicleArchType", required = false) String vehicleArchType,
            @RequestBody RemoteOperationLiftGateRequestV2_0 remoteLiftgateRequest)
            throws InterruptedException, ExecutionException {
        if (!requestId.isEmpty()) {
            remoteLiftgateRequest.setRoRequestId(requestId);
        }
        LOGGER.info("Received  remoteLiftgateRequest :{}, userId :{} ,vehicleId :{} "
                        + ",partnerId :{} ,ecuType :{} ,vehicleArchType: {}",
                Utils.logForging(remoteLiftgateRequest), Utils.logForging(userId),
                Utils.logForging(vehicleId), Utils.logForging(partnerId),
                Utils.logForging(ecuType), Utils.logForging(vehicleArchType));
        RemoteOperationResponse remoteOperationResponse = roHoodTrunkLiftgateService
                .createRemoteLiftgateRequest_v2(userId, vehicleId,
                        remoteLiftgateRequest, sessionId, origin, partnerId, ecuType, vehicleArchType);
        return new ResponseEntity<RemoteOperationResponse>(remoteOperationResponse,
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.ACCEPTED);
    }
}
