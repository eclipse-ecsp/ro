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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.cache.IgniteCache;
import org.eclipse.ecsp.cache.PutStringRequest;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.RCPD;
import org.eclipse.ecsp.domain.ro.RCPDRequestV1_0;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.domain.ro.dao.RCPDDAOMongoImpl;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.exceptions.EmptyResponseException;
import org.eclipse.ecsp.exceptions.TooManyRequestException;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.platform.services.ro.constant.ResponseMsgConstants;
import org.eclipse.ecsp.platform.services.ro.domain.RCPDResponse;
import org.eclipse.ecsp.utils.ApiPaginationUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import static org.eclipse.ecsp.platform.services.ro.constant.NumericConstants.ZERO;

/**
 * Service class for RCPD event operations.
 **/
@Service
public class RCPDService {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(RCPDService.class);
    private static final String RCPD_EVENT_ID = "rcpdEvent.eventId";
    private static final String RCPD_EVENT_REQUEST_ID = "rcpdEvent.eventData.rcpdRequestId";
    private static final String RCPD_EVENT_REQUEST_ID_OUTTER = "rcpdEvent.requestId";
    private static final String RCPD_EVENT_VEHICLE_ID = "rcpdEvent.vehicleId";
    private static final String RCPD_EVENT_TIMESTAMP = "rcpdEvent.timestamp";

    private final ApiPaginationUtils paginationUtils =
            new ApiPaginationUtils(
                    RCPD_EVENT_ID,
                    RCPD_EVENT_TIMESTAMP,
                    RCPD_EVENT_VEHICLE_ID);

    @Autowired
    RCPDDAOMongoImpl rcpdDAOMongoImpl;

    @Autowired
    private KafkaService kafkaService;

    @Value("${rcpd.kafka.sink.topic}")
    private String rcpdKafkaTopic;

    @Value("${history.default.page.size:1000}")
    private int defaultPageSize;

    @Autowired
    private Utils utils;

    @Autowired
    private IgniteCache igniteCache;


    /**
     * Create RCPD request.
     *
     * @param userId    the user id
     * @param vehicleId the vehicle id
     * @param sessionId the session id
     * @return the remote operation response
     * @throws ExecutionException the execution exception
     */
    // Service to handle RCPD Requests
    public RCPDResponse createRCPDRequest(String userId,
                                          String vehicleId,
                                          RCPDRequestV1_0 rcpdRequest,
                                          String sessionId)
            throws
            ExecutionException,
            TooManyRequestException {

        LOGGER.info("Publishing RCPD command to Kafka for origin: {} vehicleId: {}, userid: {}, RCPDRequest:{}",
                Utils.logForging(rcpdRequest.getOrigin()),
                Utils.logForging(vehicleId),
                Utils.logForging(userId),
                Utils.logForging(rcpdRequest));

        if (Boolean.TRUE.equals(isRCPDRequestPending(userId, vehicleId))) {
            throw new TooManyRequestException(
                    ResponseMsgConstants.MULTIPLE_REQUEST_NOT_ALLOWED,
                    ResponseMsgConstants.MULTIPLE_REQUEST_NOT_ALLOWED_MESSAGE);
        }

        kafkaService.sendIgniteEventonTopic(
                utils.createIgniteEvent(
                        Version.V1_0,
                        Constants.RCPDREQUEST,
                        vehicleId,
                        rcpdRequest,
                        rcpdRequest.getRcpdRequestId(),
                        sessionId,
                        userId),
                rcpdKafkaTopic);

        return new RCPDResponse(ResponseMsgConstants.RCPD_COMMAND_SUCCESS, rcpdRequest.getRcpdRequestId());

    }

