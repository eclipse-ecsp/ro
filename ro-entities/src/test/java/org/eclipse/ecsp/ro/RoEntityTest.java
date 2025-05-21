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

package org.eclipse.ecsp.ro;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.remoteInhibit.CrankNotificationDataV1_0;
import org.eclipse.ecsp.domain.ro.AbstractRoEventData;
import org.eclipse.ecsp.domain.ro.RecurrenceType;
import org.eclipse.ecsp.domain.ro.RemoteOperationAlarmV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationClimateV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationClimateV2_0;
import org.eclipse.ecsp.domain.ro.RemoteOperationDoorsV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationDriverDoorV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationDriverWindowV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationEngineV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationGloveBoxV2_0;
import org.eclipse.ecsp.domain.ro.RemoteOperationHoodV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationHornV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationLiftgateV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationLiftgateV2_0;
import org.eclipse.ecsp.domain.ro.RemoteOperationLightsV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationLightsV1_2;
import org.eclipse.ecsp.domain.ro.RemoteOperationResponseV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationResponseV1_1.Response;
import org.eclipse.ecsp.domain.ro.RemoteOperationScheduleV1;
import org.eclipse.ecsp.domain.ro.RemoteOperationStatusV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationStatusV1_1.Status;
import org.eclipse.ecsp.domain.ro.RemoteOperationTrunkV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationTrunkV2_0;
import org.eclipse.ecsp.domain.ro.RemoteOperationType;
import org.eclipse.ecsp.domain.ro.RemoteOperationWindowsV1_1;
import org.eclipse.ecsp.domain.ro.Ro;
import org.eclipse.ecsp.domain.ro.RoSchedule;
import org.eclipse.ecsp.domain.ro.Schedule;
import org.eclipse.ecsp.domain.ro.ScheduleDto;
import org.eclipse.ecsp.domain.ro.ScheduleStatus;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.domain.ro.constant.TestConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test class for RoEntity.
 *
 * @author asingh
 */
class RoEntityTest {

    private ObjectMapper mapper;

    /**
     * setup().
     */
    @BeforeEach
    public void setup() {
        mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        mapper.enableDefaultTyping();
    }

    /**
     * method to test RODoor Event.
     */
    @Test
    void testRoDoorEvent() throws IOException {
        String requestJson = IOUtils.toString(RoEntityTest.class.getResourceAsStream("/remoteStatusRequest_1.json"),
                "UTF-8");
        Ro roEntity = mapper.readValue(requestJson, Ro.class);
        Assertions.assertNotNull(roEntity.toString());
        Assertions.assertEquals(Constants.REMOTEOPERATIONDOORS.toString(),
                roEntity.getRoEvent().getEventId().toString());
        RemoteOperationDoorsV1_1 remoteDoorData = (RemoteOperationDoorsV1_1) roEntity.getRoEvent().getEventData();
        Assertions.assertEquals("P2KvQwet9A28bOrQgxQDcp60xhCP9O9a", remoteDoorData.getRoRequestId());
        Assertions.assertEquals(RemoteOperationDoorsV1_1.State.UNLOCKED, remoteDoorData.getState());
        Assertions.assertEquals("ROSupportOwner", remoteDoorData.getOrigin());
        Assertions.assertEquals(Version.V1_1, roEntity.getSchemaVersion());
        Assertions.assertNotNull(roEntity.getRoResponseList());
        Assertions.assertNotNull(remoteDoorData.toString());
    }

    /**
     * method to test RODoorDriver Event.
     */
    @Test
    void testRoDoorDriverEvent() throws IOException {

        String requestJson = IOUtils
                .toString(RoEntityTest.class.getResourceAsStream("/remoteHistoryRequestForDriverDoors.json"), "UTF-8");

        Ro roEntity = mapper.readValue(requestJson, Ro.class);
        Assertions.assertEquals(Constants.REMOTEOPERATIONDRIVERDOOR.toString(),
                roEntity.getRoEvent().getEventId().toString());
        RemoteOperationDriverDoorV1_1 remoteDoorDriverData = (RemoteOperationDriverDoorV1_1) roEntity.getRoEvent()
                .getEventData();
        Assertions.assertEquals("kmpxNPUwqEW69CzK7VmO98BLWpLOqSw1", remoteDoorDriverData.getRoRequestId());
        Assertions.assertEquals(RemoteOperationDriverDoorV1_1.State.UNLOCKED, remoteDoorDriverData.getState());
        Assertions.assertEquals("ROSupportOwner", remoteDoorDriverData.getOrigin());
        Assertions.assertNotNull(remoteDoorDriverData.toString());

    }

