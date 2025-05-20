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
import org.eclipse.ecsp.domain.ro.RoScheduleV2;
import org.eclipse.ecsp.domain.ro.ScheduleStatus;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteOrderBy;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.nosqldao.Updates;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.springframework.stereotype.Repository;
import java.time.ZoneId;
import java.util.List;

/**
 * DAO Layer for RoScheduleV2 object.
 */
@Repository
public class RoScheduleV2DAOMongoImpl extends IgniteBaseDAOMongoImpl<ObjectId, RoScheduleV2> {

    /**
     * Find RoScheduleV2 by vehicleId and roType.
     *
     * @param vehicleId vehicleId
     * @param roType    roType
     * @return List {@link RoScheduleV2} List of schedules as RoScheduleV2
     */
    public List<RoScheduleV2> findByVehicleId(String vehicleId, String roType) {
        IgniteCriteriaGroup igniteCriteriaGroup = new IgniteCriteriaGroup(
                new IgniteCriteria(Constants.RO_VEHICLE_ID, Operator.EQ, vehicleId))
                .and(new IgniteCriteria(Constants.RO_SCHEDULE_TYPE, Operator.EQ, roType));
        IgniteQuery baseIgniteQuery = new IgniteQuery(igniteCriteriaGroup);
        return find(baseIgniteQuery);
    }

    /**
     * Update scheduleId and status for the given vehicleId, schedulerKey and roType.
     *
     * @param vehicleId    vehicleId
     * @param schedulerKey schedulerKey
     * @param scheduleId   scheduleId - to be updated
     * @param roType       roType
     * @param status       status
     * @return boolean - if update is success
     * @see ScheduleStatus
     */
    public boolean updateACVSchedulerId(String vehicleId,
                                        String schedulerKey,
                                        String scheduleId,
                                        String roType,
                                        ScheduleStatus status) {
        Updates updates = new Updates();
        updates.addFieldSet(Constants.RO_SCHEDULE_ID, scheduleId);
        updates.addFieldSet(Constants.RO_UPDATE_TIME, System.currentTimeMillis());
        if (ObjectUtils.isNotEmpty(status)) {
            updates.addFieldSet(Constants.RO_SCHEDULE_STATUS, status);
        }
        IgniteCriteriaGroup igniteCriteriaGroup = new IgniteCriteriaGroup(
                new IgniteCriteria(Constants.RO_VEHICLE_ID, Operator.EQ, vehicleId))
                .and(new IgniteCriteria(Constants.RO_SCHEDULE_KEY, Operator.EQ, schedulerKey))
                .and(new IgniteCriteria(Constants.RO_SCHEDULE_TYPE, Operator.EQ, roType));
        IgniteQuery baseIgniteQuery = new IgniteQuery(igniteCriteriaGroup);
        return update(baseIgniteQuery, updates);
    }

    /**
     * Update scheduleId, scheduleTs and status for the given vehicleId, schedulerKey and roType.
     *
     * @param vehicleId    vehicleId
     * @param schedulerKey schedulerKey
     * @param scheduleId   scheduleId - to be updated
     * @param roType       roType
     * @param status       status
     * @param departureTs  departureTs
     * @return boolean - if update is success
     */
    public boolean updateACVSchedulerId(String vehicleId,
                                        String schedulerKey,
                                        String scheduleId,
                                        String roType,
                                        ScheduleStatus status,
                                        long departureTs) {
        Updates updates = new Updates();
        updates.addFieldSet(Constants.RO_SCHEDULE_ID, scheduleId);
        updates.addFieldSet(Constants.RO_SCHEDULE_TS, departureTs);
        updates.addFieldSet(Constants.RO_UPDATE_TIME, System.currentTimeMillis());

        if (ObjectUtils.isNotEmpty(status)) {
            updates.addFieldSet(Constants.RO_SCHEDULE_STATUS, status);
        }

        IgniteCriteriaGroup igniteCriteriaGroup = new IgniteCriteriaGroup(
                new IgniteCriteria(Constants.RO_VEHICLE_ID, Operator.EQ, vehicleId))
                .and(new IgniteCriteria(Constants.RO_SCHEDULE_KEY, Operator.EQ, schedulerKey))
                .and(new IgniteCriteria(Constants.RO_SCHEDULE_TYPE, Operator.EQ, roType));
        IgniteQuery baseIgniteQuery = new IgniteQuery(igniteCriteriaGroup);

        return update(baseIgniteQuery, updates);
    }

    /**
     * Update state as INACTIVE for the given vehicleId and scheduleId.
     *
     * @param vehicleId  vehicleId
     * @param scheduleId scheduleId - to be updated
     * @return boolean - if update is success
     */
    public boolean updateStateInactive(String vehicleId, String scheduleId) {
        IgniteCriteriaGroup igniteCriteriaGroup = new IgniteCriteriaGroup(
                new IgniteCriteria(Constants.RO_VEHICLE_ID, Operator.EQ, vehicleId))
                .and(new IgniteCriteria(Constants.RO_SCHEDULE_ID, Operator.EQ, scheduleId));
        IgniteQuery baseIgniteQuery = new IgniteQuery(igniteCriteriaGroup);
        Updates updates = new Updates();
        updates.addFieldSet(Constants.RO_SCHEDULE_STATUS, ScheduleStatus.DELETED);
        return update(baseIgniteQuery, updates);
    }

