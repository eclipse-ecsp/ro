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
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationDoorsReq;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationDriverDoorReq;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationResponse;
import org.eclipse.ecsp.platform.services.ro.service.RODoorsService;
import org.eclipse.ecsp.platform.services.ro.service.Utils;
import org.eclipse.ecsp.security.Security;
import org.eclipse.ecsp.services.constants.EventAttribute;
import org.eclipse.ecsp.threadlocal.PlatformThreadLocal;
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
 * Controller Class RO Doors Operation.
 *
 * @author midnani
 */
@RestController
public class RODoorsController {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(RODoorsController.class);

    @Autowired
    private RODoorsService roDoorsService;

    /**
     * Create remote door request Api.
     *
     * @param clientRequestId    the client request id
     * @param requestId          the request id
     * @param sessionId          the session id
     * @param origin             the origin
     * @param partnerId          the partner id
     * @param ecuType            the ecu type
     * @param vehicleArchType    the vehicle arch type
     * @param userId             the user id
     * @param vehicleId          the vehicle id
     * @param remoteDoorsRequest the remote doors request
     * @return the response entity
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    @Operation(summary = "PUT /v1.1/users/{userId}/vehicles/{vehicleId}/ro/doors",
            description = "create Remote Door Request",
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Accepted",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RemoteOperationResponse.class)))
            }
    )
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR,
            scopes = {"ROSupportOwner, ROSupportThirdParty, SelfManage, ROSupportLawEnforcement,"
                    + " EOLUser, RODoorsB2BOwner, RODelegator"})
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
    @PutMapping(value = "/v1.1/users/{userId}/vehicles/{vehicleId}/ro/doors",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RemoteOperationResponse> createRemoteDoorRequest(
            @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
            @RequestHeader(value = "RequestId") String requestId,
            @RequestHeader(value = "SessionId") String sessionId,
            @RequestHeader(value = "OriginId") String origin,
            @RequestHeader(value = EventAttribute.PARTNER_ID, required = false) String partnerId,
            @RequestHeader(value = "ecuType", required = false) String ecuType,
            @RequestHeader(value = "vehicleArchType", required = false) String vehicleArchType,
            @PathVariable("userId") @Parameter(description = "userId") String userId,
            @PathVariable("vehicleId") @Parameter(description = "vehicleId") String vehicleId,
            @RequestBody RemoteOperationDoorsReq remoteDoorsRequest)
            throws InterruptedException, ExecutionException {
        if (!requestId.isEmpty()) {
            remoteDoorsRequest.setRoRequestId(requestId);
        }
        LOGGER.info("Received  remoteDoorsRequest :{},"
                        + " userId :{} ,vehicleId :{} ,partnerId :{} ,ecuType :{} ,"
                        + "vehicleArchType :{}",
                Utils.logForging(remoteDoorsRequest), Utils.logForging(userId),
                Utils.logForging(vehicleId), Utils.logForging(partnerId),
                Utils.logForging(ecuType), Utils.logForging(vehicleArchType));
        LOGGER.info("PlatformId value :{}", PlatformThreadLocal.getPlatformId());
        RemoteOperationResponse remoteDoorsResponse = roDoorsService
                .createRemoteDoorsRequest(userId, vehicleId, remoteDoorsRequest,
                        sessionId, origin, partnerId, ecuType, vehicleArchType);
        return new ResponseEntity<RemoteOperationResponse>(remoteDoorsResponse,
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.ACCEPTED);

    }

    /**
     * Create remote driver door request Api.
     *
     * @param clientRequestId         the client request id
     * @param requestId               the request id
     * @param sessionId               the session id
     * @param origin                  the origin
     * @param partnerId               the partner id
     * @param ecuType                 the ecu type
     * @param vehicleArchType         the vehicle arch type
     * @param userId                  the user id
     * @param vehicleId               the vehicle id
     * @param remoteDriverDoorRequest the remote driver door request
     * @return the response entity
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    @Operation(summary = "PUT /v1.1/users/{userId}/vehicles/{vehicleId}/ro/doors/driver",
            description = "create Remote Driver Door Request",
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Accepted",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RemoteOperationResponse.class)))
            }
    )
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR,
            scopes = {"ROSupportOwner, ROSupportThirdParty, SelfManage, ROSupportLawEnforcement,"
                    + " EOLUser, RODoorsB2BOwner, RODelegator"})
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
    @PutMapping(value = "/v1.1/users/{userId}/vehicles/{vehicleId}/ro/doors/driver",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RemoteOperationResponse> createRemoteDriverDoorRequest(
            @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
            @RequestHeader(value = "RequestId") String requestId,
            @RequestHeader(value = "SessionId") String sessionId,
            @RequestHeader(value = "OriginId") String origin,
            @RequestHeader(value = EventAttribute.PARTNER_ID, required = false) String partnerId,
            @RequestHeader(value = "ecuType", required = false) String ecuType,
            @RequestHeader(value = "vehicleArchType", required = false) String vehicleArchType,
            @PathVariable("userId") @Parameter(description = "userId") String userId,
            @PathVariable("vehicleId") @Parameter(description = "vehicleId") String vehicleId,
            @RequestBody RemoteOperationDriverDoorReq remoteDriverDoorRequest)
            throws InterruptedException, ExecutionException {
        if (!requestId.isEmpty()) {
            remoteDriverDoorRequest.setRoRequestId(requestId);
        }
        LOGGER.info("Received  remoteDriverDoorsRequest :{}, userId :{} ,vehicleId :{} "
                        + ",partnerId :{} ,ecuType :{}, vehicleArchType :{}",
                Utils.logForging(remoteDriverDoorRequest), Utils.logForging(userId),
                Utils.logForging(vehicleId), Utils.logForging(partnerId),
                Utils.logForging(ecuType), Utils.logForging(vehicleArchType));
        RemoteOperationResponse remoteOperationResponse = roDoorsService
                .createRemoteDriverDoorRequest(userId, vehicleId,
                        remoteDriverDoorRequest, sessionId, origin, partnerId, ecuType, vehicleArchType);
        return new ResponseEntity<RemoteOperationResponse>(remoteOperationResponse,
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.ACCEPTED);
    }
}
