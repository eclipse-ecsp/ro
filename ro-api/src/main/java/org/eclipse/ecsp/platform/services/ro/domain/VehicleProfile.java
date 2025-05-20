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

package org.eclipse.ecsp.platform.services.ro.domain;

import dev.morphia.annotations.Id;
import org.eclipse.ecsp.domain.AuthorizedPartner;
import org.eclipse.ecsp.domain.Ecu;
import org.eclipse.ecsp.domain.Event;
import org.eclipse.ecsp.domain.ModemInfo;
import org.eclipse.ecsp.domain.SaleAttributes;
import org.eclipse.ecsp.domain.User;
import org.eclipse.ecsp.domain.VehicleAttributes;
import org.eclipse.ecsp.domain.VehicleCapabilities;
import org.eclipse.ecsp.domain.Version;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the vehicle profile with various attributes and metadata. *
 *
 * @author suyasrivasta
 */
public class VehicleProfile {

    private Version schemaVersion = Version.V1_0;

    private String vin;
    @Id
    private String vehicleId;
    private Date createdOn;
    private Date updatedOn;
    private Date productionDate;
    private String soldRegion;
    private String saleDate;
    private VehicleAttributes vehicleAttributes;
    private List<User> authorizedUsers;
    private ModemInfo modemInfo;
    private String vehicleArchType;
    private Map<String, ? extends Ecu> ecus;
    private Boolean dummy;
    private Map<String, Event> events;
    private Map<String, Map<String, String>> customParams;
    private VehicleCapabilities vehicleCapabilities;
    private SaleAttributes saleAttributes;
    private Boolean eolValidationInProgress;
    private Boolean blockEnrollment;
    private Map<String, ? extends AuthorizedPartner> authorizedPartners;
    private String ePIDDBChecksum;
    private String connectedPlatform;
    private LocalDateTime lastUpdatedTime;