    /**
     * method to test RoEngine Event.
     */
    @Test
    void testRoEngineEvent() throws IOException {
        String requestJson = IOUtils
                .toString(RoEntityTest.class.getResourceAsStream("/remoteStatusEngineRequest_1.json"), "UTF-8");
        Ro roEntity = mapper.readValue(requestJson, Ro.class);
        Assertions.assertEquals(Constants.REMOTEOPERATIONENGINE.toString(),
                roEntity.getRoEvent().getEventId().toString());
        RemoteOperationEngineV1_1 remoteEngineData = (RemoteOperationEngineV1_1) roEntity.getRoEvent().getEventData();
        Assertions.assertEquals("kmpxNPUwqEW69CzK7VmO98BLWpLOqSw1", remoteEngineData.getRoRequestId());
        Assertions.assertEquals(RemoteOperationEngineV1_1.State.STARTED, remoteEngineData.getState());
        Assertions.assertEquals("ROSupportOwner", remoteEngineData.getOrigin());
        Assertions.assertEquals(TestConstants.SEVENTY_FIVE, remoteEngineData.getDuration());
        Assertions.assertNotNull(remoteEngineData.toString());
    }

    /**
     * method to test RoClimate Event.
     */
    @Test
    void testRoClimateEvent() throws IOException {
        String requestJson = IOUtils
                .toString(RoEntityTest.class.getResourceAsStream("/remoteStatusClimateRequest_1.json"), "UTF-8");
        Ro roEntity = mapper.readValue(requestJson, Ro.class);
        Assertions.assertEquals(Constants.REMOTEOPERATIONCLIMATE.toString(),
                roEntity.getRoEvent().getEventId().toString());
        RemoteOperationClimateV1_1 remoteClimateData = (RemoteOperationClimateV1_1) roEntity.getRoEvent()
                .getEventData();
        Assertions.assertEquals("kmpxNPUwqEW69CzK7VmO98BLWpLOqSw1", remoteClimateData.getRoRequestId());
        Assertions.assertEquals(RemoteOperationClimateV1_1.State.ON, remoteClimateData.getState());
        Assertions.assertEquals("ROSupportOwner", remoteClimateData.getOrigin());
        Assertions.assertEquals(RemoteOperationClimateV1_1.AcState.ON, remoteClimateData.getAcState());
        Assertions.assertEquals(TestConstants.SEVENTY_FIVE, remoteClimateData.getDuration());
        Assertions.assertEquals(TestConstants.NINETY, remoteClimateData.getFanSpeed());
        Assertions.assertEquals(TestConstants.SEVENTY_EIGHT, remoteClimateData.getTemperature());
        Assertions.assertEquals(RemoteOperationClimateV1_1.TemperatureUnit.CELSIUS,
                remoteClimateData.getTemperatureUnit());
        Assertions.assertNotNull(remoteClimateData.toString());
    }

    /**
     * method to test RoClimate v2 Event.
     */
    @Test
    void testRoClimateV2Event() throws IOException {
        String requestJson = IOUtils
                .toString(RoEntityTest.class.getResourceAsStream("/remoteStatusClimateRequest_2.json"), "UTF-8");
        Ro roEntity = mapper.readValue(requestJson, Ro.class);
        Assertions.assertEquals(Constants.REMOTEOPERATIONCLIMATE.toString(),
                roEntity.getRoEvent().getEventId().toString());
        RemoteOperationClimateV2_0 remoteClimateData = (RemoteOperationClimateV2_0) roEntity.getRoEvent()
                .getEventData();
        Assertions.assertEquals("kmpxNPUwqEW69CzK7VmO98BLWpLOqSw1", remoteClimateData.getRoRequestId());
        Assertions.assertEquals(RemoteOperationClimateV2_0.State.ON, remoteClimateData.getState());
        Assertions.assertEquals("ROSupportOwner", remoteClimateData.getOrigin());
        Assertions.assertEquals(RemoteOperationClimateV2_0.TemperatureUnit.Celsius,
                remoteClimateData.getTemperatureUnit());
        Assertions.assertEquals(Integer.valueOf(TestConstants.SEVENTY_FIVE), remoteClimateData.getFanSpeed());
        Assertions.assertEquals(Integer.valueOf(TestConstants.SEVENTY_EIGHT),
                remoteClimateData.getTemperature());
        Assertions.assertEquals(TestConstants.DOUBLE_24_5, remoteClimateData.getTargetTemperature());
        Assertions.assertEquals(TestConstants.NINETY, remoteClimateData.getTimeoutForAfterTemperature());
        Assertions.assertEquals(TestConstants.NINETY, remoteClimateData.getTimeOutPreTrip());

        Assertions.assertNotNull(remoteClimateData.toString());
    }

