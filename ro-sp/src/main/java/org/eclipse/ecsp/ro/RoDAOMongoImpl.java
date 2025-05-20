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

package org.eclipse.ecsp.ro;

import org.apache.commons.lang3.ObjectUtils;
import org.bson.types.ObjectId;
import org.eclipse.ecsp.domain.ro.Ro;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteOrderBy;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.eclipse.ecsp.ro.constants.Constants.ONE;

/**
 * DAO Layer for Ro object.
 *
 * @author Neerajkumar
 */
@Repository
public class RoDAOMongoImpl extends IgniteBaseDAOMongoImpl<ObjectId, Ro> {

    public static final String RO_SESSION_ID = "roEvent.bizTransactionId";
    public static final String RO_VEHICLE_ID = "roEvent.vehicleId";
    public static final String RO_REQUEST_ID = "roEvent.eventData.roRequestId";
    public static final String RO_MSGID = "roEvent.messageId";
    public static final String RO_SCHEDULER_KEY = "roEvent.eventData.schedulerKey";
    public static final String RO_EVENT_TIMESTAMP = "roEvent.timestamp";

    /**
     * Get Ro entity by roRequestId and vehicleId.
     *
     * @param roRequestId roRequestId
     * @param vehicleId   vehicleId
     */
    public Optional<Ro> getRIEntityByFieldName(String roRequestId, String vehicleId) {
        IgniteCriteria criteria = new IgniteCriteria(RO_REQUEST_ID, Operator.EQ, roRequestId);

        IgniteCriteria vehicleIdCriteria = new IgniteCriteria(RO_VEHICLE_ID, Operator.EQ, vehicleId);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(criteria);
        criteriaGroup.and(vehicleIdCriteria);
        IgniteQuery igQuery = new IgniteQuery(criteriaGroup);

        List<Ro> entities = (List<Ro>) this.find(igQuery);
        Ro roRes = CollectionUtils.isEmpty(entities) ? null : entities.get(Constants.ZERO);

        return Optional.ofNullable(roRes);
    }

    /**
     * Get latest Ro entity by sessionId and vehicleId.
     *
     * @param sessionId sessionId
     * @param vehicleId vehicleId
     */
    public Ro getLatesRIEntityForNotification(String sessionId, String vehicleId) {
        final int one = 1;
        final int minusOne = -1;
        IgniteCriteria sessionCriteria = new IgniteCriteria(RO_SESSION_ID, Operator.EQ, sessionId);
        IgniteCriteria vehicleCriteria = new IgniteCriteria(RO_VEHICLE_ID, Operator.EQ, vehicleId);

        IgniteCriteriaGroup criteriaGroup =
                new IgniteCriteriaGroup(sessionCriteria).and(vehicleCriteria);
        IgniteQuery igQuery = new IgniteQuery(criteriaGroup);
        List<Ro> entities = this.find(igQuery);

        if (!entities.isEmpty()) {
            Collections.sort(
                    entities,
                    (e1, e2) ->
                            (e2.getRoEvent().getTimestamp() > e1.getRoEvent().getTimestamp()) ? one : minusOne);
            return entities.get(org.eclipse.ecsp.ro.constants.Constants.ZERO);
        }
        return null;
    }

    /**
     * Prepare IgniteQuery with sessionId and vehicleId in the IgniteCriteriaGroup.
     *
     * @param roRequestId roRequestId
     * @param vehicleId   vehicleId
     * @see IgniteQuery
     * @see IgniteCriteriaGroup
     */
    public IgniteQuery prepareIgniteQueryForRoRequest(String roRequestId, String vehicleId) {
        IgniteCriteria criteria = new IgniteCriteria(RO_REQUEST_ID, Operator.EQ, roRequestId);
        IgniteCriteria vehicleIdCriteria = new IgniteCriteria(RO_VEHICLE_ID, Operator.EQ, vehicleId);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(criteria);
        criteriaGroup.and(vehicleIdCriteria);
        IgniteQuery igniteQuery = new IgniteQuery(criteriaGroup);
        igniteQuery.setPageSize(ONE);
        igniteQuery.setPageNumber(ONE);
        return igniteQuery;
    }

