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

package org.eclipse.ecsp.ro.processor;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.prometheus.client.CollectorRegistry;
import org.apache.commons.io.IOUtils;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.AbstractRoEventData;
import org.eclipse.ecsp.domain.ro.RecurrenceType;
import org.eclipse.ecsp.domain.ro.Schedule;
import org.eclipse.ecsp.entities.AbstractIgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.events.scheduler.CreateScheduleEventData;
import org.eclipse.ecsp.events.scheduler.DeleteScheduleEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleNotificationEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleOpStatusEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleOpStatusEventData.ScheduleOpStatusErrorCode;
import org.eclipse.ecsp.events.scheduler.ScheduleStatus;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.ro.RoScheduleDAOMongoImpl;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

/**
 * test class for ScheduleEventDataHandler.
 */
@SpringBootTest(classes = org.eclipse.ecsp.Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = {"classpath:/application-test.properties", "classpath:/application-base.properties"})
@TestMethodOrder(MethodOrderer.MethodName.class)
public class ScheduleEventDataHandlerTest extends CommonTestBase {

    @Mock
    StreamProcessingContext spc;

    @Autowired
    private ScheduleEventDataHandler scheduleEventDataHandler;

    @MockBean
    private RoScheduleDAOMongoImpl roScheduleDAOMongoImpl;

    @Before
    @After
    public void cleanup() {
        CollectorRegistry.defaultRegistry.clear();
    }

    @Test
    public void processScheduledROEvent() throws JSONException, IOException {
        IgniteStringKey igniteKey = new IgniteStringKey();
        igniteKey.setKey("key");

        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        ScheduleNotificationEventData scheduleNotificationEventData = new ScheduleNotificationEventData();
        scheduleNotificationEventData.setScheduleIdId("scheduleUserId");
        byte[] payLoad = IOUtils
                .toString(ScheduleEventDataHandlerTest.class
                        .getResourceAsStream("/rcpdScheduleNotificationTest.json"), "UTF-8")
                .getBytes(StandardCharsets.UTF_8);
        scheduleNotificationEventData.setPayload(payLoad);
        igniteEvent.setEventData(scheduleNotificationEventData);
        igniteEvent.setEventId("testEventId");
        igniteEvent.setVersion(Version.V1_1);
        igniteEvent.setTimestamp(System.currentTimeMillis());
        igniteEvent.setVehicleId("bmw123");
        igniteEvent.setRequestId("requestId123");
        igniteEvent.setBizTransactionId("bizTransactionId");
        final int i = 1000;
        final int i1 = 60;
        final int i2 = 3;
        AbstractIgniteEvent abstractIgniteEvent = scheduleEventDataHandler
                .processScheduledROEvent(igniteKey, igniteEvent,
                        System.currentTimeMillis() + (i2 * i1 * i));
        Assert.assertNotNull(abstractIgniteEvent);
    }

    @Test
    public void createSchedulerEventTest() throws JsonProcessingException {

        IgniteEventImpl value = new IgniteEventImpl();
        AbstractRoEventData aoe = new AbstractRoEventData();
        Schedule schedule = new Schedule();
        schedule.setRecurrenceType(RecurrenceType.DAILY);
        aoe.setSchedule(schedule);
        value.setEventData(aoe);
        value.setEventId("testEventId");
        value.setVersion(Version.V1_1);
        value.setTimestamp(System.currentTimeMillis());
        value.setVehicleId("bmw123");
        value.setBizTransactionId("bizTransactionId");
        value.setCorrelationId("correlationId");
        value.setMessageId("messageId");

        IgniteEventImpl event = new IgniteEventImpl();
        event.setRequestId("requestId");
        event.setDFFQualifier("qualifier");

        String[] sourceTopics = {"ro"};

        IgniteStringKey key = new IgniteStringKey();
        IgniteEventImpl scheduleEvt = scheduleEventDataHandler
                .createSchedulerEvent(key, value, event, sourceTopics, "serviceName",
                        "serviceName", Integer.MAX_VALUE);

        Assert.assertNotNull(scheduleEvt);
    }

