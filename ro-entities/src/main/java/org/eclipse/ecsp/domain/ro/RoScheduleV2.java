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
import org.bson.types.ObjectId;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.entities.IgniteEntity;

/**
 * Ro Schedule version 2 entity class.
 */
@Entity(Constants.RO_SCHEDULE_V2)
@JsonInclude(Include.NON_NULL)
public class RoScheduleV2 implements IgniteEntity {

    @Id
    private ObjectId id;
    private String vehicleId;
    private Version schemaVersion;
    private String scheduleType;
    private String schedulerKey;
    private String scheduleId;
    private long scheduleTs;
    private String name;
    private ScheduleStatus status;
    private RecurrenceType recurrenceType;
    private long createdOn;
    private long updatedOn;
    private String zoneId;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    @Override
    public Version getSchemaVersion() {
        return schemaVersion;
    }

    @Override
    public void setSchemaVersion(Version schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getSchedulerKey() {
        return schedulerKey;
    }

    public void setSchedulerKey(String schedulerKey) {
        this.schedulerKey = schedulerKey;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public long getScheduleTs() {
        return scheduleTs;
    }

    public void setScheduleTs(long scheduleTs) {
        this.scheduleTs = scheduleTs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
    }

    public RecurrenceType getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(RecurrenceType recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
    }

    public long getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(long updatedOn) {
        this.updatedOn = updatedOn;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    @Override
    public String toString() {
        return "RoScheduleV2{"
                + "id=" + id
                + ", vehicleId='" + vehicleId + '\''
                + ", schemaVersion=" + schemaVersion
                + ", scheduleType='" + scheduleType + '\''
                + ", schedulerKey='" + schedulerKey + '\''
                + ", scheduleId='" + scheduleId + '\''
                + ", scheduleTs=" + scheduleTs
                + ", name='" + name + '\''
                + ", status=" + status
                + ", recurrenceType=" + recurrenceType
                + ", createdOn=" + createdOn
                + ", updatedOn=" + updatedOn
                + ", zoneId='" + zoneId + '\''
                + '}';
    }
}
