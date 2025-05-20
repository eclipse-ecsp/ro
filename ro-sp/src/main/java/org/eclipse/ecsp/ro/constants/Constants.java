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

package org.eclipse.ecsp.ro.constants;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.eclipse.ecsp.domain.ro.RemoteOperationResponseV1_1.Response;
import org.eclipse.ecsp.entities.dma.DeviceMessageErrorCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Constants class for Remote Operation.
 */
public class Constants {

    public static final String COMMA = ",";
    public static final String UNDER_SCORE = "_";
    public static final String SINK_TOPIC_NAME_KEY = "sink.topic.name";
    public static final String SOURCE_TOPIC_NAME_KEY = "source.topic.name";
    public static final String RO_STREAM_PROCESSOR = "ro-stream-processor";
    public static final String REMOTE_OPERATION_RESPONSE_EVENT_ID = "RemoteOperationResponse";
    public static final String REMOTE_OPERATION_REQUEST_EVENT_ID = "RemoteOperationRequest";
    public static final String NOTIFICATION_MAPPING = "NOTIFICATION_MAPPING";
    public static final String UNKNOWN = "UNKNOWN";
    public static final String UPDATE_RO_NOTIFICATION_MAPPING = "UpdateRONotificationMapping";
    public static final String STOCKING_RULE_CONFIGURATIONOBJECT = "notificationMapping";
    public static final String GET_TIMEZONE = "GET_TIMEZONE";
    public static final String REMOTEOPERATIONRESPONSE = "RemoteOperationResponse";
    public static final String REMOTEOPERATIONNOTIFICATION = "RemoteOperationNotification";
    public static final String REMOTEOPERATIONNOTIFICATIONEVENT = "RemoteOperationNotificationEvent";
    public static final String RIRESPONSE = "RIResponse";
    public static final String CRANKNOTIFICATIONDATA = "CrankNotificationData";
    public static final String RCPD_RESPONSE = "RCPDResponse";
    public static final String REMOTEOPERATIONRESPONSEEVENTS = "RemoteOperationResponseEvents";
    public static final String EVENT_ID_DELETE_SCHEDULE = "RemoteOperationDeleteSchedule";
    public static final String EVENT_ID_REMOTE_INHIBIT_REQUEST = "RIRequest";
    public static final String EVENT_ID_REMOTE_INHIBIT_RESPONSE = "RIResponse";
    public static final String EVENT_ID_CRANK_NOTIFICATION_DATA = "CrankNotificationData";
    public static final String NOTIFICATION_QUALIFIER = "NOTIFICATION_QUALIFIER";
    public static final String QUALIFIER = "QUALIFIER";
    public static final String SERVICE_NAME = "ServiceName";
    public static final String RO_OBJECT_ID = "roObjectId";
    public static final String RO_REQUEST_ID = "roRequestId";
    public static final String DEFAULT_IDENTIFIER = "defaultIdentifier";
    public static final String VEHICLE_ARCHTYPE1_IDENTIFIER = "vehicle_archType1Identifier";
    public static final String REQUESTID = "RequestId";
    public static final String TIMEZONE = "Timezone";
    public static final String BIZTRANSACTIONID = "BizTransactionId";
    public static final String EVENTID = "EventID";
    public static final String VEHICLEID = "VehicleId";
    public static final String DATA = "Data";
    public static final String USERCONTEXT = "UserContext";
    public static final Map<DeviceMessageErrorCode, Response> ERROR_RESPONSE =
            ImmutableMap.of(
                    DeviceMessageErrorCode.DEVICE_DELIVERY_CUTOFF_EXCEEDED,
                    Response.FAIL_MESSAGE_DELIVERY_TIMED_OUT,
                    DeviceMessageErrorCode.DEVICE_STATUS_INACTIVE,
                    Response.FAIL_VEHICLE_NOT_CONNECTED,
                    DeviceMessageErrorCode.RETRY_ATTEMPTS_EXCEEDED,
                    Response.FAIL_DELIVERY_RETRYING
            );

    /**
     * DMA messages: <br>
     * <br>
     * FAIL_DELIVERY_RETRYING , DEVICE_STATUS_INACTIVE, FAIL_VEHICLE_NOT_CONNECTED , shoulder tap try.
     * -> Do not pick the next record from redis. <br>
     * <br>
     * Pick the next record only when RETRY_ATTEMPTS_EXCEEDED -
     * DEVICE_DELIVERY_CUTOFF_EXCEEDED(FAIL_MESSAGE_DELIVERY_TIMED_OUT )
     * SHOULDER_TAP_RETRY_ATTEMPTS_EXCEEDED
     */
    public static final List<DeviceMessageErrorCode> PICKABLE_DEVICE_MESSAGE_ERROR_CODE =
            ImmutableList.of(
                    DeviceMessageErrorCode.DEVICE_DELIVERY_CUTOFF_EXCEEDED,
                    DeviceMessageErrorCode.RETRY_ATTEMPTS_EXCEEDED,
                    DeviceMessageErrorCode.SHOULDER_TAP_RETRY_ATTEMPTS_EXCEEDED);

