/*
 *
 * ******************************************************************************
 *
 *  Copyright (c) 2023-24 Harman International
 *
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"),
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

package org.eclipse.ecsp.platform.services.ro.constant;

import lombok.Getter;

/**
 * enum class {@link EventIdConstants} containing event Ids.
 */
@Getter
public enum EventIdConstants {

    /*
     * Remote Operation Event IDs
     */
    EVENT_ID_ALARM("RemoteOperationAlarm"),
    EVENT_ID_CLIMATE("RemoteOperationClimate"),
    EVENT_ID_DELETE_SCHEDULE("RemoteOperationDeleteSchedule"),
    EVENT_ID_DOORS("RemoteOperationDoors"),
    EVENT_ID_DRIVER_DOOR("RemoteOperationDriverDoor"),
    EVENT_ID_DRIVER_WINDOW("RemoteOperationDriverWindow"),
    EVENT_ID_ENGINE("RemoteOperationEngine"),
    EVENT_ID_GLOVEBOX("RemoteOperationGloveBox"),
    EVENT_ID_HOOD("RemoteOperationHood"),
    EVENT_ID_HORN("RemoteOperationHorn"),
    EVENT_ID_LIFTGATE("RemoteOperationLiftgate"),
    EVENT_ID_LIGHTS("RemoteOperationLights"),
    EVENT_ID_LIGHTS_ONLY("RemoteOperationLightsOnly"),
    EVENT_ID_TRUNK("RemoteOperationTrunk"),
    EVENT_ID_WINDOWS("RemoteOperationWindows"),

    /*
     * RI Event IDs
     */

    ALERTS("alerts"),
    EVENTS("events"),
    EVENT_ID_REMOTE_INHIBIT_REQUEST("RIRequest"),
    EVENT_ID_REMOTE_INHIBIT_RESPONSE("RIResponse"),
    ROREQUESTID("roRequestId"),
    TOTAL_RECORDS("totalRecords"),
    VEHICLE_ARCH_TYPE("vehicleArchType");

    private final String value;

    EventIdConstants(String value) {
        this.value = value;
    }

}