    /**
     * method to test RoLight Event.
     */

    @Test
    void testRoLightEvent() throws IOException {
        String requestJson = IOUtils
                .toString(RoEntityTest.class.getResourceAsStream("/remoteStatusLightRequest_1.json"), "UTF-8");
        Ro roEntity = mapper.readValue(requestJson, Ro.class);
        Assertions.assertEquals(Constants.REMOTEOPERATIONLIGHTS.toString(),
                roEntity.getRoEvent().getEventId().toString());
        RemoteOperationLightsV1_1 remoteLightData = (RemoteOperationLightsV1_1) roEntity.getRoEvent().getEventData();
        Assertions.assertEquals("kmpxNPUwqEW69CzK7VmO98BLWpLOqSw1", remoteLightData.getRoRequestId());
        Assertions.assertEquals(RemoteOperationLightsV1_1.State.ON, remoteLightData.getState());
        Assertions.assertEquals("ROSupportOwner", remoteLightData.getOrigin());
        Assertions.assertEquals(TestConstants.SEVENTY_FIVE, remoteLightData.getDuration());
        Assertions.assertNotNull(remoteLightData.toString());
    }

    /**
     * method to test RoLight Only Event.
     */

    @Test
    void testRoLightOnlyEvent() throws IOException {
        String requestJson = IOUtils
                .toString(RoEntityTest.class.getResourceAsStream("/remoteStatusLightRequest_1_2.json"), "UTF-8");
        Ro roEntity = mapper.readValue(requestJson, Ro.class);
        Assertions.assertEquals(Constants.REMOTEOPERATIONLIGHTSONLY.toString(),
                roEntity.getRoEvent().getEventId().toString());
        RemoteOperationLightsV1_2 remoteLightData = (RemoteOperationLightsV1_2) roEntity.getRoEvent().getEventData();
        Assertions.assertEquals("kmpxNPUwqEW69CzK7VmO98BLWpLOqSw1", remoteLightData.getRoRequestId());
        Assertions.assertEquals(RemoteOperationLightsV1_2.State.ON, remoteLightData.getState());
        Assertions.assertEquals("ROSupportOwner", remoteLightData.getOrigin());
        Assertions.assertEquals(TestConstants.SEVENTY_FIVE, remoteLightData.getDuration());
        Assertions.assertNotNull(remoteLightData.toString());
    }

    /**
     * method to test RoHorn Event.
     */
    @Test
    void testRoHornEvent() throws IOException {

        String requestJson = IOUtils.toString(RoEntityTest.class.getResourceAsStream("/remoteStatusHornRequest_1.json"),
                "UTF-8");

        Ro roEntity = mapper.readValue(requestJson, Ro.class);
        Assertions.assertEquals(Constants.REMOTEOPERATIONHORN.toString(),
                roEntity.getRoEvent().getEventId().toString());
        RemoteOperationHornV1_1 remoteHornData = (RemoteOperationHornV1_1) roEntity.getRoEvent().getEventData();
        Assertions.assertEquals("kmpxNPUwqEW69CzK7VmO98BLWpLOqSw1", remoteHornData.getRoRequestId());
        Assertions.assertEquals(RemoteOperationHornV1_1.State.ON, remoteHornData.getState());
        Assertions.assertEquals("ROSupportOwner", remoteHornData.getOrigin());
        Assertions.assertEquals(TestConstants.SEVENTY_FIVE, remoteHornData.getDuration());
        Assertions.assertNotNull(remoteHornData.toString());
    }

    /**
     * method to test RoAlarm Event.
     */