    @Test
    public void processSchedulerAckEventTest() throws
            JsonParseException, JsonMappingException, IOException, JSONException {
        IgniteEventImpl value = new IgniteEventImpl();
        value.setVehicleId("vehicleId");
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        DeleteScheduleEventData deleteSchedulerEvent = new DeleteScheduleEventData("scheduleId");
        ScheduleStatus status = ScheduleStatus.DELETE;
        igniteEvent.setEventData(deleteSchedulerEvent);
        ScheduleOpStatusEventData schedule = new ScheduleOpStatusEventData("scheduleId", status, igniteEvent, false);
        schedule.setStatusErrorCode(ScheduleOpStatusErrorCode.EXPIRED_SCHEDULE);
        value.setEventData(schedule);
        IgniteStringKey key = new IgniteStringKey();
        scheduleEventDataHandler.processSchedulerAckEvent(key, value);
        Mockito.verify(roScheduleDAOMongoImpl, times(1)).deleteRoSchedule(anyString(), anyString());
    }

    @Test
    public void processSchedulerAckEventTest2() throws
            JsonParseException, JsonMappingException, IOException, JSONException {
        IgniteEventImpl value = new IgniteEventImpl();
        value.setVehicleId("vehicleId");
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        DeleteScheduleEventData deleteSchedulerEvent = new DeleteScheduleEventData("scheduleId");
        ScheduleStatus status = ScheduleStatus.DELETE;
        igniteEvent.setEventData(deleteSchedulerEvent);
        ScheduleOpStatusEventData schedule = new ScheduleOpStatusEventData("scheduleId", status, igniteEvent, true);
        value.setEventData(schedule);
        IgniteStringKey key = new IgniteStringKey();
        scheduleEventDataHandler.processSchedulerAckEvent(key, value);
        Mockito.verify(roScheduleDAOMongoImpl, times(1)).deleteRoSchedule(anyString(), anyString());
    }

    @Test
    public void processSchedulerAckEventTest3() throws
            JsonParseException, JsonMappingException, IOException, JSONException {
        IgniteEventImpl value = new IgniteEventImpl();
        value.setVehicleId("vehicleId");
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        DeleteScheduleEventData deleteSchedulerEvent = new DeleteScheduleEventData("scheduleId");
        ScheduleStatus status = ScheduleStatus.DELETE;
        igniteEvent.setEventData(deleteSchedulerEvent);
        ScheduleOpStatusEventData schedule = new ScheduleOpStatusEventData("scheduleId", status, igniteEvent, true);
        schedule.setStatusErrorCode(ScheduleOpStatusErrorCode.EXPIRED_SCHEDULE);
        value.setEventData(schedule);
        IgniteStringKey key = new IgniteStringKey();
        scheduleEventDataHandler.processSchedulerAckEvent(key, value);
        Mockito.verify(roScheduleDAOMongoImpl, times(0)).deleteRoSchedule(anyString(), anyString());
    }

    @Test
    public void processSchedulerAckEventTest4() throws
            JsonParseException, JsonMappingException, IOException, JSONException {
        IgniteEventImpl value = new IgniteEventImpl();
        value.setVehicleId("vehicleId");
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        CreateScheduleEventData createScheduleEventData = new CreateScheduleEventData();
        ScheduleStatus status = ScheduleStatus.CREATE;
        igniteEvent.setEventData(createScheduleEventData);
        ScheduleOpStatusEventData schedule = new ScheduleOpStatusEventData("scheduleId", status, igniteEvent, true);
        schedule.setStatusErrorCode(ScheduleOpStatusErrorCode.EXPIRED_SCHEDULE);
        value.setEventData(schedule);
        IgniteStringKey key = new IgniteStringKey();
        scheduleEventDataHandler.processSchedulerAckEvent(key, value);
        Mockito.verify(roScheduleDAOMongoImpl, times(0)).deleteRoSchedule(anyString(), anyString());
    }

}
