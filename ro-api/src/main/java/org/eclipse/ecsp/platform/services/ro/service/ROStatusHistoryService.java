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

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.ro.Ro;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.exceptions.EmptyResponseException;
import org.eclipse.ecsp.exceptions.ForbiddenException;
import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteOrderBy;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.platform.services.ro.constant.ResponseMsgConstants;
import org.eclipse.ecsp.platform.services.ro.constant.RoApiConstants;
import org.eclipse.ecsp.platform.services.ro.dao.RoDAOMongoImpl;
import org.eclipse.ecsp.utils.ApiPaginationUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import static org.eclipse.ecsp.platform.services.ro.constant.NumericConstants.ZERO;

/**
 * Service Class for RO Status History Operations.
 *
 * @author midnani
 */
@Service
public class ROStatusHistoryService {

    private static final String RO_EVENT_TIMESTAMP = "roEvent.timestamp";

    private static final String RO_EVENT_VEHICLE_ID = "roEvent.vehicleId";

    private static final String RO_EVENT_REQUEST_ID = "roEvent.eventData.roRequestId";

    private static final String RO_EVENT_ID = "roEvent.eventId";

    private static final String RO_EVENT_USER_ID = "roEvent.userContextInfo.userId";

    private static final String RO_EVENT_REQUEST_ID_OUTTER = "roEvent.requestId";

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ROStatusHistoryService.class);

    @Autowired
    private RoDAOMongoImpl roDAOMongoImpl;

    @Value("${history.default.page.size:1000}")
    private int defaultPageSize;

    private ApiPaginationUtils paginationUtils = new ApiPaginationUtils(
            RO_EVENT_ID,
            RO_EVENT_TIMESTAMP,
            RO_EVENT_VEHICLE_ID);

    /**
     * Gets remote op status.
     *
     * @param userId        : userId
     * @param vehicleId     : vehicleId
     * @param roRequestId   : roRequestId
     * @param responseLimit : responseLimit
     * @return RO
     * @throws InterruptedException   InterruptedException
     * @throws ExecutionException     ExecutionException
     * @throws EmptyResponseException EmptyResponseException
     */
    public Ro getRemoteOpStatus(String userId,
                                String vehicleId, String roRequestId, Integer responseLimit)
            throws EmptyResponseException, ForbiddenException {
        LOGGER.info("Fetching Remote Operation Status for vehicleId:{}, userid :{},"
                        + " roRequestId: {}", Utils.logForging(vehicleId),
                Utils.logForging(userId),
                Utils.logForging(roRequestId));
        List<Ro> roStatus = getRoEntitiesByKey(RO_EVENT_REQUEST_ID, roRequestId,
                RO_EVENT_VEHICLE_ID, vehicleId);
        LOGGER.debug(RoApiConstants.VEHICLE_ID + ":{}, Remote Operation history list size :{}",
                Utils.logForging(vehicleId),
                Utils.logForging((roStatus != null ? roStatus.size() : 0)));
        if (Objects.isNull(roStatus) || Objects.isNull(roStatus.get(0))) {
            throw new EmptyResponseException(ResponseMsgConstants.HISTORY_NOT_FOUND,
                    ResponseMsgConstants.NO_HISTORY_DATA_MESSAGE);
        }
        Ro status = roStatus.get(0);

        List<IgniteEvent> roResponseList = status.getRoResponseList();

        if (ObjectUtils.isNotEmpty(roResponseList)) {
            if (roResponseList.size() > responseLimit) {
                roResponseList = roResponseList.subList(0, responseLimit);
            }
            roResponseList.sort(Utils.getIgniteEventTimeStampComparator());
            status.setRoResponseList(roResponseList);
        }
        return status;
    }

    /**
     * Gets remote op status v 2.
     *
     * @param userId        the user id
     * @param vehicleId     the vehicle id
     * @param roRequestId   the ro request id
     * @param responseLimit the response limit
     * @return the remote op status v 2
     * @throws EmptyResponseException the empty response exception
     */
    public List<Ro> getRemoteOpStatusV2(String userId,
                                        String vehicleId, String roRequestId, Integer responseLimit)
            throws EmptyResponseException {
        LOGGER.info("Fetching Remote Operation Status V2 for vehicleId:{}, userid :{}, "
                        + "roRequestId: {}", Utils.logForging(vehicleId),
                Utils.logForging(userId),
                Utils.logForging(roRequestId));
        List<Ro> roStatus = getRoEntitiesByKey(RO_EVENT_REQUEST_ID, roRequestId, RO_EVENT_VEHICLE_ID, vehicleId);
        LOGGER.debug("vehicleId:{}, Remote Operation history list size :{}",
                Utils.logForging(vehicleId),
                Utils.logForging((roStatus != null ? roStatus.size() : 0)));
        if (ObjectUtils.isEmpty(roStatus)) {
            throw new EmptyResponseException(ResponseMsgConstants.HISTORY_NOT_FOUND,
                    ResponseMsgConstants.NO_HISTORY_DATA_MESSAGE);
        }

        for (Ro status : roStatus) {
            List<IgniteEvent> roResponseList = status.getRoResponseList();
            if (ObjectUtils.isNotEmpty(roResponseList)) {
                if (roResponseList.size() > responseLimit) {
                    roResponseList = roResponseList.subList(0, responseLimit);
                }
                roResponseList.sort(Utils.getIgniteEventTimeStampComparator());
                status.setRoResponseList(roResponseList);
            }
        }

        return roStatus;
    }

    /**
     * Gets remote op history.
     *
     * @param userId        : userId
     * @param vehicleId     : vehicleId
     * @param responseLimit : responseLimit
     * @return List of Ro
     * @throws InterruptedException   InterruptedException
     * @throws ExecutionException     ExecutionException
     * @throws EmptyResponseException EmptyResponseException
     */
    public List<Ro> getRemoteOpHistory(String userId,
                                       String vehicleId, Integer responseLimit)
            throws InterruptedException, ExecutionException, EmptyResponseException {
        LOGGER.debug("Fetching Remote Operation history for vehicleId:{}, userid :{}",
                Utils.logForging(vehicleId), Utils.logForging(userId));
        List<Ro> roHistory = getRoEntitiesByKey(RO_EVENT_VEHICLE_ID, vehicleId,
                RO_EVENT_USER_ID, userId, responseLimit);
        LOGGER.debug("vehicleId:{}, Remote Operation history list size :{}",
                Utils.logForging(vehicleId), Utils.logForging((roHistory == null
                        ? 0 : roHistory.size())));
        if (roHistory == null || roHistory.isEmpty()) {
            throw new EmptyResponseException(ResponseMsgConstants.HISTORY_NOT_FOUND,
                    ResponseMsgConstants.NO_HISTORY_DATA_MESSAGE);
        }
        return roHistory;
    }

    /**
     * Fetch ro history for portal map.
     *
     * @param vehicleId      vehicleId
     * @param since          since
     * @param until          until
     * @param responsesLimit responsesLimit
     * @param lastRecordId   lastRecordId
     * @param sortOrder      sortOrder
     * @param eventId        eventId
     * @return Map
     * @throws EmptyResponseException EmptyResponseException
     */
    public Map<Long, List<Ro>> fetchRoHistoryForPortal(String vehicleId, Long since,
                                                       Long until, Integer responsesLimit,
                                                       String lastRecordId, String sortOrder,
                                                       String eventId) throws EmptyResponseException {

        LOGGER.debug("Advanced fetching Ecall history for the vehicleId :{} ",
                Utils.logForging(vehicleId));

        IgniteCriteriaGroup basicCriteriaGroup = paginationUtils.buildBasicCriteriaGroup(
                vehicleId, since, until, eventId);
        excludeEvents(basicCriteriaGroup, eventId);
        IgniteQuery igQuery = new IgniteQuery(basicCriteriaGroup);

        // ignite criteria group not support clone, so need to create a new instance one more time.
        IgniteCriteriaGroup basicCriteriaGroupCopy = paginationUtils.buildBasicCriteriaGroup(vehicleId,
                since, until, eventId);
        excludeEvents(basicCriteriaGroupCopy, eventId);
        Ro lastRecord = getLastRecord(lastRecordId);
        if (lastRecord != null) {
            paginationUtils.setTimestamp(lastRecord.getRoEvent().getTimestamp());
            paginationUtils.setObjectId(lastRecord.getId());
            igQuery = paginationUtils.buildIgniteQuery(basicCriteriaGroup, basicCriteriaGroupCopy,
                    sortOrder);
        }

        paginationUtils.buildSortByAndLimit(igQuery, sortOrder, responsesLimit,
                defaultPageSize);

        List<Ro> ecallList = roDAOMongoImpl.find(igQuery);

        if (CollectionUtils.isEmpty(ecallList)) {
            throw new EmptyResponseException(ResponseMsgConstants.HISTORY_NOT_FOUND,
                    ResponseMsgConstants.NO_HISTORY_DATA_MESSAGE);
        }

        Map<Long, List<Ro>> result = new HashMap<>();
        long totalCount = roDAOMongoImpl.countByQuery(igQuery);
        if (totalCount != ZERO && !CollectionUtils.isEmpty(ecallList)) {
            result.put(totalCount, ecallList);
        }

        return result;
    }

    /**
     * Get Last Record.
     *
     * @param lastRecordId lastRecordId
     * @return Ro
     */
    private Ro getLastRecord(String lastRecordId) {
        if (StringUtils.isEmpty(lastRecordId)) {
            return null;
        }

        IgniteCriteria requestIdCriteria = new IgniteCriteria(RO_EVENT_REQUEST_ID_OUTTER,
                Operator.EQ, lastRecordId);
        IgniteQuery igQuery = new IgniteQuery(new IgniteCriteriaGroup(requestIdCriteria));

        List<Ro> ecallList = roDAOMongoImpl.find(igQuery);
        if (CollectionUtils.isEmpty(ecallList)) {
            return null;
        }
        return ecallList.get(ZERO);
    }

    /**
     * Exclude Events.
     *
     * @param basicCriteriaGroup basicCriteriaGroup
     * @param eventId            eventId
     */
    private void excludeEvents(IgniteCriteriaGroup basicCriteriaGroup, String eventId) {
        if (StringUtils.isEmpty(eventId)) {
            List<String> riEventList = new ArrayList<>();
            riEventList.add(Constants.EVENT_ID_REMOTE_INHIBIT_REQUEST);
            riEventList.add(Constants.EVENT_ID_REMOTE_INHIBIT_RESPONSE);
            riEventList.add(Constants.EVENT_ID_CRANK_NOTIFICATION_DATA);

            basicCriteriaGroup.and(new IgniteCriteria(RO_EVENT_ID, Operator.NOT_IN,
                    riEventList));
        }
    }

    /**
     * Get RO Entities by key .
     *
     * @param vehicleKey    vehicleKey
     * @param vehicleValue  vehicleValue
     * @param userKey       userKey
     * @param userValue     userValue
     * @param responseLimit responseLimit
     * @return List of Ro
     */
    private List<Ro> getRoEntitiesByKey(String vehicleKey, String vehicleValue,
                                        String userKey, String userValue, Integer responseLimit) {
        IgniteCriteria vehicleCriteria = new IgniteCriteria(vehicleKey, Operator.EQ,
                vehicleValue);
        IgniteCriteria userCriteria = new IgniteCriteria(userKey, Operator.EQ, userValue);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(vehicleCriteria)
                .and(userCriteria);

        excludeEvents(criteriaGroup, null);

        IgniteQuery igQuery;
        if (null != responseLimit) {
            igQuery = new IgniteQuery(criteriaGroup).orderBy(new IgniteOrderBy().byfield(
                    RO_EVENT_TIMESTAMP).desc());
            igQuery.setPageSize(responseLimit);
            igQuery.setPageNumber(1);
        } else {
            igQuery = new IgniteQuery(criteriaGroup).orderBy(new IgniteOrderBy()
                    .byfield(RO_EVENT_TIMESTAMP).desc());
        }
        List<Ro> roList = roDAOMongoImpl.find(igQuery);
        LOGGER.debug("vehicleId:{}, Remote Operation history list :{}",
                Utils.logForging(vehicleValue), Utils.logForging(roList));

        return roList;
    }

    private List<Ro> getRoEntitiesByKey(String key, String value, String keyVehicleID,
                                        String valueVehicleID) {
        IgniteCriteria criteria = new IgniteCriteria(key, Operator.EQ, value);
        IgniteCriteria criteriaVehicle = new IgniteCriteria(keyVehicleID,
                Operator.EQ, valueVehicleID);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(criteria)
                .and(criteriaVehicle);
        IgniteQuery igQuery = new IgniteQuery(criteriaGroup);
        return roDAOMongoImpl.find(igQuery);
    }

}