    @Test
    void testRoAlarmEvent() throws IOException {

        String requestJson = IOUtils
                .toString(RoEntityTest.class.getResourceAsStream("/remoteStatusAlarmRequest_1.json"), "UTF-8");

        Ro roEntity = mapper.readValue(requestJson, Ro.class);
        Assertions.assertEquals(Constants.REMOTEOPERATIONALARM.toString(),
                roEntity.getRoEvent().getEventId().toString());
        RemoteOperationAlarmV1_1 remoteAlarmData = (RemoteOperationAlarmV1_1) roEntity.getRoEvent().getEventData();
        Assertions.assertEquals("kmpxNPUwqEW69CzK7VmO98BLWpLOqSw1", remoteAlarmData.getRoRequestId());
        Assertions.assertEquals(RemoteOperationAlarmV1_1.State.ON, remoteAlarmData.getState());
        Assertions.assertEquals("ROSupportOwner", remoteAlarmData.getOrigin());
        Assertions.assertEquals(TestConstants.SEVENTY_FIVE, remoteAlarmData.getDuration());
        Assertions.assertNotNull(remoteAlarmData.toString());
    }

    /**
     * method to test RoHood Event.
     */
    @Test
    void testRoHoodEvent() throws IOException {

        String requestJson = IOUtils.toString(RoEntityTest.class.getResourceAsStream("/remoteStatusHoodRequest_1.json"),
                "UTF-8");

        Ro roEntity = mapper.readValue(requestJson, Ro.class);
        Assertions.assertEquals(Constants.REMOTEOPERATIONHOOD.toString(),
                roEntity.getRoEvent().getEventId().toString());
        RemoteOperationHoodV1_1 remoteHoodData = (RemoteOperationHoodV1_1) roEntity.getRoEvent().getEventData();
        Assertions.assertEquals("kmpxNPUwqEW69CzK7VmO98BLWpLOqSw1", remoteHoodData.getRoRequestId());
        Assertions.assertEquals(RemoteOperationHoodV1_1.State.UNLOCKED, remoteHoodData.getState());
        Assertions.assertEquals("ROSupportOwner", remoteHoodData.getOrigin());
        Assertions.assertNotNull(remoteHoodData.toString());
    }

    /**
     * method to test RoLiftGate Event.
     */
    @Test
    void testRoLiftGateEvent() throws IOException {

        String requestJson = IOUtils
                .toString(RoEntityTest.class.getResourceAsStream("/remoteStatusLiftGateRequest_1.json"), "UTF-8");

        Ro roEntity = mapper.readValue(requestJson, Ro.class);
        Assertions.assertEquals(Constants.REMOTEOPERATIONLIFTGATE.toString(),
                roEntity.getRoEvent().getEventId().toString());
        RemoteOperationLiftgateV1_1 remoteLiftGateData = (RemoteOperationLiftgateV1_1) roEntity.getRoEvent()
                .getEventData();
        Assertions.assertEquals("kmpxNPUwqEW69CzK7VmO98BLWpLOqSw1", remoteLiftGateData.getRoRequestId());
        Assertions.assertEquals(RemoteOperationLiftgateV1_1.State.OPENED, remoteLiftGateData.getState());
        Assertions.assertEquals("ROSupportOwner", remoteLiftGateData.getOrigin());
        Assertions.assertNotNull(remoteLiftGateData.toString());
    }

    /**
     * method to test RoLiftGate V2.0 Event.
     */
    @Test
    void testRoLiftGateV2Event() throws IOException {

        String requestJson = IOUtils
                .toString(RoEntityTest.class.getResourceAsStream("/remoteStatusLiftGateRequest_2.json"), "UTF-8");

        Ro roEntity = mapper.readValue(requestJson, Ro.class);
        Assertions.assertEquals(Constants.REMOTEOPERATIONLIFTGATE.toString(),
                roEntity.getRoEvent().getEventId().toString());
        RemoteOperationLiftgateV2_0 remoteLiftGateData = (RemoteOperationLiftgateV2_0) roEntity.getRoEvent()
                .getEventData();
        Assertions.assertEquals("kmpxNPUwqEW69CzK7VmO98BLWpLOqSw1", remoteLiftGateData.getRoRequestId());
        Assertions.assertEquals(RemoteOperationLiftgateV2_0.State.OPENED, remoteLiftGateData.getState());
        Assertions.assertEquals("ROSupportOwner", remoteLiftGateData.getOrigin());
        Assertions.assertNotNull(remoteLiftGateData.toString());
    }