    /**
     * Prepare IgniteQuery with sessionId, messageId and vehicleId in the IgniteCriteriaGroup.
     *
     * @param sessionId sessionId
     * @param vehicleId vehicleId
     * @param messageId messageId
     * @see IgniteQuery
     * @see IgniteCriteriaGroup
     */
    public IgniteQuery prepareIgniteQueryBySessionIdANDMsgId(
            String vehicleId, String sessionId, String messageId) {
        IgniteCriteria sessionCriteria = new IgniteCriteria(RO_SESSION_ID, Operator.EQ, sessionId);
        IgniteCriteria msgIdCriteria = new IgniteCriteria(RO_MSGID, Operator.EQ, messageId);
        IgniteCriteria vehicleIdCriteria = new IgniteCriteria(RO_VEHICLE_ID, Operator.EQ, vehicleId);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(vehicleIdCriteria);
        criteriaGroup.and(sessionCriteria);
        criteriaGroup.and(msgIdCriteria);
        IgniteQuery igniteQuery = new IgniteQuery(criteriaGroup);
        igniteQuery.orderBy(new IgniteOrderBy().byfield(RO_EVENT_TIMESTAMP).desc());
        igniteQuery.setPageSize(ONE);
        igniteQuery.setPageNumber(ONE);
        return igniteQuery;
    }

    /**
     * Prepare IgniteQuery with sessionId and vehicleId in the IgniteCriteriaGroup.
     *
     * @param sessionId sessionId
     * @param vehicleId vehicleId
     * @see IgniteQuery
     * @see IgniteCriteriaGroup
     */
    public IgniteQuery prepareIgniteQueryForRIRequestWithSessionId(
            String sessionId, String vehicleId) {

        IgniteCriteria sessionCriteria = new IgniteCriteria(RO_SESSION_ID, Operator.EQ, sessionId);
        IgniteCriteria vehicleCriteria = new IgniteCriteria(RO_VEHICLE_ID, Operator.EQ, vehicleId);

        IgniteCriteriaGroup criteriaGroup =
                new IgniteCriteriaGroup(sessionCriteria).and(vehicleCriteria);
        IgniteQuery igniteQuery =
                new IgniteQuery(criteriaGroup)
                        .orderBy(new IgniteOrderBy().byfield(RO_EVENT_TIMESTAMP).desc());
        igniteQuery.setPageSize(ONE);
        igniteQuery.setPageNumber(ONE);
        return igniteQuery;
    }

    private Optional<Ro> getROEntityByFieldName(String vehicleId, IgniteCriteria criteria) {
        IgniteCriteria vehicleIdCriteria = new IgniteCriteria(RO_VEHICLE_ID, Operator.EQ, vehicleId);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(criteria);
        criteriaGroup.and(vehicleIdCriteria);
        IgniteQuery igQuery =
                new IgniteQuery(criteriaGroup)
                        .orderBy(new IgniteOrderBy().byfield(RO_EVENT_TIMESTAMP).desc());

        List<Ro> roList = find(igQuery);
        if (ObjectUtils.isEmpty(roList)) {
            return Optional.empty();
        }

        return Optional.of(roList.get(0));
    }

    public Optional<Ro> getROEntityByFieldNameByRoReqIdExceptACV(
            String vehicleId, String roRequestId) {
        IgniteCriteria criteria = new IgniteCriteria(RO_REQUEST_ID, Operator.EQ, roRequestId);
        return getROEntityByFieldName(vehicleId, criteria);
    }

    public Optional<Ro> getROEntityByFieldNameByRoReqId(String vehicleId, String roRequestId) {
        IgniteCriteria criteria = new IgniteCriteria(RO_REQUEST_ID, Operator.EQ, roRequestId);
        return getROEntityByFieldName(vehicleId, criteria);
    }

    public Optional<Ro> getROEntityByFieldNameByBizIdExceptACV(String vehicleId, String sessionId) {
        IgniteCriteria criteria = new IgniteCriteria(RO_SESSION_ID, Operator.EQ, sessionId);
        return getROEntityByFieldName(vehicleId, criteria);
    }
}
