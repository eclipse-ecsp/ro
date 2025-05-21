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

package org.eclipse.ecsp.domain.remoteInhibit;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.eclipse.ecsp.entities.AbstractEventData;
import org.eclipse.ecsp.services.constants.EventAttribute;
import org.eclipse.ecsp.services.entities.EventDataVehicleAttribute;

/**
 * Remote Inhibit Data abstract representation.
 */
@JsonInclude(Include.NON_NULL)
@JsonFilter(EventAttribute.REMOTE_INHIBIT_FILTER)
public class AbstractRemoteInhibitEventData extends AbstractEventData implements EventDataVehicleAttribute {

    private static final long serialVersionUID = 7577202148331933660L;

    private String origin;
    private String roRequestId;
    private String userId;

    private String vehicleArchType;

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getRoRequestId() {
        return roRequestId;
    }

    public void setRoRequestId(String roRequestId) {
        this.roRequestId = roRequestId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getVehicleArchType() {
        return vehicleArchType;
    }

    @Override
    public void setVehicleArchType(String vehicleArchType) {
        this.vehicleArchType = vehicleArchType;
    }
}