    /**
     * method to test RoTrunk Event.
     */
    @Test
    void testRoTrunkEvent() throws IOException {

        String requestJson = IOUtils
                .toString(RoEntityTest.class.getResourceAsStream("/remoteStatusTrunkRequest_1.json"), "UTF-8");

        Ro roEntity = mapper.readValue(requestJson, Ro.class);
        Assertions.assertEquals(Constants.REMOTEOPERATIONTRUNK.toString(),
                roEntity.getRoEvent().getEventId().toString());
        RemoteOperationTrunkV1_1 remoteTrunkData = (RemoteOperationTrunkV1_1) roEntity.getRoEvent().getEventData();
        Assertions.assertEquals("kmpxNPUwqEW69CzK7VmO98BLWpLOqSw1", remoteTrunkData.getRoRequestId());
        Assertions.assertEquals(RemoteOperationTrunkV1_1.State.OPENED, remoteTrunkData.getState());
        Assertions.assertEquals("ROSupportOwner", remoteTrunkData.getOrigin());
        Assertions.assertNotNull(remoteTrunkData.toString());
    }

    /**
     * method to test RoTrunk v2 Event.
     */
    @Test
    void testRoTrunkV2Event() throws IOException {

        String requestJson = IOUtils
                .toString(RoEntityTest.class.getResourceAsStream("/remoteStatusTrunkRequest_2.json"), "UTF-8");

        Ro roEntity = mapper.readValue(requestJson, Ro.class);
        Assertions.assertEquals(Constants.REMOTEOPERATIONTRUNK, roEntity.getRoEvent().getEventId());
        RemoteOperationTrunkV2_0 remoteTrunkData = (RemoteOperationTrunkV2_0) roEntity.getRoEvent().getEventData();
        Assertions.assertEquals("kmpxNPUwqEW69CzK7VmO98BLWpLOqSw1", remoteTrunkData.getRoRequestId());
        Assertions.assertEquals(RemoteOperationTrunkV2_0.State.OPENED, remoteTrunkData.getState());
        Assertions.assertEquals("ROSupportOwner", remoteTrunkData.getOrigin());
        Assertions.assertNotNull(remoteTrunkData.toString());
    }

    /**
     * method to test Glove Box Event.
     */
    @Test
    void testRoGloveBoxEvent() throws IOException {

        String requestJson = IOUtils
                .toString(RoEntityTest.class.getResourceAsStream("/remoteStatusGloveBoxRequest_V2.json"), "UTF-8");

        Ro roEntity = mapper.readValue(requestJson, Ro.class);
        Assertions.assertEquals(Constants.REMOTEOPERATIONGLOVEBOX, roEntity.getRoEvent().getEventId());
        RemoteOperationGloveBoxV2_0 remoteGloveBoxData =
                (RemoteOperationGloveBoxV2_0) roEntity.getRoEvent().getEventData();
        Assertions.assertEquals("kmpxNPUwqEW69CzK7VmO98BLWpLOqSw1", remoteGloveBoxData.getRoRequestId());
        Assertions.assertEquals(RemoteOperationGloveBoxV2_0.State.LOCKED, remoteGloveBoxData.getState());
        Assertions.assertEquals("ROSupportOwner", remoteGloveBoxData.getOrigin());
        Assertions.assertNotNull(remoteGloveBoxData.toString());
    }

    /**
     * method to test RoWindow Event.
     */
    @Test
    void testRoWindowEvent() throws IOException {

        String requestJson = IOUtils
                .toString(RoEntityTest.class.getResourceAsStream("/remoteStatusWindowRequest_1.json"), "UTF-8");

        Ro roEntity = mapper.readValue(requestJson, Ro.class);
        Assertions.assertEquals(Constants.REMOTEOPERATIONWINDOWS.toString(),
                roEntity.getRoEvent().getEventId().toString());
        RemoteOperationWindowsV1_1 remoteWindowData = (RemoteOperationWindowsV1_1) roEntity.getRoEvent().getEventData();
        Assertions.assertEquals("kmpxNPUwqEW69CzK7VmO98BLWpLOqSw1", remoteWindowData.getRoRequestId());
        Assertions.assertEquals(RemoteOperationWindowsV1_1.State.OPENED, remoteWindowData.getState());
        Assertions.assertEquals("ROSupportOwner", remoteWindowData.getOrigin());
        Assertions.assertEquals(TestConstants.TWENTY, remoteWindowData.getPercent());
        Assertions.assertEquals(TestConstants.SEVENTY_FIVE, remoteWindowData.getDuration());
        Assertions.assertNotNull(remoteWindowData.toString());
    }

