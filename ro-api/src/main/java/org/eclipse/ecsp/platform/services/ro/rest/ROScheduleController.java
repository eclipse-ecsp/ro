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
import org.eclipse.ecsp.domain.ro.RemoteOperationType;
import org.eclipse.ecsp.domain.ro.ScheduleDto;
import org.eclipse.ecsp.exceptions.BadRequestException;
import org.eclipse.ecsp.exceptions.EmptyResponseException;
import org.eclipse.ecsp.platform.services.ro.domain.ROScheduleDeleteRequest;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationResponse;
import org.eclipse.ecsp.platform.services.ro.service.ROScheduleService;
import org.eclipse.ecsp.platform.services.ro.service.Utils;
import org.eclipse.ecsp.security.Security;
import org.eclipse.ecsp.utils.ApiUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Controller for RO Schedule Apis.
 */
@RestController
public class ROScheduleController {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ROScheduleController.class);

    @Autowired
    private ROScheduleService roScheduleService;

    /**
     * Gets ro schedules.
     *
     * @param clientRequestId the client request id
     * @param requestId       the request id
     * @param sessionId       the session id
     * @param userId          the user id
     * @param vehicleId       the vehicle id
     * @param roType          the ro type
     * @return the ro schedules
     * @throws EmptyResponseException the empty response exception
     */
    @Operation(summary = "GET /v1/users/{userId}/vehicles/{vehicleId}/ro/{roType}/schedules",
            description = "get RO Schedules",
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ScheduleDto.class)))
            }
    )
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR,
            scopes = {"ROSupportOwner, ROSupportThirdParty, SelfManage, ROSupportLawEnforcement, ROEngineB2BOwner"})
    @Parameter(name = "ClientRequestId", description = "ClientRequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "RequestId", description = "RequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "SessionId", description = "SessionId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    @GetMapping(
            value = "/v1/users/{userId}/vehicles/{vehicleId}/ro/{roType}/schedules",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ScheduleDto>> getROSchedules(
            @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
            @RequestHeader(value = "RequestId") String requestId,
            @RequestHeader(value = "SessionId") String sessionId,
            @PathVariable("userId") @Parameter(description = "userId") String userId,
            @PathVariable("vehicleId") @Parameter(description = "vehicleId") String vehicleId,
            @PathVariable("roType") @Parameter(description = "roType") RemoteOperationType roType)
            throws EmptyResponseException {

        LOGGER.info("Invoking get schedules for ro:{}, userId :{}, vehicleId :{}  ",
                Utils.logForging(roType),
                Utils.logForging(userId),
                Utils.logForging(vehicleId));

        List<ScheduleDto> remoteOperationResponse = roScheduleService.getROSchedules(vehicleId, roType);

        return new ResponseEntity<List<ScheduleDto>>(
                remoteOperationResponse,
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.OK);
    }

    /**
     * Delete ro schedules.
     *
     * @param clientRequestId the client request id
     * @param requestId       the request id
     * @param sessionId       the session id
     * @param userId          the user id
     * @param vehicleId       the vehicle id
     * @param roType          the ro type
     * @param deleteRequest   the delete request
     * @return the response entity
     * @throws ExecutionException  the execution exception
     * @throws BadRequestException the bad request exception
     */
    @Operation(summary = "DELETE /v1/users/{userId}/vehicles/{vehicleId}/ro/{roType}/schedules",
            description = "delete RO Schedules",
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RemoteOperationResponse.class)))
            }
    )
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR,
            scopes = {"ROSupportOwner, ROSupportThirdParty, SelfManage, ROSupportLawEnforcement, ROEngineB2BOwner"})
    @Parameter(name = "ClientRequestId", description = "ClientRequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "RequestId", description = "RequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "SessionId", description = "SessionId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    @DeleteMapping(
            value = "/v1/users/{userId}/vehicles/{vehicleId}/ro/{roType}/schedules",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RemoteOperationResponse> deleteROSchedules(
            @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
            @RequestHeader(value = "RequestId") String requestId,
            @RequestHeader(value = "SessionId") String sessionId,
            @PathVariable("userId") @Parameter(description = "userId") String userId,
            @PathVariable("vehicleId") @Parameter(description = "vehicleId") String vehicleId,
            @PathVariable("roType") @Parameter(description = "roType") RemoteOperationType roType,
            @Valid @RequestBody ROScheduleDeleteRequest deleteRequest)
            throws ExecutionException, BadRequestException {

        LOGGER.info("Invoking delete schedules for ro:{}, userId :{}, vehicleId :{}, requestBody :{}  ",
                Utils.logForging(roType),
                Utils.logForging(userId),
                Utils.logForging(vehicleId),
                Utils.logForging(deleteRequest));

        RemoteOperationResponse remoteOperationResponse = roScheduleService.deleteROSchedules(
                vehicleId,
                roType,
                userId,
                sessionId,
                requestId,
                deleteRequest.getSchedulerKey());

        return new ResponseEntity<RemoteOperationResponse>(
                remoteOperationResponse,
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.OK);
    }
}