    public static final String GENERICNOTIFICATIONEVENT = "GenericNotificationEvent";
    public static final String REMOTEOPERATIONALARMV1_1 = "RemoteOperationAlarmV1_1";
    public static final String REMOTEOPERATIONCLIMATEV1_1 = "RemoteOperationClimateV1_1";
    public static final String REMOTEOPERATIONDOORSV1_1 = "RemoteOperationDoorsV1_1";
    public static final String REMOTEOPERATIONDRIVERDOORV1_1 = "RemoteOperationDriverDoorV1_1";
    public static final String REMOTEOPERATIONDRIVERWINDOWV1_1 = "RemoteOperationDriverWindowV1_1";
    public static final String REMOTEOPERATIONENGINEV1_1 = "RemoteOperationEngineV1_1";
    public static final String REMOTEOPERATIONHOODV1_1 = "RemoteOperationHoodV1_1";
    public static final String REMOTEOPERATIONHORNV1_1 = "RemoteOperationHornV1_1";
    public static final String REMOTEOPERATIONLIFTGATEV1_1 = "RemoteOperationLiftgateV1_1";
    public static final String REMOTEOPERATIONLIGHTSV1_1 = "RemoteOperationLightsV1_1";
    public static final String REMOTEOPERATIONLIGHTSV1_2 = "RemoteOperationLightsV1_2";
    public static final String REMOTEOPERATIONWINDOWSV1_1 = "RemoteOperationWindowsV1_1";
    public static final String REMOTEOPERATIONTRUNKV1_1 = "RemoteOperationTrunkV1_1";
    public static final String REMOTEOPERATIONGLOVEBOXV1_1 = "RemoteOperationGloveBoxV1_1";
    public static final String REMOTEOPERATIONCARGOBOXV1_1 = "RemoteOperationCargoBoxV1_1";
    public static final String REMOTEOPERATIONGLOVEBOXV2_0 = "RemoteOperationGloveBoxV2_0";
    public static final String REMOTEOPERATIONTRUNKV2_0 = "RemoteOperationTrunkV2_0";
    public static final String REMOTEOPERATIONCARGOBOXV2_0 = "RemoteOperationCargoBoxV2_0";
    public static final String REMOTEOPERATIONLIFTGATEV2_0 = "RemoteOperationLiftgateV2_0";
    public static final String REMOTEOPERATIONCLIMATEV2_0 = "RemoteOperationClimateV2_0";
    public static final String GETTIMEZONECONVERTEREVENT = "GetTimeZoneConverter";

    // Cache Constants
    public static final String RO_CACHE_NAME = "roCache";
    // ENGINE_NOT_START
    public static final String ENGINE_NOT_START = "ENGINE_NOT_START";
    // remote engine status cache key
    public static final String RO_ENGINE_STATUS_PREFIX = "RO_ENGINE_STATUS_";
    // remote queue cache key
    public static final String RO_QUEUE_PREFIX = "RO_QUEUE_";
    public static final String DMA_DEVICE_DELIVERY_CUTOFF_RESOLVER_CLASS =
            "dma.device.delivery.cutoff.resolver.class";
    public static final String PRIVACYMODEACTIVE = "ACTIVE";
    public static final String SUCCESS_SCHEDULE_CREATED = "SUCCESS_SCHEDULE_CREATED";
    public static final String FAIL_SCHEDULE_CREATED = "FAIL_SCHEDULE_CREATED";
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final Integer SIXTY = 60;
    public static final Long ONE_THOUSAND = 1000L;
    private static final List<String> RO_EVENTS = new ArrayList<>();

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get all RO Events as list.
     *
     * @return list of RO events
     */
    public static final List<String> getRoEvents() {
        RO_EVENTS.add(org.eclipse.ecsp.domain.ro.constant.Constants.REMOTEOPERATIONALARM);
        RO_EVENTS.add(org.eclipse.ecsp.domain.ro.constant.Constants.REMOTEOPERATIONCLIMATE);
        RO_EVENTS.add(org.eclipse.ecsp.domain.ro.constant.Constants.REMOTEOPERATIONDOORS);
        RO_EVENTS.add(org.eclipse.ecsp.domain.ro.constant.Constants.REMOTEOPERATIONDRIVERDOOR);
        RO_EVENTS.add(org.eclipse.ecsp.domain.ro.constant.Constants.REMOTEOPERATIONDRIVERWINDOW);
        RO_EVENTS.add(org.eclipse.ecsp.domain.ro.constant.Constants.REMOTEOPERATIONHOOD);
        RO_EVENTS.add(org.eclipse.ecsp.domain.ro.constant.Constants.REMOTEOPERATIONHORN);
        RO_EVENTS.add(org.eclipse.ecsp.domain.ro.constant.Constants.REMOTEOPERATIONLIFTGATE);
        RO_EVENTS.add(org.eclipse.ecsp.domain.ro.constant.Constants.REMOTEOPERATIONLIGHTS);
        RO_EVENTS.add(org.eclipse.ecsp.domain.ro.constant.Constants.REMOTEOPERATIONLIGHTSONLY);
        RO_EVENTS.add(org.eclipse.ecsp.domain.ro.constant.Constants.REMOTEOPERATIONSTATUS);
        RO_EVENTS.add(org.eclipse.ecsp.domain.ro.constant.Constants.REMOTEOPERATIONTRUNK);
        RO_EVENTS.add(org.eclipse.ecsp.domain.ro.constant.Constants.REMOTEOPERATIONWINDOWS);
        RO_EVENTS.add(org.eclipse.ecsp.domain.ro.constant.Constants.REMOTEOPERATIONGLOVEBOX);
        RO_EVENTS.add(org.eclipse.ecsp.domain.ro.constant.Constants.REMOTEOPERATIONCARGOBOX);
        return Collections.unmodifiableList(RO_EVENTS);
    }
}