    /**
     * method to test RoDriverWindow Event.
     */

    @Test
    void testRoDriverWindowEvent() throws IOException {

        String requestJson = IOUtils
                .toString(RoEntityTest.class.getResourceAsStream("/remoteStatusDriverWindowRequest_1.json"), "UTF-8");

        Ro roEntity = mapper.readValue(requestJson, Ro.class);
        Assertions.assertEquals(Constants.REMOTEOPERATIONDRIVERWINDOW
                .toString(), roEntity.getRoEvent().getEventId().toString());
        RemoteOperationDriverWindowV1_1 remoteDrWindowData = (RemoteOperationDriverWindowV1_1) roEntity.getRoEvent()
                .getEventData();
        Assertions.assertEquals("kmpxNPUwqEW69CzK7VmO98BLWpLOqSw1", remoteDrWindowData.getRoRequestId());
        Assertions.assertEquals(RemoteOperationDriverWindowV1_1.State.OPENED, remoteDrWindowData.getState());
        Assertions.assertEquals("ROSupportOwner", remoteDrWindowData.getOrigin());
        Assertions.assertEquals(TestConstants.TWENTY, remoteDrWindowData.getPercent());
        Assertions.assertEquals(TestConstants.SEVENTY_FIVE, remoteDrWindowData.getDuration());
        Assertions.assertNotNull(remoteDrWindowData.toString());
    }

    /**
     * method to test RoStatus Event.
     */
    @Test
    void testRoStatusEvent() throws IOException {
        String requestJson = IOUtils.toString(RoEntityTest.class.getResourceAsStream("/remoteOperationStatus_1.json"),
                "UTF-8");
        Ro roEntity = mapper.readValue(requestJson, Ro.class);
        Assertions.assertEquals(Constants.REMOTEOPERATIONSTATUS.toString(),
                roEntity.getRoEvent().getEventId().toString());
        RemoteOperationStatusV1_1 remoteStatusData = (RemoteOperationStatusV1_1) roEntity.getRoEvent().getEventData();
        Assertions.assertEquals("P2KvQwet9A28bOrQgxQDcp60xhCP9O9a", remoteStatusData.getRoRequestId());
        Assertions.assertEquals(Status.SUCCESS, remoteStatusData.getStatus());
        Assertions.assertEquals("ROSupportOwner", remoteStatusData.getOrigin());
        Assertions.assertNotNull(remoteStatusData.toString());
    }
    /*
     * method to test scheduleDto
     */

    @Test
    void testScheduleDto() {
        ScheduleDto scheduleDto = new ScheduleDto();
        scheduleDto.setCreatedOn(TestConstants.LONG_150930484788L);
        scheduleDto.setName("User");
        scheduleDto.setScheduleId("u123");
        scheduleDto.setScheduleTs(TestConstants.LONG_1237262);
        scheduleDto.setSchedulerKey("s123");
        scheduleDto.setStatus(ScheduleStatus.ACTIVE);
        scheduleDto.setUpdatedOn(TestConstants.LONG_15230837300L);
        scheduleDto.setRecurrenceType(RecurrenceType.DAILY);

        Schedule schedule = new Schedule();
        schedule.setName("S123");
        schedule.setFirstScheduleTs(TestConstants.LONG_15230837300L);
        schedule.setRecurrenceType(RecurrenceType.MONTHLY);

        Schedule schedule2 = new Schedule();
        schedule2.setFirstScheduleTs(schedule.getFirstScheduleTs());
        schedule2.setName(schedule.getName());
        schedule2.setRecurrenceType(schedule.getRecurrenceType());

        AbstractRoEventData roEventData = new AbstractRoEventData();
        roEventData.setUserId("user123");
        roEventData.setSchedule(schedule);

        RoSchedule roSchedule = new RoSchedule();
        Map<String, List<ScheduleDto>> schedules = new HashMap<>();
        schedules.put("sc123", new ArrayList<ScheduleDto>());
        roSchedule.setSchemaVersion(Version.V1_0);
        roSchedule.setVehicleId("v1234");
        roSchedule.setSchedules(schedules);

        Assertions.assertEquals("user123", roEventData.getUserId());
        Assertions.assertNotNull(roEventData.getSchedule());

        Assertions.assertEquals(Version.V1_0, roSchedule.getSchemaVersion());
        RemoteOperationType rot = RemoteOperationType.REMOTE_OPERATION_HORN;
        Assertions.assertNotNull(rot);
        Assertions.assertEquals("v1234", roSchedule.getVehicleId());
        Assertions.assertNotNull(roSchedule.getSchedules());
        Assertions.assertNotNull(schedule.toString());

        Assertions.assertEquals("User", scheduleDto.getName());
        Assertions.assertEquals(TestConstants.LONG_150930484788L, scheduleDto.getCreatedOn());
        Assertions.assertEquals(RecurrenceType.DAILY, scheduleDto.getRecurrenceType());
        Assertions.assertEquals("u123", scheduleDto.getScheduleId());
        Assertions.assertEquals("s123", scheduleDto.getSchedulerKey());
        Assertions.assertEquals(TestConstants.LONG_1237262, scheduleDto.getScheduleTs());
        Assertions.assertEquals(ScheduleStatus.ACTIVE, scheduleDto.getStatus());
        Assertions.assertEquals(TestConstants.LONG_15230837300L, scheduleDto.getUpdatedOn());
    }

