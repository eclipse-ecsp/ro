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
 * Constant Class for Numeric Values.
 */
public class NumericConstants {

    // integer constants
    public static final int ZERO = 0;
    public static final int MINUS_ONE = -1;
    public static final int TWO = 2;
    public static final int THREE = 3;
    public static final int FOUR = 4;
    public static final int SEVEN = 7;
    public static final int TEN = 10;
    public static final int ELEVEN = 11;
    public static final int THIRTY = 30;
    public static final int FIFTY = 50;
    public static final int SIXTY = 60;
    public static final int HUNDRED = 100;
    public static final int HUNDRED_TWENTY = 120;
    public static final int SUCCESS_RESPONSE = 200;
    public static final int THRESHOLD = 100000;

    // Timestamp constants
    public static final long CREATED_ON = 1234567891L;
    public static final long RESP_LIST_TIME_STAMP = 42323242342344L;
    public static final long SCHEDULE_TS = 114732583L;
    public static final long TIME_STAMP = 1555766166000L;
    public static final long UPDATED_ON = 1234567890L;

    // floating point constants
    public static final double ALTITUDE = 12.45;
    public static final double LATITUDE = 23.45;
    public static final double LONGITUDE = 45.67;

    // Timeouts
    public static final int ALARM_STATUS_NTF_INTERVAL = 232;
    public static final int ALARM_EXEC_TIMEOUT = 4444;
    public static final int CLIMATE_EXEC_TIMEOUT = 123;
    public static final int CLIMATE_STATUS_NTF_INTERVAL = 212;
    public static final int DRIVER_DOOR_EXEC_TIMEOUT = 323;
    public static final int DRIVER_WINDOW_EXEC_TIMEOUT = 222;
    public static final int ENGINE_EXEC_TIMEOUT = 123322;
    public static final int LIFT_GATE_EXEC_TIMEOUT = 324;
    public static final int LIGHTS_EXEC_TIMEOUT = 223;
    public static final int REMOTE_CLIMATE_TIMEOUT = 132;
    public static final int REMOTE_ENGINE_EXECUTION = 3322;
    public static final int WINDOWS_EXEC_TIMEOUT = 324;
    public static final int WINDOWS_STATUS_NTF_INTERVAL = 322;

    private NumericConstants() {
    }

}