    /**
     * Represents the vehicle profile with various attributes and metadata. *
     *
     * @author suyasrivasta
     */
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((authorizedPartners == null) ? 0 : authorizedPartners.hashCode());
        result = prime * result + ((authorizedUsers == null) ? 0 : authorizedUsers.hashCode());
        result = prime * result + ((blockEnrollment == null) ? 0 : blockEnrollment.hashCode());
        result = prime * result + ((createdOn == null) ? 0 : createdOn.hashCode());
        result = prime * result + ((customParams == null) ? 0 : customParams.hashCode());
        result = prime * result + ((dummy == null) ? 0 : dummy.hashCode());
        result = prime * result + ((ecus == null) ? 0 : ecus.hashCode());
        result = prime * result + ((eolValidationInProgress == null) ? 0 : eolValidationInProgress.hashCode());
        result = prime * result + ((events == null) ? 0 : events.hashCode());
        result = prime * result + ((modemInfo == null) ? 0 : modemInfo.hashCode());
        result = prime * result + ((productionDate == null) ? 0 : productionDate.hashCode());
        result = prime * result + ((saleAttributes == null) ? 0 : saleAttributes.hashCode());
        result = prime * result + ((saleDate == null) ? 0 : saleDate.hashCode());
        result = prime * result + ((schemaVersion == null) ? 0 : schemaVersion.hashCode());
        result = prime * result + ((soldRegion == null) ? 0 : soldRegion.hashCode());
        result = prime * result + ((updatedOn == null) ? 0 : updatedOn.hashCode());
        result = prime * result + ((vehicleArchType == null) ? 0 : vehicleArchType.hashCode());
        result = prime * result + ((vehicleAttributes == null) ? 0 : vehicleAttributes.hashCode());
        result = prime * result + ((vehicleCapabilities == null) ? 0 : vehicleCapabilities.hashCode());
        result = prime * result + ((vehicleId == null) ? 0 : vehicleId.hashCode());
        result = prime * result + ((vin == null) ? 0 : vin.hashCode());
        result = prime * result + ((connectedPlatform == null) ? 0 : connectedPlatform.hashCode());
        result = prime * result + ((lastUpdatedTime == null) ? 0 : lastUpdatedTime.hashCode());
        return result;
    }

    /** equals.
     * the authorizedUsers to set.*/
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        VehicleProfile other = (VehicleProfile) obj;
        return Objects.equals(schemaVersion, other.schemaVersion)
                && Objects.equals(vin, other.vin)
                && Objects.equals(vehicleId, other.vehicleId)
                && Objects.equals(createdOn, other.createdOn)
                && Objects.equals(updatedOn, other.updatedOn)
                && Objects.equals(productionDate, other.productionDate)
                && Objects.equals(soldRegion, other.soldRegion)
                && Objects.equals(saleDate, other.saleDate)
                && Objects.equals(vehicleAttributes, other.vehicleAttributes)
                && Objects.equals(authorizedUsers, other.authorizedUsers)
                && Objects.equals(modemInfo, other.modemInfo)
                && Objects.equals(vehicleArchType, other.vehicleArchType)
                && Objects.equals(ecus, other.ecus)
                && Objects.equals(dummy, other.dummy)
                && Objects.equals(events, other.events)
                && Objects.equals(customParams, other.customParams)
                && Objects.equals(vehicleCapabilities, other.vehicleCapabilities)
                && Objects.equals(saleAttributes, other.saleAttributes)
                && Objects.equals(eolValidationInProgress, other.eolValidationInProgress)
                && Objects.equals(blockEnrollment, other.blockEnrollment)
                && Objects.equals(authorizedPartners, other.authorizedPartners)
                && Objects.equals(ePIDDBChecksum, other.ePIDDBChecksum)
                && Objects.equals(connectedPlatform, other.connectedPlatform)
                && Objects.equals(lastUpdatedTime, other.lastUpdatedTime);
    }

    /** Represents the vehicle profile with various attributes and metadata. */
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    public String toString() {
        return "VehicleProfile [" + (schemaVersion != null ? "schemaVersion=" + schemaVersion + ", " : "")
                + (vin != null ? "vin=" + vin + ", " : "")
                + (ePIDDBChecksum != null ? "ePIDDBChecksum=" + ePIDDBChecksum + ", " : "")
                + (vehicleId != null ? "vehicleId=" + vehicleId + ", " : "")
                + (createdOn != null ? "createdOn=" + createdOn + ", " : "")
                + (updatedOn != null ? "updatedOn=" + updatedOn + ", " : "")
                + (productionDate != null ? "productionDate=" + productionDate + ", " : "")
                + (soldRegion != null ? "soldRegion=" + soldRegion + ", " : "")
                + (saleDate != null ? "saleDate=" + saleDate + ", " : "")
                + (vehicleAttributes != null ? "vehicleAttributes=" + vehicleAttributes + ", " : "")
                + (authorizedUsers != null ? "authorizedUsers=" + authorizedUsers + ", " : "")
                + (modemInfo != null ? "modemInfo=" + modemInfo + ", " : "")
                + (vehicleArchType != null ? "vehicleArchType=" + vehicleArchType + ", " : "")
                + (ecus != null ? "ecus=" + ecus + ", " : "") + (dummy != null ? "dummy=" + dummy + ", " : "")
                + (events != null ? "events=" + events + ", " : "")
                + (customParams != null ? "customParams=" + customParams + ", " : "")
                + (vehicleCapabilities != null ? "vehicleCapabilities=" + vehicleCapabilities + ", " : "")
                + (saleAttributes != null ? "saleAttributes=" + saleAttributes + ", " : "")
                + (eolValidationInProgress != null ? "eolValidationInProgress=" + eolValidationInProgress + ", " : "")
                + (blockEnrollment != null ? "blockEnrollment=" + blockEnrollment + ", " : "")
                + (authorizedPartners != null ? "authorizedPartners=" + authorizedPartners : "")
                + (lastUpdatedTime != null ? "lastUpdatedTime=" + lastUpdatedTime : "")
                + (connectedPlatform != null ? "connectedPlatform=" + connectedPlatform : "") + "]";
    }

}