    /**
     * method to test RoResponse.
     */
    @Test
    void testRoResponse() throws IOException {
        String requestJson = IOUtils.toString(RoEntityTest.class.getResourceAsStream("/remoteOperationResponse_1.json"),
                "UTF-8");
        Ro roEntity = mapper.readValue(requestJson, Ro.class);
        roEntity.setId(new ObjectId());
        roEntity.setLastUpdatedTime(LocalDateTime.of(TestConstants.INT_2020, Month.MARCH,
                TestConstants.TWENTY_NINE, TestConstants.NINETEEN, TestConstants.THIRTY, TestConstants.FORTY));
        Assertions.assertEquals(LocalDateTime.of(TestConstants.INT_2020, Month.MARCH,
                TestConstants.TWENTY_NINE, TestConstants.NINETEEN,
                TestConstants.THIRTY, TestConstants.FORTY), roEntity.getLastUpdatedTime());
        roEntity.getId();
        Assertions.assertEquals(Constants.REMOTEOPERATIONRESPONSE.toString(),
                roEntity.getRoEvent().getEventId().toString());
        RemoteOperationResponseV1_1 remoteResponseData = (RemoteOperationResponseV1_1) roEntity.getRoEvent()
                .getEventData();
        Assertions.assertEquals("P2KvQwet9A28bOrQgxQDcp60xhCP9O9a", remoteResponseData.getRoRequestId());
        Assertions.assertEquals(Response.SUCCESS, remoteResponseData.getResponse());
        Assertions.assertEquals("ROSupportOwner", remoteResponseData.getOrigin());
        Assertions.assertNotNull(remoteResponseData.toString());
    }

    @Test
    void testCrankNotificationDataV1_0() {
        CrankNotificationDataV1_0 crankNotificationData = new CrankNotificationDataV1_0();
        crankNotificationData.setCrankAttempted(true);
        crankNotificationData.setAltitude(TestConstants.DOUBLE_2543535D);
        crankNotificationData.setBearing(TestConstants.DOUBLE_3434374D);
        crankNotificationData.setLatitude(TestConstants.DOUBLE_34734834D);
        crankNotificationData.setLongitude(TestConstants.DOUBLE_2323232D);
        crankNotificationData.setHorPosError(TestConstants.INT_404);

        RemoteOperationScheduleV1 remoteOperationScheduleV1 = new RemoteOperationScheduleV1();
        remoteOperationScheduleV1.setRoRequestId("req123");
        remoteOperationScheduleV1.setSchedulerKey("sche123");

        Assertions.assertEquals("req123", remoteOperationScheduleV1.getRoRequestId());
        Assertions.assertEquals("sche123", remoteOperationScheduleV1.getSchedulerKey());

        Assertions.assertTrue(crankNotificationData.isCrankAttempted());
        Assertions.assertNotNull(crankNotificationData.getAltitude());
        Assertions.assertNotNull(crankNotificationData.getBearing());
        Assertions.assertNotNull(crankNotificationData.getLatitude());
        Assertions.assertNotNull(crankNotificationData.getLongitude());
        Assertions.assertNotNull(crankNotificationData.getHorPosError());
        Assertions.assertNotNull(crankNotificationData.toString());
    }
}
