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
import jakarta.validation.Valid;
import org.eclipse.ecsp.domain.ro.Ro;
import org.eclipse.ecsp.exceptions.EmptyResponseException;
import org.eclipse.ecsp.exceptions.ForbiddenException;
import org.eclipse.ecsp.platform.services.ro.constant.EventIdConstants;
import org.eclipse.ecsp.platform.services.ro.service.ROEventFilter;
import org.eclipse.ecsp.platform.services.ro.service.ROStatusHistoryService;
import org.eclipse.ecsp.platform.services.ro.service.Utils;
import org.eclipse.ecsp.security.Security;
import org.eclipse.ecsp.services.constants.EventAttribute;
import org.eclipse.ecsp.utils.ApiUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Controller Class for Ro Status history related Apis.
 *
 * @author midnani
 */
@RestController
public class ROStatusHistoryController {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ROStatusHistoryController.class);

    @Autowired
    private ROEventFilter roEventFilter;

    @Autowired
    private ROStatusHistoryService rOStatusHistoryCompatibleService;

    /**
     * Last Remote Operation Status.
     *
     * @param userId      userId
     * @param vehicleId   vehicleId
     * @param roRequestId roRequestId
     * @return ResponseEntity
     * @throws EmptyResponseException  empty response case
     * @throws JsonProcessingException error in processing request/response
     */
    @Operation(summary = "GET /v1.1/users/{userId}/vehicles/{vehicleId}/ro/requests/{roRequestId}",
            description = "get Remote Op Status",
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Ro.class)))
            }
    )
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR,
            scopes = {"ROSupportOwner, ROSupportThirdParty, SelfManage, ROSupportLawEnforcement, RODelegator"})
    @Parameter(name = "RequestId", description = "RequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "SessionId", description = "SessionId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "PartnerId", description = "PartnerId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "OriginId", description = "OriginId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    @GetMapping(value = "/v1.1/users/{userId}/vehicles/{vehicleId}/ro/requests/{roRequestId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getRemoteOpStatus(@RequestHeader(value = "ClientRequestId",
                                                            required = false) String clientRequestId,
                                                    @RequestHeader(value = "SessionId") String sessionId,
                                                    @RequestHeader(value = "RequestId") String requestId,
                                                    @RequestHeader(value = "OriginId") String origin,
                                                    @RequestHeader(value = EventAttribute.PARTNER_ID, required = false)
                                                        String partnerId,
                                                    @PathVariable("userId") @Parameter(description = "userId")
                                                        String userId,
                                                    @PathVariable("vehicleId") @Parameter(description = "vehicleId")
                                                        String vehicleId,
                                                    @PathVariable("roRequestId") @Parameter(description = "roRequestId")
                                                        String roRequestId,
                                                    @RequestParam(value = "responsesLimit", required = false,
                                                        defaultValue = "${ro.status.limit}")
                                                    @Parameter(description = "responsesLimit") Integer responseLimit)
            throws EmptyResponseException, JsonProcessingException, ForbiddenException {
        LOGGER.info("Received getRemoteOpStatus for OriginId :{} ,userId :{} ,vehicleId :{} ,"
                        + " roRequestId: {} ,partnerId :{}", Utils.logForging(origin),
                Utils.logForging(userId),
                Utils.logForging(vehicleId),
                Utils.logForging(roRequestId), Utils.logForging(partnerId));
        Ro remoteOpStatus = rOStatusHistoryCompatibleService.getRemoteOpStatus(userId,
                vehicleId, roRequestId, responseLimit);
        String body = roEventFilter.filter(remoteOpStatus);
        return new ResponseEntity<String>(
                body,
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.OK);
    }

    /**
     * Gets remote op status v2.
     *
     * @param clientRequestId the client request id
     * @param sessionId       the session id
     * @param requestId       the request id
     * @param origin          the origin
     * @param partnerId       the partner id
     * @param userId          the user id
     * @param vehicleId       the vehicle id
     * @param roRequestId     the ro request id
     * @param responseLimit   the response limit
     * @return the remote op status v 2
     * @throws EmptyResponseException  the empty response exception
     * @throws JsonProcessingException the json processing exception
     */
    @Operation(summary = "GET /v2/ro/requests",
            description = "get Remote Op Status V2",
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Ro.class)))
            }
    )
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR,
            scopes = {"ROSupportOwner, ROSupportThirdParty, SelfManage, ROSupportLawEnforcement, RODelegator"})
    @Parameter(name = "RequestId", description = "RequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "SessionId", description = "SessionId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "PartnerId", description = "PartnerId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "OriginId", description = "OriginId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "userId", description = "userId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "vehicleId", description = "vehicleId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "roRequestId", description = "roRequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    @GetMapping(value = "/v2/ro/requests", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getRemoteOpStatusV2(@RequestHeader
                                                              (value = "ClientRequestId",
                                                              required = false) String clientRequestId,
                                                      @RequestHeader(value = "SessionId") String sessionId,
                                                      @RequestHeader(value = "RequestId") String requestId,
                                                      @RequestHeader(value = "OriginId") String origin,
                                                      @RequestHeader(value = EventAttribute.PARTNER_ID,
                                                          required = false) String partnerId,
                                                      @RequestHeader("userId") String userId,
                                                      @RequestHeader("vehicleId") String vehicleId,
                                                      @RequestHeader("roRequestId") String roRequestId,
                                                      @RequestParam(value = "responsesLimit", required = false,
                                                          defaultValue = "${ro.status.limit}")
                                                      @Parameter(description = "responsesLimit") Integer responseLimit)
            throws EmptyResponseException, JsonProcessingException {
        LOGGER.info("Received getRemoteOpStatus v2 for OriginId :{} ,userId :{} ,vehicleId"
                        + " :{} , roRequestId: {} ,partnerId :{}", Utils.logForging(origin),
                Utils.logForging(userId), Utils.logForging(vehicleId),
                Utils.logForging(roRequestId), Utils.logForging(partnerId));
        List<Ro> remoteOpStatus = rOStatusHistoryCompatibleService
                .getRemoteOpStatusV2(userId, vehicleId, roRequestId, responseLimit);
        String body = roEventFilter.filter(remoteOpStatus);
        return new ResponseEntity<String>(
                body,
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.OK);
    }

    /**
     * Remote Operations History.
     *
     * @param userId    userId
     * @param vehicleId vehicleId
     * @return ResponseEntity
     * @throws InterruptedException    InterruptedException
     * @throws ExecutionException      ExecutionException
     * @throws EmptyResponseException  EmptyResponseException
     * @throws JsonProcessingException JsonProcessingException
     */
    @Operation(summary = "GET /v1.1/users/{userId}/vehicles/{vehicleId}/ro/history",
            description = "get Remote Op History",
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Ro.class)))
            }
    )
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR,
            scopes = {"ROSupportOwner, ROSupportThirdParty, SelfManage, ROSupportLawEnforcement, RODelegator"})
    @Parameter(name = "RequestId", description = "RequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "SessionId", description = "SessionId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "PartnerId", description = "PartnerId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "OriginId", description = "OriginId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    @GetMapping(value = "/v1.1/users/{userId}/vehicles/{vehicleId}/ro/history",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Valid
    public ResponseEntity<String> getRemoteOpHistory(@RequestHeader(value = "ClientRequestId",
                                                             required = false) String clientRequestId,
                                                     @RequestHeader(value = "SessionId") String sessionId,
                                                     @RequestHeader(value = "RequestId") String requestId,
                                                     @RequestHeader(value = "OriginId") String origin,
                                                     @RequestHeader(value = EventAttribute.PARTNER_ID, required = false)
                                                         String partnerId,
                                                     @PathVariable("userId") @Parameter(description = "userId")
                                                         String userId,
                                                     @PathVariable("vehicleId") @Parameter(description = "vehicleId")
                                                         String vehicleId,
                                                     @RequestParam(value = "responsesLimit", required = false,
                                                         defaultValue = "${ro.history.limit}")
                                                     @Parameter(description = "responsesLimit") Integer responseLimit)
            throws InterruptedException, ExecutionException, EmptyResponseException, JsonProcessingException {
        LOGGER.info("Received getRemoteOpHistory for OriginId :{} ,userId :{} ,vehicleId "
                        + ":{} ,partnerId :{}", Utils.logForging(origin), Utils.logForging(userId),
                Utils.logForging(vehicleId), Utils.logForging(partnerId));
        List<Ro> remoteOpHistory = rOStatusHistoryCompatibleService
                .getRemoteOpHistory(userId, vehicleId, responseLimit);
        String body = roEventFilter.filter(remoteOpHistory);
        return new ResponseEntity<String>(
                body,
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.OK);
    }

    /**
     * Fetch ro history for portal.
     *
     * @param clientRequestId the client request id
     * @param requestId       the request id
     * @param sessionId       the session id
     * @param origin          the origin
     * @param userId          the user id
     * @param vehicleId       the vehicle id
     * @param partnerId       the partner id
     * @param eventId         the event id
     * @param since           the since
     * @param until           the until
     * @param lastRecordId    the last record id
     * @param sortOrder       the sort order
     * @param responseCount   the response count
     * @return {@link ResponseEntity}
     * @throws JsonProcessingException the json processing exception
     * @throws EmptyResponseException  the empty response exception
     */
    @Operation(summary = "GET /v2/ro/history",
            description = "fetch Ro History For Portal",
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = String.class)))
            }
    )
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR,
            scopes = {"ROSupportOwner, ROSupportThirdParty, SelfManage, ROSupportLawEnforcement, "
                    + "ROHistoryOwner, ROB2BOwner"})
    @Parameter(name = "ClientRequestId", description = "ClientRequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "RequestId", description = "RequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "SessionId", description = "SessionId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "PartnerId", description = "PartnerId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "OriginId", description = "OriginId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "userId", description = "userId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "vehicleId", description = "vehicleId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    @GetMapping(value = "/v2/ro/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> fetchRoHistoryForPortal(
            @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
            @RequestHeader(value = "RequestId") String requestId,
            @RequestHeader(value = "SessionId") String sessionId,
            @RequestHeader(value = "OriginId") String origin,
            @RequestHeader(value = "userId") String userId,
            @RequestHeader(value = "vehicleId") String vehicleId,
            @RequestHeader(value = EventAttribute.PARTNER_ID, required = false) String partnerId,
            @RequestParam(value = "eventId", required = false) @Parameter(description = "eventId")
            String eventId,
            @RequestParam(value = "since", required = false) @Parameter(description = "since") Long since,
            @RequestParam(value = "until", required = false) @Parameter(description = "until") Long until,
            @RequestParam(value = "lastRecordId", required = false) @Parameter(description = "lastRecordId")
            String lastRecordId,
            @RequestParam(value = "sortOrder", defaultValue = "asc", required = false) @Parameter(description =
                    "sortOrder") String sortOrder,
            @RequestParam(value = "responseCount", required = false) @Parameter(description = "responseCount")
            Integer responseCount)
            throws JsonProcessingException, EmptyResponseException, JSONException {

        LOGGER.info(
                "Received GET adminRoHistory for userId :{} ,vehicleId :{}, eventId: {}, "
                        + "since: {}, until: {}, responseCount: {}, sortOrder: {}, "
                        + "lastRecordId: {}, partnerId: {}", Utils.logForging(userId),
                Utils.logForging(vehicleId), Utils.logForging(eventId),
                Utils.logForging(since), Utils.logForging(until),
                Utils.logForging(responseCount), Utils.logForging(sortOrder),
                Utils.logForging(lastRecordId), Utils.logForging(partnerId));

        Map<Long, List<Ro>> list = rOStatusHistoryCompatibleService
                .fetchRoHistoryForPortal(vehicleId, since, until, responseCount,
                        lastRecordId, sortOrder, eventId);

        String alertsJsonArray = roEventFilter.filter(list.values().iterator().next());

        JSONObject alertsJsonObject = new JSONObject();
        alertsJsonObject.put(EventIdConstants.TOTAL_RECORDS.getValue(), list.keySet().iterator().next());
        alertsJsonObject.put(EventIdConstants.ALERTS.getValue(), new JSONArray(alertsJsonArray));

        return new ResponseEntity<>(alertsJsonObject.toString(),
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.OK);

    }
}
