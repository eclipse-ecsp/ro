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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.eclipse.ecsp.domain.ro.RCPD;
import org.eclipse.ecsp.domain.ro.RCPDRequestV1_0;
import org.eclipse.ecsp.exceptions.EmptyResponseException;
import org.eclipse.ecsp.exceptions.TooManyRequestException;
import org.eclipse.ecsp.platform.services.ro.constant.EventIdConstants;
import org.eclipse.ecsp.platform.services.ro.constant.ResponseMsgConstants;
import org.eclipse.ecsp.platform.services.ro.domain.RCPDResponse;
import org.eclipse.ecsp.platform.services.ro.service.RCPDService;
import org.eclipse.ecsp.platform.services.ro.service.Utils;
import org.eclipse.ecsp.security.Security;
import org.eclipse.ecsp.services.constants.EventAttribute;
import org.eclipse.ecsp.services.utils.JsonMapperUtils;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Controller Class for RCPD Api.
 */
@RestController
public class RCPDController {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(RCPDController.class);

    private static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private RCPDService rcpdService;

    static {
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        filterProvider.addFilter(EventAttribute.EVENT_FILTER,
                SimpleBeanPropertyFilter.serializeAllExcept(EventAttribute.ID,
                        EventAttribute.TIMEZONE,
                        EventAttribute.SCHEMA_VERSION, EventAttribute.SOURCE_DEVICE_ID,
                        EventAttribute.VEHICLE_ID,
                        EventAttribute.MESSAGE_ID, EventAttribute.CORRELATION_ID,
                        EventAttribute.BIZTRANSACTION_ID,
                        EventAttribute.BENCH_MODE, EventAttribute.RESPONSE_EXPECTED,
                        EventAttribute.DEVICE_DELIVERY_CUTOFF,
                        EventAttribute.DFF_QUALIFIER, EventAttribute.USER_CONTEXT,
                        EventAttribute.LAST_UPDATED_TIME));

        filterProvider.addFilter(EventAttribute.RO_RESPONSE_FILTER,
                SimpleBeanPropertyFilter.serializeAllExcept(EventAttribute.ORIGIN,
                        EventAttribute.USERID));

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setFilterProvider(filterProvider);
    }