    /**
     * Gets RCPD request status with request id.
     *
     * @param userId        the user id
     * @param vehicleId     the vehicle id
     * @param rcpdRequestId the rcpd request id
     * @param responseLimit the response limit
     * @return the rcpd request status with request id
     * @throws InterruptedException   the interrupted exception
     * @throws ExecutionException     the execution exception
     * @throws EmptyResponseException the empty response exception
     */
    public RCPD getRCPDRequestStatusWithRequestId(String userId,
                                                  String vehicleId,
                                                  String rcpdRequestId,
                                                  Integer responseLimit)
            throws
            InterruptedException,
            ExecutionException,
            EmptyResponseException {

        LOGGER.info("Fetching RCPD Status for vehicleId: {}, userid: {}, rcpdRequestId: {}",
                Utils.logForging(vehicleId),
                Utils.logForging(userId),
                Utils.logForging(rcpdRequestId));

        List<RCPD> rcpdStatus = getRoEntitiesByKey(
                RCPD_EVENT_REQUEST_ID,
                rcpdRequestId,
                RCPD_EVENT_VEHICLE_ID,
                vehicleId);

        LOGGER.debug("vehicleId: {}, RCPD list size :{}",
                Utils.logForging(vehicleId),
                Utils.logForging((rcpdStatus != null ? rcpdStatus.size() : 0)));

        if (rcpdStatus == null || rcpdStatus.isEmpty()) {
            throw new EmptyResponseException(
                    ResponseMsgConstants.HISTORY_NOT_FOUND,
                    ResponseMsgConstants.NO_HISTORY_DATA_MESSAGE);
        }

        RCPD status = rcpdStatus.get(0);

        List<IgniteEvent> rcpdResponseList = status.getRcpdResponseList();

        if (!CollectionUtils.isEmpty(rcpdResponseList)) {
            if (rcpdResponseList.size() > responseLimit) {
                rcpdResponseList = rcpdResponseList.subList(0, responseLimit);
            }
            rcpdResponseList.sort(Utils.getIgniteEventTimeStampComparator());
            status.setRcpdResponseList(rcpdResponseList);
        }
        return status;
    }

    /**
     * Gets RCPD status.
     *
     * @param userId    the user id
     * @param vehicleId the vehicle id
     * @return the RCPD status
     * @throws InterruptedException   the interrupted exception
     * @throws ExecutionException     the execution exception
     * @throws EmptyResponseException the empty response exception
     */
    public String getRCPDStatus(String userId, String vehicleId)
            throws
            InterruptedException,
            ExecutionException,
            EmptyResponseException {

        LOGGER.info("Fetching Remote Operation Status for vehicleId:{}, userid : {}",
                Utils.logForging(vehicleId),
                Utils.logForging(userId));

        String json = igniteCache.getString(Constants.getRedisKey(
                Constants.RCPD_SERVICE,
                vehicleId,
                Constants.RCPD_STATUS));

        if (json != null) {
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
            };

            try {
                HashMap<String, Object> mapResponse = mapper.readValue(json, typeRef);
                if (mapResponse.get(Constants.RCPD_REQUEST_USERID) != null) {
                    mapResponse.remove(Constants.RCPD_REQUEST_USERID);
                }
                json = Utils.convertMapToJSonString(mapResponse);
            } catch (JsonProcessingException e) {
                LOGGER.debug("Error while while converting string  to map");
            }
        }

