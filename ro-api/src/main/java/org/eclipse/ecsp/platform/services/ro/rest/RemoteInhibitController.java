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
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.eclipse.ecsp.domain.ro.Ro;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.exceptions.EmptyResponseException;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteInhibitRequest;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationResponse;
import org.eclipse.ecsp.platform.services.ro.service.RemoteInhibitService;
import org.eclipse.ecsp.platform.services.ro.service.Utils;
import org.eclipse.ecsp.security.Security;
import org.eclipse.ecsp.services.constants.EventAttribute;
import org.eclipse.ecsp.services.utils.JsonMapperUtils;
import org.eclipse.ecsp.utils.ApiUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.ExecutionException;

/**
 * Remote inhibit controller.
 *
 * @author pkumar16
 */
@RestController
public class RemoteInhibitController {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(RemoteInhibitController.class);

    @Autowired
    private RemoteInhibitService remoteInhibitService;

    /**
     * Create remote inhibit request.
     *
     * @param clientRequestId      the client request id
     * @param requestId            the request id
     * @param sessionId            the session id
     * @param origin               the origin
     * @param ecuType              the ecu type
     * @param vehicleArchType      the vehicle arch type
     * @param userId               the user id
     * @param vehicleId            the vehicle id
     * @param remoteInhibitRequest the remote inhibit request
     * @return the response entity
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    @Operation(summary = "POST /v1.1/users/{userId}/vehicles/{vehicleId}/ro/inhibit",
            description = "create RemoteInhibit Request",
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Accepted",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RemoteOperationResponse.class)))
            }
    )
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR,
            scopes = {"StolenRemoteInhibitSupportOwner, FleetRemoteInhibitOwner, RemoteInhibitB2BOwner"})
    @Parameter(name = "ClientRequestId", description = "ClientRequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "RequestId", description = "RequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "SessionId", description = "SessionId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "OriginId", description = "OriginId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "vehicleArchType", description = "vehicleArchType",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    @PostMapping(value = "/v1.1/users/{userId}/vehicles/{vehicleId}/ro/inhibit",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RemoteOperationResponse> createRemoteInhibitRequest(
            @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
            @RequestHeader(value = "RequestId") String requestId,
            @RequestHeader(value = "SessionId") String sessionId,
            @RequestHeader(value = "OriginId") String origin,
            @RequestHeader(value = "ecuType", required = false) String ecuType,
            @RequestHeader(value = "vehicleArchType", required = false) String vehicleArchType,
            @PathVariable("userId") @Parameter(description = "userId") String userId,
            @PathVariable("vehicleId") @Parameter(description = "vehicleId") String vehicleId,
            @RequestBody RemoteInhibitRequest remoteInhibitRequest)
            throws InterruptedException, ExecutionException {
        if (!requestId.isEmpty()) {
            remoteInhibitRequest.setRoRequestId(requestId);
        }
        LOGGER.info("Received  remoteInhibitRequest :{}, userId :{} ,vehicleId :{} ,ecuType: {} ,vehicleArchType :{}",
                Utils.logForging(remoteInhibitRequest), Utils.logForging(userId),
                Utils.logForging(vehicleId), Utils.logForging(ecuType),
                Utils.logForging(vehicleArchType));
        RemoteOperationResponse remoteInhibitResponse = remoteInhibitService
                .createRIRequest(userId, vehicleId,
                        remoteInhibitRequest, sessionId, origin, ecuType, vehicleArchType);
        return new ResponseEntity<RemoteOperationResponse>(remoteInhibitResponse,
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.ACCEPTED);
    }

    /**
     * Gets ri response.
     *
     * @param userId          the user id
     * @param vehicleId       the vehicle id
     * @param roRequestId     the ro request id
     * @param clientRequestId the client request id
     * @param requestId       the request id
     * @param sessionId       the session id
     * @param origin          the origin
     * @return the ri response
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws JsonProcessingException the json processing exception
     * @throws EmptyResponseException  the empty response exception
     */
    @Operation(summary = "GET /v2/users/{userId}/vehicles/{vehicleId}/remoteinhibit/requests/{roRequestId}",
            description = "get RI Response",
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = String.class)))
            }
    )
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR,
            scopes = {"StolenRemoteInhibitSupportOwner, FleetRemoteInhibitOwner, RemoteInhibitB2BOwner"})
    @Parameter(name = "ClientRequestId", description = "ClientRequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "RequestId", description = "RequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "SessionId", description = "SessionId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "OriginId", description = "OriginId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    @GetMapping(value = "/v2/users/{userId}/vehicles/{vehicleId}/remoteinhibit/requests/{roRequestId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getRIResponse(@PathVariable("userId") @Parameter(description = "userId")
                                                String userId,
                                                @PathVariable("vehicleId") @Parameter(description = "vehicleId")
                                                String vehicleId,
                                                @PathVariable("roRequestId") @Parameter(description = "roRequestId")
                                                    String roRequestId,
                                                @RequestHeader(value = "ClientRequestId", required = false)
                                                    String clientRequestId,
                                                @RequestHeader(value = "RequestId") String requestId,
                                                @RequestHeader(value = "SessionId") String sessionId,
                                                @RequestHeader(value = "OriginId") String origin)
            throws InterruptedException, ExecutionException, JsonProcessingException, EmptyResponseException {

        LOGGER.info("Received response for Remote Inhibit APIs OriginId :{} ,userId :{} "
                        + ",vehicleId :{} , roRequestId: {}  ",
                Utils.logForging(origin), Utils.logForging(userId),
                Utils.logForging(vehicleId), Utils.logForging(roRequestId));

        Ro riResponse = remoteInhibitService.getRIResponse(userId, vehicleId, roRequestId,
                requestId, sessionId, origin);

        return new ResponseEntity<String>(
                JsonMapperUtils.applyExcludefilterAndGetAsString(EventAttribute.REMOTE_INHIBIT, riResponse),
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.OK);
    }

    /**
     * Gets ri response partial.
     *
     * @param userId          the user id
     * @param vehicleId       the vehicle id
     * @param roRequestId     the ro request id
     * @param clientRequestId the client request id
     * @param requestId       the request id
     * @param sessionId       the session id
     * @param origin          the origin
     * @return the ri response partial
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws JsonProcessingException the json processing exception
     * @throws EmptyResponseException  the empty response exception
     */
    @Operation(summary = "GET /v1.1/users/{userId}/vehicles/{vehicleId}/remoteinhibit/requests/{roRequestId}",
            description = "get RI Response Partial",
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = String.class)))
            }
    )
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR,
            scopes = {"StolenRemoteInhibitSupportOwner, FleetRemoteInhibitOwner, RemoteInhibitB2BOwner"})
    @Parameter(name = "ClientRequestId", description = "ClientRequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "RequestId", description = "RequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "SessionId", description = "SessionId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "OriginId", description = "OriginId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    @GetMapping(value = "/v1.1/users/{userId}/vehicles/{vehicleId}/remoteinhibit/requests/{roRequestId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getRIResponsePartial(@PathVariable("userId") @Parameter(description = "userId")
                                                       String userId,
                                                       @PathVariable("vehicleId") @Parameter(description = "vehicleId")
                                                       String vehicleId,
                                                       @PathVariable("roRequestId")
                                                           @Parameter(description = "roRequestId")
                                                           String roRequestId,
                                                       @RequestHeader(value = "ClientRequestId", required = false)
                                                           String clientRequestId,
                                                       @RequestHeader(value = "RequestId") String requestId,
                                                       @RequestHeader(value = "SessionId") String sessionId,
                                                       @RequestHeader(value = "OriginId") String origin)
            throws InterruptedException, ExecutionException, JsonProcessingException, EmptyResponseException {

        LOGGER.info("Received response for Remote Inhibit APIs OriginId :{} ,userId :{} "
                        + ",vehicleId :{} , roRequestId: {}  ",
                Utils.logForging(origin), Utils.logForging(userId),
                Utils.logForging(vehicleId), Utils.logForging(roRequestId));

        IgniteEvent riResponse = remoteInhibitService.getRIResponsePartial(userId,
                vehicleId, roRequestId, requestId, sessionId, origin);
        return new ResponseEntity<String>(
                JsonMapperUtils.applyExcludefilterAndGetAsString(EventAttribute.REMOTE_INHIBIT, riResponse),
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.OK);
    }

}
