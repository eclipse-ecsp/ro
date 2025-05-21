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

package org.eclipse.ecsp.domain.ro.constant;

/**
 * Constants.
 */
public class Constants {

    public static final String HYPHEN = "-";

    // RO Response Status Constants
    public static final String SUCCESS = "SUCCESS";
    public static final String SUCCESS_CONTINUE = "SUCCESS_CONTINUE";
    public static final String FAILURE = "FAILURE";
    public static final String FAIL = "FAIL";

    // RCPD response status constants
    public static final String RCPD_RESPONSE_NOT_AVAILABLE = "RESPONSE_NOT_AVAILABLE";
    public static final String USUAL_FAILURE = "USUAL_FAILURE";
    public static final String TIME_OUT = "TIME_OUT";
    public static final String MOVING_VEHICLE = "MOVING_VEHICLE";
    public static final String CUSTOM_EXTENSION = "CUSTOM_EXTENSION";
    public static final String FAIL_MESSAGE_DELIVERY_TIMED_OUT = "FAIL_MESSAGE_DELIVERY_TIMED_OUT";
    public static final String FAIL_VEHICLE_NOT_CONNECTED = "FAIL_VEHICLE_NOT_CONNECTED";
    public static final String FAIL_DELIVERY_RETRYING = "FAIL_DELIVERY_RETRYING";
    public static final String RETRYING_DEVICE_DELIVERY_MESSAGE = "RETRYING_DEVICE_DELIVERY_MESSAGE";

    // Schedule Constants
    public static final String SUCCESS_SCHEDULE_CREATED = "SUCCESS_SCHEDULE_CREATED";
    public static final String SUCCESS_SCHEDULE_DELETED = "SUCCESS_SCHEDULE_DELETED";
    public static final String SUCCESS_SCHEDULE_UPDATED = "SUCCESS_SCHEDULE_UPDATED";
    public static final String FAIL_SCHEDULE_CREATED = "FAIL_SCHEDULE_CREATED";
    public static final String FAIL_SCHEDULE_DELETED = "FAIL_SCHEDULE_DELETED";
    public static final String FAIL_SCHEDULE_UPDATED = "FAIL_SCHEDULE_UPDATED";

    // RO Window
    public static final String OPENED = "OPENED";
    public static final String CLOSED = "CLOSED";
    public static final String PARTIAL_OPENED = "PARTIAL_OPENED";

    // RO Engine
    public static final String STARTED = "STARTED";
    public static final String STOPPED = "STOPPED";
    public static final String SLOW_MOVING = "SLOW_MOVING";
    public static final String IGNITION_DISABLED = "IGNITION_DISABLED";
    public static final String IGNITION_ENABLED = "IGNITION_ENABLED";

    // RO Door
    public static final String LOCKED = "LOCKED";
    public static final String UNLOCKED = "UNLOCKED";
    public static final String LOCK_TRUNK = "LOCK_TRUNK";
    public static final String UNLOCK_TRUNK = "UNLOCK_TRUNK";
    public static final String LOCK_GLOVE_BOX = "LOCK_GLOVE_BOX";
    public static final String UNLOCK_GLOVE_BOX = "UNLOCK_GLOVE_BOX";

    // RO Climate, Alarm
    public static final String ON = "ON";
    public static final String OFF = "OFF";
    public static final String AUTO = "AUTO";
    public static final String COMFORT_AUTO_ON_MODE_START_ALL = "COMFORT_AUTO_ON_MODE_START_ALL";
    public static final String COMFORT_AUTO_ON_MODE_DISABLED = "COMFORT_AUTO_ON_MODE_DISABLED";
    public static final String DEFAULT_ORIGIN = "THIRDPARTY2";

    // Entities
    public static final String RO = "ro";
    public static final String RO_SCHEDULE = "roSchedule";
    public static final String RO_SCHEDULE_V2 = "roScheduleV2";
    public static final String SETTINGS = "settings";
    public static final String RCPD = "rcpd";

