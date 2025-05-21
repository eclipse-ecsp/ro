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

package org.eclipse.ecsp.domain.ro;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.entities.IgniteEntity;
import java.util.List;
import java.util.Map;

/**
 * RoSchedule Entity class.
 */
@Entity(Constants.RO_SCHEDULE)
@JsonInclude(Include.NON_NULL)
public class RoSchedule implements IgniteEntity {

    @Id
    private String vehicleId;
    private Version schemaVersion;
    private Map<String, List<ScheduleDto>> schedules;

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    @Override
    public Version getSchemaVersion() {
        return this.schemaVersion;
    }

    @Override
    public void setSchemaVersion(Version schemaVersion) {
        this.schemaVersion = schemaVersion;

    }

    public Map<String, List<ScheduleDto>> getSchedules() {
        return schedules;
    }

    public void setSchedules(Map<String, List<ScheduleDto>> schedules) {
        this.schedules = schedules;
    }

}
