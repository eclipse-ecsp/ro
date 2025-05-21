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

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.ecsp.entities.AbstractEventData;
import org.eclipse.ecsp.services.constants.EventAttribute;
import org.eclipse.ecsp.services.entities.EventDataVehicleAttribute;

/**
 * Abstract RO event representation.
 */
@JsonInclude(Include.NON_NULL)
@JsonFilter(EventAttribute.RO_RESPONSE_FILTER)
public class AbstractRoEventData extends AbstractEventData implements EventDataVehicleAttribute {

    private static final long serialVersionUID = -4542583862055567881L;

    private String origin;

    private String userId;

    private Schedule schedule;

    private String vehicleArchType;

    @JsonProperty(value = EventAttribute.PARTNER_ID)
    private String partnerId;

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    @Override
    public String getVehicleArchType() {
        return vehicleArchType;
    }

    @Override
    public void setVehicleArchType(String type) {
        this.vehicleArchType = type;
    }
}