    // RemoteOperationEvents/RemoteCommand/RemoteConfig
    public static final String REMOTEOPERATIONCLIMATE = "RemoteOperationClimate";
    public static final String REMOTEOPERATIONENGINE = "RemoteOperationEngine";
    public static final String REMOTEOPERATIONDELETESCHEDULE = "RemoteOperationDeleteSchedule";
    public static final String REMOTEOPERATIONLIGHTS = "RemoteOperationLights";
    public static final String REMOTEOPERATIONLIGHTSONLY = "RemoteOperationLightsOnly";
    public static final String REMOTEOPERATIONHORN = "RemoteOperationHorn";
    public static final String REMOTEOPERATIONALARM = "RemoteOperationAlarm";
    public static final String REMOTEOPERATIONDOORS = "RemoteOperationDoors";
    public static final String REMOTEOPERATIONDRIVERDOOR = "RemoteOperationDriverDoor";
    public static final String REMOTEOPERATIONHOOD = "RemoteOperationHood";
    public static final String REMOTEOPERATIONLIFTGATE = "RemoteOperationLiftgate";
    public static final String REMOTEOPERATIONTRUNK = "RemoteOperationTrunk";
    public static final String REMOTEOPERATIONWINDOWS = "RemoteOperationWindows";
    public static final String REMOTEOPERATIONDRIVERWINDOW = "RemoteOperationDriverWindow";
    public static final String REMOTEOPERATIONRESPONSE = "RemoteOperationResponse";
    public static final String REMOTEOPERATIONSTATUS = "RemoteOperationStatus";
    public static final String RCPDREQUEST = "RCPDRequest";
    public static final String RCPDRESPONSE = "RCPDResponse";

    // Remote Inhibit
    public static final String EVENT_ID_REMOTE_INHIBIT_REQUEST = "RIRequest";
    public static final String EVENT_ID_REMOTE_INHIBIT_RESPONSE = "RIResponse";
    public static final String EVENT_ID_CRANK_NOTIFICATION_DATA = "CrankNotificationData";
    public static final String REMOTE_INHIBIT = "remoteInhibit";

    // Entity Attributes
    public static final String RO_RESPONSE_LIST = "roResponseList";
    public static final String DEVICE_MESSAGE_FAILURES = "deviceMessageFailures";

    //RemoteOperationNotification
    public static final String RO_NOTIFICATION_LIST = "roNotificationList";
    public static final String DEVICE_MESSAGE_FAILURES_FILTER = "deviceMessageFailuresFilter";
    public static final String RO_VEHICLE_ARCH_TYPE = "archType";
    public static final String RO_VEHICLE_ECU_TYPE = "ecuType";
    public static final String RO_REQUEST_ORIGIN = "origin";
    public static final String RO_REQUEST_USERID = "userId";
    public static final String RO_REQUEST_STATE = "state";
    public static final String RO_REQUEST_EVENTID = "EventID";
    public static final String RCPD_REQUEST_ORIGIN = "origin";
    public static final String RCPD_REQUEST_USERID = "userId";
    public static final String RCPD_RESPONSE_LIST = "rcpdResponseList";
    public static final String RCPD_REQUEST_SCHEDULEID = "scheduleRequestId";
    public static final String TIME_STAMP = "timestamp";
    public static final String VEHICLE_STATUS = "vehicleStatus";
    public static final String PENDING = "PENDING";
    public static final String OFFBOARD_STATUS = "offBoardStatus";
    public static final String RO_STATUS = "roStatus";
    public static final String RO_VEHICLE_ID = "vehicleId";
    public static final String RO_SCHEDULE_ID = "scheduleId";
    public static final String RO_SCHEDULE_TS = "scheduleTs";
    public static final String RO_SCHEDULE_KEY = "schedulerKey";
    public static final String RO_SCHEDULE_TYPE = "scheduleType";
    public static final String RO_UPDATE_TIME = "updatedOn";
    public static final String RO_SCHEDULE_STATUS = "status";
    public static final String RO_FILTER_FIRSTSCHEDULETS = "firstScheduleTs";
    public static final String RO_FILTER_DEPARTURETS = "departureTs";
    public static final String RO_FILTER_SCHEDULEKEY = "scheduleKey";

    // R0 2.0
    public static final String REMOTEOPERATIONGLOVEBOX = "RemoteOperationGloveBox";
    public static final String REMOTEOPERATIONNOTIFICATION = "RemoteOperationNotification";
    public static final String REMOTEOPERATIONNOTIFICATIONEVENT = "RemoteOperationNotificationEvent";
    public static final String REMOTEOPERATIONCARGOBOX = "RemoteOperationCargoBox";
    public static final String FAILED = "FAILED";

    // ServiceName
    public static final String RCPD_SERVICE = "RCPD";
    public static final String RCPD_STATUS = "STATUS";

    // Other Constants
    public static final int ZERO = 0;
    public static final int THIRTY_ONE = 31;

    private Constants() {
        throw new UnsupportedOperationException("cannot create an instance of Constants class");
    }

    // redis cache key is the combination of serviceName, vehicleId and key separated by colon
    public static String getRedisKey(String serviceName, String vehicleId, String key) {
        return serviceName + ":" + vehicleId + ":" + key;
    }

}
