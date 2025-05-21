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

import org.eclipse.ecsp.domain.ro.RoSchedule;
import org.eclipse.ecsp.domain.ro.ScheduleDto;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.eclipse.ecsp.ro.constants.Constants;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import java.util.Iterator;
import java.util.List;

/**
 * DAO Layer for RoSchedule object.
 */
@Repository
public class RoScheduleDAOMongoImpl extends IgniteBaseDAOMongoImpl<String, RoSchedule> {

    /**
     * Update schedulerId for the given vehicleId, schedulerKey and roType with the provided
     * scheduleId.
     *
     * @param vehicleId    vehicleId
     * @param schedulerKey schedulerKey
     * @param scheduleId   scheduleId - to be updated
     * @param roType       roType
     * @return boolean - if update is success
     */
    public boolean updateSchedulerId(
            String vehicleId, String schedulerKey, String scheduleId, String roType) {
        boolean isUpdated = false;
        RoSchedule roSchedules = this.findById(vehicleId);

        if (roSchedules != null && !CollectionUtils.isEmpty(roSchedules.getSchedules())) {

            List<ScheduleDto> schedules = roSchedules.getSchedules().get(roType);

            if (!CollectionUtils.isEmpty(schedules)) {
                schedules.stream()
                        .forEach(
                                x -> {
                                    if (schedulerKey.equalsIgnoreCase(x.getSchedulerKey())) {
                                        x.setScheduleId(scheduleId);
                                    }
                                });
            }
            isUpdated = update(roSchedules);
        }
        return isUpdated;
    }

    /**
     * Delete RoSchedule for the given vehicleId and schedulerKey.
     *
     * @param vehicleId    vehicleId
     * @param schedulerKey schedulerKey
     * @return boolean - if delete is success
     */
    public boolean deleteRoSchedule(String vehicleId, String schedulerKey) {
        RoSchedule roSchedules = this.findById(vehicleId);
        boolean isUpdated = false;
        if (roSchedules != null && !CollectionUtils.isEmpty(roSchedules.getSchedules())) {
            for (String roType : Constants.getRoEvents()) {
                isUpdated = updateROSchedule(roSchedules, schedulerKey, roType);
            }
        }
        return isUpdated;
    }

    /**
     * If schedules contain the roType schedule, then update the RoSchedule where schedulerKey does
     * not match.
     *
     * @param roSchedules  RoSchedule
     * @param schedulerKey schedulerKey
     * @param roType       roType
     * @return boolean - if update is success
     */
    Boolean updateROSchedule(RoSchedule roSchedules, String schedulerKey, String roType) {
        List<ScheduleDto> schedules = roSchedules.getSchedules().get(roType);
        if (!CollectionUtils.isEmpty(schedules)) {
            Iterator<ScheduleDto> iterator = schedules.iterator();
            while (iterator.hasNext()) {
                ScheduleDto scheduleDto = iterator.next();
                if (schedulerKey.equalsIgnoreCase(scheduleDto.getScheduleId())) {
                    iterator.remove();
                }
            }
        }
        return update(roSchedules);
    }

    /**
     * Get scheduleId for the given vehicleId, schedulerKey and roType.
     *
     * @param vehicleId    vehicleId
     * @param schedulerKey schedulerKey
     * @param roType       roType
     * @return scheduleId
     */
    public String getScheduleId(String vehicleId, String schedulerKey, String roType) {
        String id = null;
        RoSchedule roSchedules = this.findById(vehicleId);
        if (roSchedules != null && !CollectionUtils.isEmpty(roSchedules.getSchedules())) {
            List<ScheduleDto> schedules = roSchedules.getSchedules().get(roType);
            if (!CollectionUtils.isEmpty(schedules)) {
                for (ScheduleDto x : schedules) {
                    if (null != schedulerKey && schedulerKey.equalsIgnoreCase(x.getSchedulerKey())) {
                        id = x.getScheduleId();
                    }
                }
            }
        }
        return id;
    }
}