    /**
     * Update schedulerStatus for the given vehicleId, schedulerKey and roType.
     *
     * @param vehicleId    vehicleId
     * @param schedulerKey schedulerKey
     * @param roType       roType
     * @param status       status - status to be updated
     * @return boolean - if update is success
     */
    public boolean updateSchedulerStatus(String vehicleId, String schedulerKey, String roType, ScheduleStatus status) {
        IgniteCriteriaGroup igniteCriteriaGroup = new IgniteCriteriaGroup(
                new IgniteCriteria(Constants.RO_VEHICLE_ID, Operator.EQ, vehicleId))
                .and(new IgniteCriteria(Constants.RO_SCHEDULE_KEY, Operator.EQ, schedulerKey))
                .and(new IgniteCriteria(Constants.RO_SCHEDULE_TYPE, Operator.EQ, roType));
        IgniteQuery baseIgniteQuery = new IgniteQuery(igniteCriteriaGroup);
        Updates updates = new Updates();
        updates.addFieldSet(Constants.RO_SCHEDULE_STATUS, status);
        updates.addFieldSet(Constants.RO_UPDATE_TIME, System.currentTimeMillis());
        return update(baseIgniteQuery, updates);
    }

    /**
     * Delete RoSchedule for the given vehicleId.
     *
     * @param vehicleId vehicleId
     * @return boolean - if delete is success
     */
    public boolean deleteByVehicleId(String vehicleId) {
        IgniteCriteriaGroup igniteCriteriaGroup = new IgniteCriteriaGroup(
                new IgniteCriteria(Constants.RO_VEHICLE_ID, Operator.EQ, vehicleId));
        IgniteQuery baseIgniteQuery = new IgniteQuery(igniteCriteriaGroup);

        if (deleteByQuery(baseIgniteQuery) > 0) {
            return true;
        }
        return false;
    }

    /**
     * Get TimeZoneId by vehicleId and roType.
     *
     * @param vehicleId vehicle ID
     * @param roType    RO Type
     * @return {@link ZoneId} zoneId object
     * @see ZoneId
     */
    public ZoneId getTimeZoneIdByVehicleId(String vehicleId, String roType) {
        IgniteCriteriaGroup igniteCriteriaGroup = new IgniteCriteriaGroup(
                new IgniteCriteria(Constants.RO_VEHICLE_ID, Operator.EQ, vehicleId))
                .and(new IgniteCriteria(Constants.RO_SCHEDULE_TYPE, Operator.EQ, roType));
        IgniteQuery baseIgniteQuery = new IgniteQuery(igniteCriteriaGroup)
                .orderBy(new IgniteOrderBy().byfield(Constants.RO_UPDATE_TIME).desc());
        baseIgniteQuery.setPageNumber(1);
        baseIgniteQuery.setPageSize(1);
        List<RoScheduleV2> roScheduleV2s = find(baseIgniteQuery);
        if (ObjectUtils.isEmpty(roScheduleV2s)) {
            return null;
        }
        RoScheduleV2 roScheduleV2 = roScheduleV2s.get(0);
        if (ObjectUtils.isEmpty(roScheduleV2.getZoneId())) {
            return null;
        }
        return ZoneId.of(roScheduleV2.getZoneId());
    }

    /**
     * Get TimeZoneId by vehicleId, scheduleId and roType.
     *
     * @param vehicleId   vehicle ID
     * @param schedulerId schedule Id
     * @param roType      RO Type
     * @return {@link ZoneId} zoneId object
     * @see ZoneId
     */
    public ZoneId getTimeZoneByScheduleId(String vehicleId, String schedulerId, String roType) {
        IgniteCriteriaGroup igniteCriteriaGroup = new IgniteCriteriaGroup(
                new IgniteCriteria(Constants.RO_VEHICLE_ID, Operator.EQ, vehicleId))
                .and(new IgniteCriteria(Constants.RO_SCHEDULE_ID, Operator.EQ, schedulerId))
                .and(new IgniteCriteria(Constants.RO_SCHEDULE_TYPE, Operator.EQ, roType));
        IgniteQuery baseIgniteQuery = new IgniteQuery(igniteCriteriaGroup)
                .orderBy(new IgniteOrderBy().byfield(Constants.RO_UPDATE_TIME).desc());
        baseIgniteQuery.setPageNumber(1);
        baseIgniteQuery.setPageSize(1);
        List<RoScheduleV2> roScheduleV2s = find(baseIgniteQuery);
        if (ObjectUtils.isEmpty(roScheduleV2s)) {
            return null;
        }
        RoScheduleV2 roScheduleV2 = roScheduleV2s.get(0);
        if (ObjectUtils.isEmpty(roScheduleV2.getZoneId())) {
            return null;
        }
        return ZoneId.of(roScheduleV2.getZoneId());
    }
}