        return json;
    }

    /**
     * Fetch RCPD history.
     *
     * @param vehicleId      the vehicle id
     * @param since          since period
     * @param until          until period
     * @param responsesLimit number of responses required
     * @param lastRecordId   the last record id
     * @param sortOrder      the sort order
     * @param eventId        the event id
     * @return the map
     * @throws EmptyResponseException the empty response exception
     */
    public Map<Long, List<RCPD>> fetchRCPDHistory(
            String vehicleId,
            Long since,
            Long until,
            Integer responsesLimit,
            String lastRecordId,
            String sortOrder,
            String eventId)
            throws
            EmptyResponseException {

        LOGGER.debug("Advanced fetching RCPD history for the vehicleId: {} ",
                Utils.logForging(vehicleId));

        IgniteCriteriaGroup basicCriteriaGroup = paginationUtils
                .buildBasicCriteriaGroup(
                        vehicleId,
                        since,
                        until,
                        eventId);

        IgniteQuery igQuery = new IgniteQuery(basicCriteriaGroup);

        // ignite criteria group not support clone, so need to create a new instance one
        // more time.
        IgniteCriteriaGroup basicCriteriaGroupCopy = paginationUtils
                .buildBasicCriteriaGroup(
                        vehicleId,
                        since,
                        until,
                        eventId);

        RCPD lastRecord = getLastRecord(lastRecordId);

        if (lastRecord != null) {
            paginationUtils.setTimestamp(lastRecord.getRcpdEvent().getTimestamp());
            paginationUtils.setObjectId(lastRecord.getId());
            igQuery = paginationUtils.buildIgniteQuery(basicCriteriaGroup, basicCriteriaGroupCopy, sortOrder);
        }

        paginationUtils.buildSortByAndLimit(igQuery, sortOrder, responsesLimit, defaultPageSize);

        List<RCPD> rcpdList = rcpdDAOMongoImpl.find(igQuery);
        if (CollectionUtils.isEmpty(rcpdList)) {
            throw new EmptyResponseException(
                    ResponseMsgConstants.HISTORY_NOT_FOUND,
                    ResponseMsgConstants.NO_HISTORY_DATA_MESSAGE);
        }

        Map<Long, List<RCPD>> result = new HashMap<>();
        long totalCount = rcpdDAOMongoImpl.countByQuery(igQuery);
        if (totalCount != ZERO && !CollectionUtils.isEmpty(rcpdList)) {
            result.put(totalCount, rcpdList);
        }

        return result;
    }

    private List<RCPD> getRoEntitiesByKey(String key,
                                          String value,
                                          String keyVehicleId,
                                          String valueVehicleID) {

        IgniteCriteria criteria = new IgniteCriteria(key, Operator.EQ, value);
        IgniteCriteria criteriaVehicle = new IgniteCriteria(keyVehicleId, Operator.EQ, valueVehicleID);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(criteria).and(criteriaVehicle);
        IgniteQuery igQuery = new IgniteQuery(criteriaGroup);
        return rcpdDAOMongoImpl.find(igQuery);
    }

    private RCPD getLastRecord(String lastRecordId) {

        if (StringUtils.isEmpty(lastRecordId)) {
            return null;
        }

        IgniteCriteria requestIdCriteria = new IgniteCriteria(RCPD_EVENT_REQUEST_ID_OUTTER, Operator.EQ, lastRecordId);
        IgniteQuery igQuery = new IgniteQuery(new IgniteCriteriaGroup(requestIdCriteria));

        List<RCPD> ecallList = rcpdDAOMongoImpl.find(igQuery);

        if (CollectionUtils.isEmpty(ecallList)) {
            return null;
        }

        return ecallList.get(ZERO);
    }

    private Boolean isRCPDRequestPending(String userId, String vehicleId) {

        String vehicleStatus = null;

        try {
            String json = getRCPDStatus(userId, vehicleId);
            ObjectMapper mapper = new ObjectMapper();
            if (json != null) {
                Map<String, Object> map = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
                });
                if (map != null) {
                    vehicleStatus = (String) map.get(Constants.VEHICLE_STATUS);
                }

                if (vehicleStatus != null && vehicleStatus.equalsIgnoreCase(Constants.PENDING)) {
                    return true;
                }
            }

            Map<String, Object> mapOfVehicleStatus = new HashMap<String, Object>();
            mapOfVehicleStatus.put(Constants.VEHICLE_STATUS, Constants.PENDING);
            mapOfVehicleStatus.put(Constants.TIME_STAMP, System.currentTimeMillis());
            mapOfVehicleStatus.put(Constants.RCPD_REQUEST_USERID, userId);

            String value = mapper.writeValueAsString(mapOfVehicleStatus);
            igniteCache.putString(
                    new PutStringRequest().withKey(Constants.getRedisKey(
                                    Constants.RCPD_SERVICE, vehicleId, Constants.RCPD_STATUS))
                            .withValue(value));

        } catch (EmptyResponseException e) {
            LOGGER.info("No data found for RCPD status");
        } catch (IOException e) {
            LOGGER.debug("Error occurred while converting string to map");
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Error occurred while converting map to json string: {}", e);
        }
        return false;

    }

}
