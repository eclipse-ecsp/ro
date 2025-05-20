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
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationGloveBoxReq;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationResponse;
import org.eclipse.ecsp.platform.services.ro.service.ROGloveBoxService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.ExecutionException;

/**
 * Remote operation controller for GloveBox lock/unlock.
 *
 * @author Arnold
 */
@RestController
public class ROGloveBoxController {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ROGloveBoxController.class);

    @Autowired
    private ROGloveBoxService roGloveBoxServices;

    /**
     * Create remote glove box request Api.
     *
     * @param clientRequestId            the client request id
     * @param requestId                  the request id
     * @param sessionId                  the session id
     * @param origin                     the origin
     * @param userId                     the user id
     * @param vehicleId                  the vehicle id
     * @param partnerId                  the partner id
     * @param ecuType                    the ecu type
     * @param vehicleArchType            the vehicle arch type
     * @param remoteOperationGloveBoxReq the remote glove box request
     * @return the response entity
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    @Operation(summary = "PUT /v2/ro/glovebox",
            description = "create Remote GloveBox Request",
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Accepted",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RemoteOperationResponse.class)))
            }
    )
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR,
            scopes = {"ROSupportOwner, ROSupportThirdParty, SelfManage, ROSupportLawEnforcement, EOLUser, "
                    + "ROGloveBoxB2BOwner, RODelegator"})
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
    @PutMapping(value = "/v2/ro/glovebox", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RemoteOperationResponse> createRemoteGloveBoxRequest(
            @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
            @RequestHeader(value = "RequestId") String requestId,
            @RequestHeader(value = "SessionId") String sessionId,
            @RequestHeader(value = "OriginId") String origin,
            @RequestHeader(value = "userId") String userId,
            @RequestHeader(value = "vehicleId") String vehicleId,
            @RequestHeader(value = EventAttribute.PARTNER_ID, required = false) String partnerId,
            @RequestHeader(value = "ecuType", required = false) String ecuType,
            @RequestHeader(value = "vehicleArchType", required = false) String vehicleArchType,
            @RequestBody @Valid RemoteOperationGloveBoxReq remoteOperationGloveBoxReq)
            throws InterruptedException, ExecutionException {
        if (!requestId.isEmpty()) {
            remoteOperationGloveBoxReq.setRoRequestId(requestId);
        }
        LOGGER.info("Received  remoteOperationGloveBoxReq :{}, "
                        + "userId :{} ,vehicleId :{} ,partnerId :{} ,ecuType :{} ,vehicleArchType :{}",
                Utils.logForging(remoteOperationGloveBoxReq), Utils.logForging(userId),
                Utils.logForging(vehicleId), Utils.logForging(partnerId),
                Utils.logForging(ecuType), Utils.logForging(vehicleArchType));
        RemoteOperationResponse remoteGloveBoxResponse = roGloveBoxServices.createRemoteGloveBoxRequest(
                userId, vehicleId, remoteOperationGloveBoxReq,
                sessionId, origin, partnerId, ecuType, vehicleArchType);
        return new ResponseEntity<RemoteOperationResponse>(remoteGloveBoxResponse,
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.ACCEPTED);

    }
}
