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

package org.eclipse.ecsp.platform.services.ro.constant;

/**
 * Constant class for RO Response Messages.
 *
 * @author Neerajkumar
 */
public class ResponseMsgConstants {

    private ResponseMsgConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final String CONTENT_TYPE = "content-type";
    public static final String HISTORY_NOT_FOUND = "HISTORY_NOT_FOUND";
    public static final String INVALID_DEPARTURE_TIME = "INVALID_DEPARTURE_TIME";
    public static final String INVALID_DEPARTURE_TIME_MESSAGE = "Departure time must be format yyyy/MM/dd HH:mm:ss";
    public static final String INVALID_SCHEDULE_KEY = "INVALID_SCHEDULER_KEY";
    public static final String INVALID_SCHEDULE_KEY_MESSAGE =
            """
                    Combination of vehicleId and Scheduler "
                    key is either invalid or not in active state
                    """;
    public static final String INVALID_SCHEDULE_TIME = "INVALID_SCHEDULE_TIME";
    public static final String INVALID_SCHEDULE_TIME_MESSAGE = "Schedule time cannot be less than current time.";
    public static final String MULTIPLE_REQUEST_NOT_ALLOWED = "MULTIPLE_REQUEST_NOT_ALLOWED";
    public static final String MULTIPLE_REQUEST_NOT_ALLOWED_MESSAGE = "RCPD request already received.";
    public static final String NO_HISTORY_DATA_MESSAGE = "No history data";
    public static final String NO_SCHEDULE_DATA_MESSAGE = "Schedule data not found";
    public static final String NO_STATUS_DATA_MESSAGE = "No status data";
    public static final String NULL_VEHICLE_PROFILE_DATA = "VehicleProfile does not exist for the vehicle";
    public static final String RCPD_COMMAND_SUCCESS = "RCPD Command Sent Successfully";
    public static final String RI_COMMAND_SUCCESS = "RI Command Sent Successfully";
    public static final String RO_COMMAND_FAIL = "Failed to receive RO Command.";
    public static final String RO_COMMAND_SUCCESS = "RO Command Sent Successfully";
    public static final String RO_SCHEDULE_DELETE_FAIL = "Failed to Delete RO Schedule.";
    public static final String RO_SCHEDULE_DELETE_SUCCESS = "RO Schedule delete Successfully";
    public static final String SCHEDULE_NOT_FOUND = "SCHEDULE_NOT_FOUND";
    public static final String STATUS_NOT_FOUND = "STATUS_NOT_FOUND";
}
