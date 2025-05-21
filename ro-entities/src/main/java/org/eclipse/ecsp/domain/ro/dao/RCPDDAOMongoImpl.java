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

package org.eclipse.ecsp.domain.ro.dao;

import org.eclipse.ecsp.domain.ro.RCPD;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import java.util.List;
import java.util.Optional;

/**
 * Mongo DAO class for RCPD.
 */
@Repository
public class RCPDDAOMongoImpl extends IgniteBaseDAOMongoImpl<String, RCPD> {
    public static final int ONE = 1;
    private static String VEHICLE_ID = "rcpdEvent.vehicleId";
    private static String RCPDREQUESTID = "rcpdEvent.eventData.rcpdRequestId";
    private static String SCHEDULEID = "rcpdEvent.eventData.scheduleRequestId";

    /**
     * Get RCPD object from the mongo db.
     *
     * @param vehicleid     vehicle id
     * @param rcpdRequestId rcpd request id
     * @param scheduleId    schedule id
     * @return {@link RCPD} object
     */
    public Optional<RCPD> getRCPDRequest(String vehicleid, String rcpdRequestId, String scheduleId) {
        IgniteCriteria vehicleIdCriteria = new IgniteCriteria(VEHICLE_ID, Operator.EQ, vehicleid);
        IgniteCriteria roRequestCriteria = null;
        if (rcpdRequestId != null) {
            roRequestCriteria = new IgniteCriteria(RCPDREQUESTID, Operator.EQ, rcpdRequestId);
        } else if (scheduleId != null) {
            roRequestCriteria = new IgniteCriteria(SCHEDULEID, Operator.EQ, scheduleId);
        }

        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(roRequestCriteria).and(vehicleIdCriteria);

        IgniteQuery igQuery = new IgniteQuery(criteriaGroup);

        List<RCPD> entities = (List<RCPD>) this.find(igQuery);
        RCPD rcpdRes = CollectionUtils.isEmpty(entities) ? null : entities.get(Constants.ZERO);

        return Optional.ofNullable(rcpdRes);
    }

    /**
     * Prepare Ignite Query to fetch RCPD object from the DB.
     *
     * @param roRequestId ro request id
     * @param vehicleId   vehicle id
     * @return {@link IgniteQuery} object
     */
    public IgniteQuery prepareIgniteQueryForRcpdRequest(String roRequestId, String vehicleId) {

        IgniteCriteria criteria = new IgniteCriteria(RCPDREQUESTID, Operator.EQ, roRequestId);
        IgniteCriteria vehicleIdCriteria = new IgniteCriteria(VEHICLE_ID, Operator.EQ, vehicleId);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(criteria);
        criteriaGroup.and(vehicleIdCriteria);
        IgniteQuery igniteQuery = new IgniteQuery(criteriaGroup);
        igniteQuery.setPageSize(ONE);
        igniteQuery.setPageNumber(ONE);
        return igniteQuery;
    }
}