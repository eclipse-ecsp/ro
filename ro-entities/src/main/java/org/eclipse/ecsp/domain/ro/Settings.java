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
import org.eclipse.ecsp.entities.AuditableIgniteEntity;
import org.eclipse.ecsp.entities.IgniteEntity;
import org.eclipse.ecsp.entities.IgniteEvent;
import java.time.LocalDateTime;

/**
 * Settings entity class.
 */
@Entity(Constants.SETTINGS)
@JsonInclude(Include.NON_NULL)
public class Settings implements IgniteEntity, AuditableIgniteEntity {

    @Id
    private String settingsId;
    private Version schemaVersion;
    private IgniteEvent roSettingsEvent;
    private LocalDateTime lastUpdatedTime;

    public Version getSchemaVersion() {
        return this.schemaVersion;
    }

    public void setSchemaVersion(Version schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public IgniteEvent getRoSettingsEvent() {
        return roSettingsEvent;
    }

    public void setRoSettingsEvent(IgniteEvent roSettingsEvent) {
        this.roSettingsEvent = roSettingsEvent;
    }

    public String getSettingsId() {
        return settingsId;
    }

    public void setSettingsId(String settingsId) {
        this.settingsId = settingsId;
    }

    @Override
    public LocalDateTime getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    @Override
    public void setLastUpdatedTime(LocalDateTime lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;

    }

    @Override
    public String toString() {
        return "Settings [settingsId=" + settingsId + ", schemaVersion=" + schemaVersion + ", roSettingsEvent="
                + roSettingsEvent + ", lastUpdatedTime=" + lastUpdatedTime + "]";
    }

}
