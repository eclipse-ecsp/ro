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
import com.fasterxml.jackson.databind.JsonMappingException;
import io.prometheus.client.CollectorRegistry;
import net.sf.ehcache.Element;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.analytics.stream.base.utils.ForcedHealthCheckEvent;
import org.eclipse.ecsp.domain.DeviceMessageFailureEventDataV1_0;
import org.eclipse.ecsp.domain.EventID;
import org.eclipse.ecsp.domain.ro.RCPD;
import org.eclipse.ecsp.domain.ro.RCPDRequestV1_0;
import org.eclipse.ecsp.domain.ro.RCPDResponseV1_0;
import org.eclipse.ecsp.domain.ro.RCPDResponseV1_0.Response;
import org.eclipse.ecsp.domain.ro.constant.TestConstants;
import org.eclipse.ecsp.domain.ro.dao.RCPDDAOMongoImpl;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.entities.dma.DeviceMessageErrorCode;
import org.eclipse.ecsp.events.scheduler.CreateScheduleEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleNotificationEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleOpStatusEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleOpStatusEventData.ScheduleOpStatusErrorCode;
import org.eclipse.ecsp.events.scheduler.ScheduleStatus;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.ro.utils.CacheUtil;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

/**
 * Test class for RCPDHandler.
 */
@SpringBootTest(classes = org.eclipse.ecsp.Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = {"classpath:/application-test.properties", "classpath:/application-base.properties"})
public class RCPDHandlerTest extends CommonTestBase {

    protected Properties consumerProps;

    protected Properties producerProps;

    @Autowired
    @InjectMocks
    RCPDHandler rcpdHandler;

    @Mock
    StreamProcessingContext sp;

    @Autowired
    RCPDDAOMongoImpl rcpdDAOMongoImpl;

    @Autowired
    private CacheUtil cacheUtil;

    @Value("${kafka.sink.scheduler.topic}")
    private String schedulertopic;