    /**
     * Create RCPD request response entity.
     *
     * @param clientRequestId the client request id
     * @param requestId       the request id
     * @param sessionId       the session id
     * @param origin          the origin
     * @param userId          the user id
     * @param vehicleId       the vehicle id
     * @return the response entity
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws TooManyRequestException the too many request exception
     */
    @SuppressWarnings("checkstyle:Indentation")
    @Operation(summary = "PUT /v1/rcpd",
            description = "createRCPDRequest",
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Accepted",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = RCPDResponse.class)))
            }
    )
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR,
            scopes = {"RCPDSupportOwner, SelfManage, RCPDRequestB2BOwner"})
    @Parameter(name = "ClientRequestId", description = "ClientRequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "RequestId", description = "RequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "SessionId", description = "SessionId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "OriginId", description = "OriginId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "UserId", description = "UserId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "VehicleId", description = "VehicleId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    @PutMapping(
            value = "/v1/rcpd"
    )
    public ResponseEntity<RCPDResponse> createRCPDRequest(
            @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
            @RequestHeader(value = "RequestId") String requestId,
            @RequestHeader(value = "SessionId") String sessionId,
            @RequestHeader(value = "OriginId") String origin,
            @RequestHeader("UserId") String userId,
            @RequestHeader("VehicleId") String vehicleId)
            throws
            InterruptedException,
            ExecutionException,
            TooManyRequestException {

        RCPDRequestV1_0 rcpdRequest = new RCPDRequestV1_0();
        rcpdRequest.setRcpdRequestId(requestId);
        rcpdRequest.setOrigin(origin);
        rcpdRequest.setUserId(userId);

        LOGGER.info("Received RCPD: {}, userId: {} ,vehicleId: {}  SessionId: {} ",
                Utils.logForging(rcpdRequest),
                Utils.logForging(userId),
                Utils.logForging(vehicleId),
                Utils.logForging(sessionId));

        RCPDResponse rcpdResponse = rcpdService.createRCPDRequest(
                userId,
                vehicleId,
                rcpdRequest,
                sessionId);

        return new ResponseEntity<RCPDResponse>(
                rcpdResponse,
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.ACCEPTED);
    }

    /**
     * Gets RCPD request status with request id.
     *
     * @param clientRequestId the client request id
     * @param sessionId       the session id
     * @param requestId       the request id
     * @param origin          the origin
     * @param userId          the user id
     * @param vehicleId       the vehicle id
     * @param rcpdRequestId   the rcpd request id
     * @param responseLimit   the response limit
     * @return the rcpd request status with request id
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws EmptyResponseException  the empty response exception
     * @throws JsonProcessingException the json processing exception
     */
    @Operation(summary = "GET /v1/rcpd/requests/{rcpdRequestId}",
            description = "get RCPDRequestStatus With RequestId",
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = String.class)))
            }
    )
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR,
            scopes = {"RCPDSupportOwner, ROSupportThirdParty, SelfManage, ROSupportLawEnforcement"})
    @Parameter(name = "ClientRequestId", description = "ClientRequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "RequestId", description = "RequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "SessionId", description = "SessionId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "OriginId", description = "OriginId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "UserId", description = "UserId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "VehicleId", description = "VehicleId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    @GetMapping(
            value = "/v1/rcpd/requests/{rcpdRequestId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> getRCPDRequestStatusWithRequestId(
            @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
            @RequestHeader(value = "SessionId") String sessionId,
            @RequestHeader(value = "RequestId") String requestId,
            @RequestHeader(value = "OriginId") String origin,
            @RequestHeader("UserId") String userId,
            @RequestHeader("VehicleId") String vehicleId,
            @PathVariable("rcpdRequestId") @Parameter(description = "rcpdRequestId") String rcpdRequestId,
            @RequestParam(value = "responsesLimit", required = false, defaultValue = "${ro.status.limit}")
            @Parameter(description = "responsesLimit") Integer responseLimit)
            throws
            InterruptedException,
            ExecutionException,
            EmptyResponseException,
            JsonProcessingException {

        LOGGER.info("Received getRemoteOpStatus for OriginId:{}, userId: {} ,"
                        + "vehicleId: {} , roRequestId: {}  sessionId: {} ",
                Utils.logForging(origin),
                Utils.logForging(userId),
                Utils.logForging(vehicleId),
                Utils.logForging(rcpdRequestId),
                Utils.logForging(sessionId));

        RCPD remoteOpStatus = rcpdService.getRCPDRequestStatusWithRequestId(
                userId,
                vehicleId,
                rcpdRequestId,
                responseLimit);

        return new ResponseEntity<String>(
                JsonMapperUtils.applyExcludefilterAndGetAsString(
                        EventAttribute.RO,
                        remoteOpStatus),
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.OK);
    }

    /**
     * Gets RCPD status.
     *
     * @param clientRequestId the client request id
     * @param sessionId       the session id
     * @param requestId       the request id
     * @param origin          the origin
     * @param userId          the user id
     * @param vehicleId       the vehicle id
     * @return the rcpd status
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws EmptyResponseException  the empty response exception
     * @throws JsonProcessingException the json processing exception
     */
    @Operation(summary = "GET /v1/rcpd/status",
            description = "getRCPDStatus",
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = String.class)))
            }
    )
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"RCPDSupportOwner, SelfManage"})
    @Parameter(name = "ClientRequestId", description = "ClientRequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "RequestId", description = "RequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "SessionId", description = "SessionId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "OriginId", description = "OriginId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "UserId", description = "UserId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "VehicleId", description = "VehicleId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    @GetMapping(
            value = "/v1/rcpd/status",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> getRCPDStatus(@RequestHeader(value = "ClientRequestId",
                                                        required = false) String clientRequestId,
                                                @RequestHeader(value = "SessionId") String sessionId,
                                                @RequestHeader(value = "RequestId") String requestId,
                                                @RequestHeader(value = "OriginId") String origin,
                                                @RequestHeader("UserId") String userId,
                                                @RequestHeader("VehicleId") String vehicleId)
            throws
            InterruptedException,
            ExecutionException,
            EmptyResponseException,
            JsonProcessingException {

        LOGGER.info("Received getRemoteOpStatus for "
                        + "OriginId :{} "
                        + ",userId :{} ,"
                        + "vehicleId :{} "
                        + ", roRequestId: {},"
                        + " sessionId: {}  ",
                Utils.logForging(origin),
                Utils.logForging(userId),
                Utils.logForging(vehicleId),
                Utils.logForging(sessionId));

        String remoteOpStatus = rcpdService.getRCPDStatus(userId, vehicleId);

        if (remoteOpStatus == null) {
            throw new EmptyResponseException(
                    ResponseMsgConstants.STATUS_NOT_FOUND,
                    ResponseMsgConstants.NO_STATUS_DATA_MESSAGE);
        }

        return new ResponseEntity<String>(
                remoteOpStatus,
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.OK);
    }

    /**
     * Fetch RCPD history response entity.
     *
     * @param clientRequestId the client request id
     * @param requestId       the request id
     * @param sessionId       the session id
     * @param origin          the origin
     * @param userId          the user id
     * @param vehicleId       the vehicle id
     * @param eventId         the event id
     * @param since           the since
     * @param until           the until
     * @param lastRecordId    the last record id
     * @param sortOrder       the sort order
     * @param responseCount   the response count
     * @return the response entity
     * @throws JsonProcessingException the json processing exception
     * @throws EmptyResponseException  the empty response exception
     * @throws JSONException           the json exception
     */
    @Operation(summary = "GET /v1/rcpd/history",
            description = "fetchRCPDHistory",
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = String.class)))
            }
    )
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR,
            scopes = {"RCPDSupportOwner, SelfManage, RCPDHistoryOwner"})
    @Parameter(name = "ClientRequestId", description = "ClientRequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "RequestId", description = "RequestId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "SessionId", description = "SessionId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "OriginId", description = "OriginId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "UserId", description = "UserId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Parameter(name = "VehicleId", description = "VehicleId",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    @Timed(name = "showAll-timed")
    @ExceptionMetered
    @Counted(name = "showAll-counted")
    @GetMapping(
            value = "/v1/rcpd/history",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> fetchRCPDHistory(
            @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
            @RequestHeader(value = "RequestId") String requestId,
            @RequestHeader(value = "SessionId") String sessionId,
            @RequestHeader(value = "OriginId") String origin,
            @RequestHeader(value = "UserId") String userId,
            @RequestHeader(value = "VehicleId") String vehicleId,
            @RequestParam(value = "eventId", required = false) @Parameter(description = "eventId") String eventId,
            @RequestParam(value = "since", required = false) @Parameter(description = "since") Long since,
            @RequestParam(value = "until", required = false) @Parameter(description = "until") Long until,
            @RequestParam(value = "lastRecordId", required = false) @Parameter(description = "lastRecordId")
            String lastRecordId,
            @RequestParam(value = "sortOrder", defaultValue = "asc", required = false)
            @Parameter(description = "sortOrder") String sortOrder,
            @RequestParam(value = "responseCount", required = false)
            @Parameter(description = "responseCount") Integer responseCount)
            throws
            JsonProcessingException,
            EmptyResponseException,
            JSONException {

        LOGGER.info(
                "Received GET rcpdHistory for userId :{}"
                        + " ,vehicleId :{},"
                        + " eventId: "
                        + "{}, since: {}"
                        + ", until: {}"
                        + ", responseCount: {}"
                        + ", sortOrder: {}"
                        + ", lastRecordId: {}",
                Utils.logForging(userId),
                Utils.logForging(vehicleId),
                Utils.logForging(eventId),
                Utils.logForging(since),
                Utils.logForging(until),
                Utils.logForging(responseCount),
                Utils.logForging(sortOrder),
                Utils.logForging(lastRecordId));

        Map<Long, List<RCPD>> list = rcpdService.fetchRCPDHistory(vehicleId, since,
                until, responseCount,
                lastRecordId, sortOrder, eventId);

        String alertsJsonArray = mapper.writeValueAsString(list.values().iterator().next());

        JSONObject alertsJsonObject = new JSONObject();

        alertsJsonObject.put(EventIdConstants.TOTAL_RECORDS.getValue(), list.keySet().iterator().next());
        alertsJsonObject.put(EventIdConstants.ALERTS.getValue(), new JSONArray(alertsJsonArray));

        return new ResponseEntity<>(alertsJsonObject.toString(),
                ApiUtils.getHeaders(clientRequestId, sessionId, requestId),
                HttpStatus.OK);

    }

}