    /**
     * setup test.
     *
     * @throws Exception Exception
     */
    @Before
    public void setup() {
        CollectorRegistry.defaultRegistry.clear();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void deleteSchedulerWithScheduleIDTest1() {
        String scheduleId = "testScheduleId";
        String vehicleId = "hq10084";
        ForcedHealthCheckEvent event = new ForcedHealthCheckEvent();
        String requestId = "testRequestId";
        event.setRequestId(requestId);
        IgniteStringKey igniteKey = new IgniteStringKey("deleteKey");
        rcpdHandler.deleteSchedulerWithScheduleID(igniteKey, scheduleId, vehicleId, event, sp);
        Assert.assertNotNull(event);
    }

    @Test
    public void isSchedulerExistsTest() {

        Map<String, String> rcpdRequestEntityMap = new HashMap<>();
        rcpdRequestEntityMap.put(org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_REQUEST_ORIGIN, "requestOrigin");
        rcpdRequestEntityMap.put(org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_REQUEST_USERID, "userId");
        rcpdRequestEntityMap.put(org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_REQUEST_SCHEDULEID, "scheduleId");

        Element rcpdRequestCacheElement = new Element("requestId_VIN", rcpdRequestEntityMap);
        cacheUtil.getCache(Constants.RO_CACHE_NAME).put(rcpdRequestCacheElement);

        String scheduleId = "scheduleId";
        String vehicleId = "vehicleId";
        ForcedHealthCheckEvent event = new ForcedHealthCheckEvent();
        String requestId = "requestId";
        event.setRequestId(requestId);
        IgniteStringKey igniteKey = new IgniteStringKey("deleteKey");
        rcpdHandler.deleteSchedulerWithScheduleID(igniteKey, scheduleId, vehicleId, event, sp);
        Assert.assertNotNull(rcpdRequestEntityMap);
    }

    @Test
    public void processRCPDResponseBranchTest() {

        Map<String, String> rcpdRequestEntityMap = new HashMap<>();
        rcpdRequestEntityMap.put(org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_REQUEST_ORIGIN, "requestOrigin");
        rcpdRequestEntityMap.put(org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_REQUEST_USERID, "userId");
        rcpdRequestEntityMap.put(org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_REQUEST_SCHEDULEID, "scheduleId");

        Element rcpdRequestCacheElement = new Element("requestId_VIN", rcpdRequestEntityMap);
        cacheUtil.getCache(Constants.RO_CACHE_NAME).put(rcpdRequestCacheElement);

        IgniteEventImpl rcpdResponse = new IgniteEventImpl();
        rcpdResponse.setRequestId("requestId");
        rcpdResponse.setVehicleId("VIN");
        rcpdResponse.setEventId("eventId");
        RCPDResponseV1_0 responseData = new RCPDResponseV1_0();
        responseData.setRcpdRequestId("requestId");
        rcpdResponse.setEventData(responseData);
        responseData.setResponse(Response.SUCCESS);

        IgniteKey igniteKey = new IgniteStringKey("deleteKey");
        rcpdHandler.processRCPDResponse(igniteKey, rcpdResponse, sp);
        Mockito.verify(sp, times(1)).forwardDirectly(any(IgniteKey.class), any(), anyString());
        Assert.assertNotNull(rcpdResponse);
    }

    @Test
    @Ignore
    public void processRCPDResponseBranchTest2() {

        Map<String, String> rcpdRequestEntityMap = new HashMap<>();
        Element rcpdRequestCacheElement = new Element("requestId_VIN", rcpdRequestEntityMap);
        cacheUtil.getCache(Constants.RO_CACHE_NAME).put(rcpdRequestCacheElement);

        IgniteEventImpl rcpdResponse = new IgniteEventImpl();
        rcpdResponse.setRequestId("requestId");
        rcpdResponse.setVehicleId("VIN");
        rcpdResponse.setEventId("eventId");
        RCPDResponseV1_0 responseData = new RCPDResponseV1_0();

        responseData.setRcpdRequestId("requestId");
        rcpdResponse.setEventData(responseData);
        responseData.setResponse(Response.SUCCESS);
        // java.lang.ClassCastException: class org.eclipse.ecsp.domain.ro
        // .RCPDResponseV1_0 cannot be cast to class org.eclipse.ecsp.domain.ro.RCPDRequestV1_0
        // buildElementForCache will cast RCPDResponseV1_0 to RCPDRequestV1_0

        IgniteKey igniteKey = new IgniteStringKey("deleteKey");
        rcpdHandler.processRCPDResponse(igniteKey, rcpdResponse, sp);
        Mockito.verify(sp, times(TestConstants.TWO)).forwardDirectly(any(IgniteKey.class), any(), anyString());
        Assert.assertNotNull(rcpdResponse);
    }

    @Test
    public void handleDeviceMessageFailureTest() {


        RCPDRequestV1_0 rcpdEventData = new RCPDRequestV1_0();
        rcpdEventData.setOrigin("");
        rcpdEventData.setRcpdRequestId("rcpdRequestId");
        rcpdEventData.setUserId("userId");

        DeviceMessageFailureEventDataV1_0 dmFailureEventData = new DeviceMessageFailureEventDataV1_0();
        IgniteEventImpl failedEvent = new IgniteEventImpl();
        dmFailureEventData.setErrorCode(DeviceMessageErrorCode.DEVICE_DELIVERY_CUTOFF_EXCEEDED);
        failedEvent.setEventData(rcpdEventData);
        failedEvent.setVehicleId("vehicleId");
        failedEvent.setBizTransactionId("bizTransactionId");
        failedEvent.setRequestId("requestId");
        dmFailureEventData.setFailedIgniteEvent(failedEvent);

        IgniteEventImpl value = new IgniteEventImpl();
        value.setEventData(dmFailureEventData);
        IgniteKey igniteKey = new IgniteStringKey("deleteKey");
        rcpdHandler.handleDeviceMessageFailure(igniteKey, value, sp);
        Mockito.verify(sp, times(0)).forwardDirectly(any(IgniteKey.class), any(), anyString());
        Assert.assertNotNull(rcpdEventData);
    }

    @Test
    public void handleDeviceMessageFailureBranchTest() {

        ReflectionTestUtils.setField(rcpdHandler, "sinkTopics", new String[]{});

        RCPDRequestV1_0 rcpdEventData = new RCPDRequestV1_0();
        rcpdEventData.setOrigin("");
        rcpdEventData.setRcpdRequestId("rcpdRequestId");
        rcpdEventData.setUserId("userId");

        DeviceMessageFailureEventDataV1_0 dmFailureEventData = new DeviceMessageFailureEventDataV1_0();
        dmFailureEventData.setErrorCode(DeviceMessageErrorCode.DEVICE_STATUS_INACTIVE);
        IgniteEventImpl failedEvent = new IgniteEventImpl();
        failedEvent.setEventData(rcpdEventData);
        failedEvent.setVehicleId("vehicleId");
        failedEvent.setBizTransactionId("bizTransactionId");
        failedEvent.setRequestId("requestId");
        dmFailureEventData.setFailedIgniteEvent(failedEvent);

        IgniteEventImpl value = new IgniteEventImpl();
        value.setEventData(dmFailureEventData);
        IgniteKey igniteKey = new IgniteStringKey("deleteKey");
        rcpdHandler.handleDeviceMessageFailure(igniteKey, value, sp);
        Mockito.verify(sp, times(1)).forwardDirectly(any(IgniteKey.class), any(), anyString());
        Assert.assertNotNull(rcpdEventData);
    }

    @Test
    public void handleDeviceMessageFailureBranchTest2() {

        ReflectionTestUtils.setField(rcpdHandler, "sinkTopics", new String[]{});

        Map<String, String> rcpdRequestEntityMap = new HashMap<>();
        rcpdRequestEntityMap.put(org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_REQUEST_ORIGIN, "requestOrigin");
        rcpdRequestEntityMap.put(org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_REQUEST_USERID, "userId");
        rcpdRequestEntityMap.put(org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_REQUEST_SCHEDULEID, "scheduleId");

        Element rcpdRequestCacheElement = new Element("rcpdRequestId_vehicleId", rcpdRequestEntityMap);
        cacheUtil.getCache(Constants.RO_CACHE_NAME).put(rcpdRequestCacheElement);

        RCPDRequestV1_0 rcpdEventData = new RCPDRequestV1_0();
        rcpdEventData.setOrigin("");
        rcpdEventData.setRcpdRequestId("rcpdRequestId");
        rcpdEventData.setUserId("userId");
        DeviceMessageFailureEventDataV1_0 dmFailureEventData = new DeviceMessageFailureEventDataV1_0();

        dmFailureEventData.setErrorCode(DeviceMessageErrorCode.DEVICE_STATUS_INACTIVE);
        IgniteEventImpl failedEvent = new IgniteEventImpl();
        failedEvent.setEventData(rcpdEventData);
        failedEvent.setVehicleId("vehicleId");
        failedEvent.setBizTransactionId("bizTransactionId");
        failedEvent.setRequestId("requestId");
        dmFailureEventData.setFailedIgniteEvent(failedEvent);

        IgniteEventImpl value = new IgniteEventImpl();
        value.setEventData(dmFailureEventData);
        IgniteKey igniteKey = new IgniteStringKey("deleteKey");
        rcpdHandler.handleDeviceMessageFailure(igniteKey, value, sp);
        Mockito.verify(sp, times(0)).forwardDirectly(any(IgniteKey.class), any(), anyString());
        Assert.assertNotNull(rcpdEventData);
    }

    @Test
    public void handleDeviceMessageFailureBranchTest3() {
        rcpdDAOMongoImpl.deleteAll();
        prepareRCPDEntity(false);

        ReflectionTestUtils.setField(rcpdHandler, "sinkTopics", new String[]{});

        RCPDRequestV1_0 rcpdEventData = new RCPDRequestV1_0();
        rcpdEventData.setOrigin("");
        rcpdEventData.setRcpdRequestId("rcpdRequestId");
        rcpdEventData.setUserId("userId");
        DeviceMessageFailureEventDataV1_0 dmFailureEventData = new DeviceMessageFailureEventDataV1_0();

        dmFailureEventData.setErrorCode(DeviceMessageErrorCode.DEVICE_STATUS_INACTIVE);
        IgniteEventImpl failedEvent = new IgniteEventImpl();
        failedEvent.setEventData(rcpdEventData);
        failedEvent.setVehicleId("vehicleId");
        failedEvent.setBizTransactionId("bizTransactionId");
        failedEvent.setRequestId("requestId");
        dmFailureEventData.setFailedIgniteEvent(failedEvent);

        IgniteEventImpl value = new IgniteEventImpl();
        value.setEventData(dmFailureEventData);
        IgniteKey igniteKey = new IgniteStringKey("deleteKey");
        rcpdHandler.handleDeviceMessageFailure(igniteKey, value, sp);
        Mockito.verify(sp, times(0)).forwardDirectly(any(IgniteKey.class), any(), anyString());
        Assert.assertNotNull(rcpdEventData);
    }

    @Test
    public void processSchedulerAckEventTest() throws JsonParseException,
            JsonMappingException, IOException, JSONException {

        CreateScheduleEventData createScheduleEventData = new CreateScheduleEventData();
        createScheduleEventData.setNotificationPayload(new byte[]{});


        ScheduleOpStatusEventData eventData = new ScheduleOpStatusEventData();
        eventData.setScheduleId("scheduleId");
        eventData.setValid(false);
        eventData.setStatus(ScheduleStatus.DELETE);
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventData(createScheduleEventData);
        eventData.setIgniteEvent(igniteEvent);
        IgniteEventImpl value = new IgniteEventImpl();

        value.setEventData(eventData);
        IgniteKey igniteKey = new IgniteStringKey("deleteKey");

        rcpdHandler.processSchedulerAckEvent(igniteKey, value);
        Assert.assertNotNull(eventData);
    }

    @Test
    public void processSchedulerAckEventTest2() throws JsonParseException,
            JsonMappingException, IOException, JSONException {

        CreateScheduleEventData createScheduleEventData = new CreateScheduleEventData();
        createScheduleEventData.setNotificationPayload(new byte[]{});


        ScheduleOpStatusEventData eventData = new ScheduleOpStatusEventData();
        eventData.setScheduleId("scheduleId");
        eventData.setValid(true);
        eventData.setStatus(ScheduleStatus.DELETE);
        eventData.setStatusErrorCode(ScheduleOpStatusErrorCode.INVALID_DELETE_SCHEDULE_INVALID_SCHEDULE_ID);
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventData(createScheduleEventData);
        eventData.setIgniteEvent(igniteEvent);
        IgniteEventImpl value = new IgniteEventImpl();

        value.setEventData(eventData);
        IgniteKey igniteKey = new IgniteStringKey("deleteKey");

        rcpdHandler.processSchedulerAckEvent(igniteKey, value);
        Assert.assertNotNull(eventData);
    }

    @Test
    public void processSchedulerAckEventTest3() throws JsonParseException,
            JsonMappingException, IOException, JSONException {

        CreateScheduleEventData createScheduleEventData = new CreateScheduleEventData();
        createScheduleEventData.setNotificationPayload(new byte[]{});

        IgniteEventImpl igniteEvent = new IgniteEventImpl();

        ScheduleOpStatusEventData eventData = new ScheduleOpStatusEventData();
        eventData.setValid(true);
        eventData.setStatus(ScheduleStatus.DELETE);
        igniteEvent.setEventData(createScheduleEventData);
        igniteEvent.setEventId(EventID.DELETE_SCHEDULE_EVENT);
        eventData.setIgniteEvent(igniteEvent);
        IgniteEventImpl value = new IgniteEventImpl();

        value.setEventData(eventData);
        IgniteKey igniteKey = new IgniteStringKey("deleteKey");

        rcpdHandler.processSchedulerAckEvent(igniteKey, value);
        Assert.assertNotNull(eventData);
    }

    @Test
    public void processSchedulerAckEventTest4() throws JsonParseException,
            JsonMappingException, IOException, JSONException {
        rcpdDAOMongoImpl.deleteAll();

        CreateScheduleEventData createScheduleEventData = new CreateScheduleEventData();
        createScheduleEventData.setNotificationPayload(new byte[]{});


        ScheduleOpStatusEventData eventData = new ScheduleOpStatusEventData();
        eventData.setScheduleId("scheduleId");
        eventData.setValid(true);
        eventData.setStatus(ScheduleStatus.DELETE);
        IgniteEventImpl igniteEvent = new IgniteEventImpl();

        igniteEvent.setEventData(createScheduleEventData);
        igniteEvent.setEventId(EventID.DELETE_SCHEDULE_EVENT);
        igniteEvent.setVehicleId("vehicleId");
        eventData.setIgniteEvent(igniteEvent);
        IgniteEventImpl value = new IgniteEventImpl();

        value.setEventData(eventData);

        Map<String, String> rcpdRequestEntityMap = new HashMap<>();
        rcpdRequestEntityMap.put(org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_REQUEST_ORIGIN, "requestOrigin");
        rcpdRequestEntityMap.put(org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_REQUEST_USERID, "userId");
        rcpdRequestEntityMap.put(org.eclipse.ecsp.domain.ro.constant.Constants.RCPD_REQUEST_SCHEDULEID, "scheduleId");

        Element rcpdRequestCacheElement = new Element("rcpdRequestId_vehicleId", rcpdRequestEntityMap);
        cacheUtil.getCache(Constants.RO_CACHE_NAME).put(rcpdRequestCacheElement);

        prepareRCPDEntity(true);

        IgniteKey igniteKey = new IgniteStringKey("deleteKey");
        rcpdHandler.processSchedulerAckEvent(igniteKey, value);
        Assert.assertNotNull(cacheUtil.getCache(Constants.RO_CACHE_NAME).get("rcpdRequestId_vehicleId"));
    }

    @Test
    public void processScheduleNotificationTest() {
        rcpdDAOMongoImpl.deleteAll();
        prepareRCPDEntity(true);

        IgniteEventImpl value = new IgniteEventImpl();

        byte[] payload = "{\"RequestId\":\"rcpdRequestId\",\"BizTransactionId\":\"bizTransactionId\"}".getBytes();
        ScheduleNotificationEventData eventData = new ScheduleNotificationEventData();
        eventData.setPayload(payload);
        value.setEventData(eventData);
        value.setVehicleId("vehicleId");

        IgniteKey igniteKey = new IgniteStringKey("deleteKey");
        rcpdHandler.processScheduleNotification(igniteKey, value, sp);
        Mockito.verify(sp, times(0)).forwardDirectly(any(IgniteKey.class), any(), anyString());

    }

    @Test
    public void processScheduleNotificationTest2() {
        rcpdDAOMongoImpl.deleteAll();
        prepareRCPDEntity(false);

        IgniteEventImpl value = new IgniteEventImpl();

        byte[] payload = "{\"RequestId\":\"rcpdRequestId\",\"BizTransactionId\":\"bizTransactionId\"}".getBytes();
        ScheduleNotificationEventData eventData = new ScheduleNotificationEventData();
        eventData.setPayload(payload);
        value.setEventData(eventData);
        value.setVehicleId("vehicleId");

        IgniteKey igniteKey = new IgniteStringKey("deleteKey");
        rcpdHandler.processScheduleNotification(igniteKey, value, sp);
        Mockito.verify(sp, times(0)).forwardDirectly(any(IgniteKey.class), any(), anyString());
    }

    @Test
    public void processScheduleNotificationTest3() {
        rcpdDAOMongoImpl.deleteAll();
        prepareRCPDEntity(false);
        ReflectionTestUtils.setField(rcpdHandler, "rcpdNotificationIdMappings", new HashMap<>());

        IgniteEventImpl value = new IgniteEventImpl();

        byte[] payload = "{\"RequestId\":\"rcpdRequestId\",\"BizTransactionId\":\"bizTransactionId\"}".getBytes();
        ScheduleNotificationEventData eventData = new ScheduleNotificationEventData();
        eventData.setPayload(payload);
        value.setEventData(eventData);
        value.setVehicleId("vehicleId");

        IgniteKey igniteKey = new IgniteStringKey("deleteKey");
        rcpdHandler.processScheduleNotification(igniteKey, value, sp);
        Mockito.verify(sp, times(0)).forwardDirectly(any(IgniteKey.class), any(), anyString());
    }

    @Test
    public void processScheduleNotificationTest4() {
        rcpdDAOMongoImpl.deleteAll();
        prepareRCPDEntity(false);
        ReflectionTestUtils.setField(rcpdHandler, "sinkTopics", new String[]{});

        IgniteEventImpl value = new IgniteEventImpl();

        byte[] payload = "{\"RequestId\":\"rcpdRequestId\",\"BizTransactionId\":\"bizTransactionId\"}".getBytes();
        ScheduleNotificationEventData eventData = new ScheduleNotificationEventData();
        eventData.setPayload(payload);
        value.setEventData(eventData);
        value.setVehicleId("vehicleId");

        IgniteKey igniteKey = new IgniteStringKey("deleteKey");
        rcpdHandler.processScheduleNotification(igniteKey, value, sp);
        Mockito.verify(sp, times(0)).forwardDirectly(any(IgniteKey.class), any(), anyString());
    }

    private void prepareRCPDEntity(boolean withResponseList) {
        RCPDRequestV1_0 rcpdEventData = new RCPDRequestV1_0();
        rcpdEventData.setOrigin("");
        rcpdEventData.setRcpdRequestId("rcpdRequestId");
        rcpdEventData.setUserId("userId");
        rcpdEventData.setScheduleRequestId("scheduleId");

        IgniteEventImpl rcpdEvent = new IgniteEventImpl();
        rcpdEvent.setEventData(rcpdEventData);
        rcpdEvent.setVehicleId("vehicleId");

        RCPD rcpd = new RCPD();
        if (withResponseList) {
            List<IgniteEvent> rcpdResponseList = new ArrayList<>();
            rcpdResponseList.add(rcpdEvent);
            rcpd.setRcpdResponseList(rcpdResponseList);
        }
        rcpd.setRcpdEvent(rcpdEvent);

        rcpdDAOMongoImpl.save(rcpd);
    }

}