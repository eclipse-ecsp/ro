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

import io.prometheus.client.CollectorRegistry;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.analytics.stream.base.idgen.internal.GlobalMessageIdGenerator;
import org.eclipse.ecsp.cache.IgniteCache;
import org.eclipse.ecsp.domain.EventID;
import org.eclipse.ecsp.domain.GenericCustomExtension;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.AbstractRoEventData;
import org.eclipse.ecsp.domain.ro.ROStatus;
import org.eclipse.ecsp.domain.ro.RemoteOperationAlarmV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationClimateV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationClimateV2_0;
import org.eclipse.ecsp.domain.ro.RemoteOperationDoorsV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationDoorsV1_1.State;
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
import org.eclipse.ecsp.domain.ro.RemoteOperationTrunkV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationTrunkV2_0;
import org.eclipse.ecsp.domain.ro.RemoteOperationWindowsV1_1;
import org.eclipse.ecsp.domain.ro.Ro;
import org.eclipse.ecsp.domain.ro.Schedule;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.domain.ro.constant.TestConstants;
import org.eclipse.ecsp.entities.AbstractIgniteEvent;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.entities.UserContext;
import org.eclipse.ecsp.events.scheduler.ScheduleNotificationEventData;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.ro.RoDAOMongoImpl;
import org.eclipse.ecsp.ro.processor.strategy.impl.RoRequestStreamProcessor;
import org.eclipse.ecsp.ro.utils.CacheUtil;
import org.eclipse.ecsp.ro.utils.CachedKeyUtil;
import org.eclipse.ecsp.services.utils.SettingsManagerClient;
import org.eclipse.ecsp.services.utils.VehicleProfileClient;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.eclipse.ecsp.ro.constants.Constants.REMOTEOPERATIONALARMV1_1;
import static org.eclipse.ecsp.ro.constants.Constants.REMOTE_OPERATION_REQUEST_EVENT_ID;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * test class for ROStreamProcessor.
 */
@SpringBootTest(classes = org.eclipse.ecsp.Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = {"classpath:/application-test.properties", "classpath:/application-base.properties"})
@TestMethodOrder(MethodOrderer.MethodName.class)
public class ROStreamProcessorUnitTest extends CommonTestBase {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ROStreamProcessorUnitTest.class);

    protected static final String RESPONSE = "response";

    protected static final String CUSTOM_DATA = "CUSTOM_DATA";

    private static String sourceTopicName;

    private static String sinkTopicName;

    @Autowired
    protected RedissonClient redissonClient;

    String key = "Device123";

    String vehicleId = "vehicleId";

    @Autowired
    IgniteCache igniteCache;

    @InjectMocks
    @Autowired
    ROStreamProcessor sp;

    @Mock
    VehicleProfileClient vehicleProfileClient;

    @Mock
    StreamProcessingContext spc;

    @Mock
    SettingsManagerClient settingsManagerClient;

    @Value("${source.topic.name}")
    private String[] sourceTopics;

    @Value("${sink.topic.name}")
    private String[] sinkTopics;

    @Value("#{${notification.responsemessage.notificationid.mapping}}")
    private Map<String, String> notificationIdMapping;

    @Autowired
    private RoDAOMongoImpl roDAO;

    @Autowired
    private CacheUtil cacheUtil;

    private IgniteEventImpl eventImpl;

    private IgniteStringKey igniteKey;

    @Mock
    private GlobalMessageIdGenerator globalMessageIdGenerator;

    @NotBlank
    @Value("${settings.object.json.path}")
    private String settingsJsonPath;

    @Autowired
    @Qualifier(REMOTE_OPERATION_REQUEST_EVENT_ID)
    private RoRequestStreamProcessor roRequestStreamProcessor;

    /**
     * setup().
     *
     * @throws Exception Exception
     */
    @Before
    public void setup() {
        CollectorRegistry.defaultRegistry.clear();
        eventImpl = new IgniteEventImpl();
        sp.init(spc);
        sp.setNotificationIdMapping(notificationIdMapping);

        sourceTopicName = sourceTopics[0];
        sinkTopicName = sinkTopics[0];
        MockitoAnnotations.initMocks(this);

        Mockito.when(globalMessageIdGenerator.generateUniqueMsgId(vehicleId)).thenReturn("1");
    }

    @Test
    public void testRoNotificationDoorLockedSuccess() throws Exception {

        RemoteOperationDoorsV1_1 doorData = getRODoorData(State.LOCKED);
        IgniteEvent igImpl = getIgniteEvent(doorData, Constants.REMOTEOPERATIONDOORS);
        insertMongoData(igImpl);

        RemoteOperationResponseV1_1 roRes = getROResponseData(Response.SUCCESS);

        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);

        Assert.assertTrue(roDAO.findAll().size() > 0);

        roDAO.deleteAll();
    }

    @Test
    public void testRoResponseWithNullCorrelationId() throws Exception {

        RemoteOperationDoorsV1_1 doorData = getRODoorData(State.LOCKED);
        IgniteEvent igImpl = getIgniteEvent(doorData, Constants.REMOTEOPERATIONDOORS);
        insertMongoData(igImpl);

        RemoteOperationResponseV1_1 roRes = getROResponseData(Response.SUCCESS);

        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);
        eventImpl.setCorrelationId(null);
        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);
        Assert.assertTrue(roDAO.findAll().size() > 0);
        eventImpl.setCorrelationId("0");
        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord2 = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord2);
        Assert.assertTrue(roDAO.findAll().size() > 0);
        roDAO.deleteAll();
    }

    @Test
    public void testRoNotificationDoorlockedFail() throws Exception {

        RemoteOperationDoorsV1_1 doorData = getRODoorData(State.LOCKED);
        IgniteEvent igImpl = getIgniteEvent(doorData, Constants.REMOTEOPERATIONDOORS);
        insertMongoData(igImpl);

        RemoteOperationResponseV1_1 roRes = getROResponseData(Response.FAIL);

        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);
        Assert.assertTrue(roDAO.findAll().size() > 0);

        roDAO.deleteAll();
    }

    @Test
    public void testRoLightOnlyNotification() throws Exception {

        RemoteOperationLightsV1_2 doorData = getROLightsONlyData(RemoteOperationLightsV1_2.State.ON);
        IgniteEvent igImpl = getIgniteEvent(doorData, Constants.REMOTEOPERATIONLIGHTSONLY);
        insertMongoData(igImpl);

        RemoteOperationResponseV1_1 roRes = getROResponseData(Response.SUCCESS);

        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);
        Assert.assertTrue(roDAO.findAll().size() > 0);

        roDAO.deleteAll();
    }

    @Test
    public void testRoLightOnlyNotificationWithCustomerExtension() throws Exception {

        RemoteOperationLightsV1_2 doorData = getROLightsONlyData(RemoteOperationLightsV1_2.State.ON);
        IgniteEvent igImpl = getIgniteEvent(doorData, Constants.REMOTEOPERATIONLIGHTSONLY);
        insertMongoData(igImpl);
        RemoteOperationResponseV1_1 roRes = getROResponseData(Response.FAIL);
        GenericCustomExtension customExtension = new GenericCustomExtension();
        Map response = new HashMap();
        response.put(RESPONSE, CUSTOM_DATA);
        customExtension.setCustomData(response);
        roRes.setCustomExtension(customExtension);
        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);
        Assert.assertTrue(roDAO.findAll().size() > 0);
        //condition coverage
        ReflectionTestUtils.setField(roRequestStreamProcessor, "roQueueEnable", false);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord2 = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord2);
        Assert.assertTrue(roDAO.findAll().size() > 0);
        roDAO.deleteAll();
    }

    @Test
    public void testRoNotificationDoorUnlockedfailed() throws Exception {

        RemoteOperationDoorsV1_1 doorData = getRODoorData(State.UNLOCKED);
        IgniteEvent igImpl = getIgniteEvent(doorData, Constants.REMOTEOPERATIONDOORS);
        insertMongoData(igImpl);

        RemoteOperationResponseV1_1 roRes = getROResponseData(Response.FAIL);

        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);
        Assert.assertTrue(roDAO.findAll().size() > 0);

        roDAO.deleteAll();
    }

    @Test
    public void testRoNotificationDoorUnlockedSuccessWithCache() throws Exception {


        RemoteOperationResponseV1_1 roRes = getROResponseData(Response.SUCCESS);

        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);

        RemoteOperationDoorsV1_1 doorData = getRODoorData(State.UNLOCKED);
        IgniteEvent igImpl = getIgniteEvent(doorData, Constants.REMOTEOPERATIONDOORS);
        cacheUtil.getCache(org.eclipse.ecsp.ro.constants.Constants.RO_CACHE_NAME)
                .put(roRequestStreamProcessor.buildElementForCache(igImpl));

        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);
        Assert.assertEquals(0, roDAO.findAll().size());

        roDAO.deleteAll();
    }

    @Test
    public void testRoNotificationDoorUnlockedSuccess() throws Exception {

        RemoteOperationDoorsV1_1 doorData = getRODoorData(State.UNLOCKED);
        IgniteEvent igImpl = getIgniteEvent(doorData, Constants.REMOTEOPERATIONDOORS);
        insertMongoData(igImpl);

        RemoteOperationResponseV1_1 roRes = getROResponseData(Response.FAIL);

        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);
        Assert.assertTrue(roDAO.findAll().size() > 0);

        roDAO.deleteAll();
    }

    @Test
    public void testRoNotificationDoorUnlockedTimeout() throws Exception {

        RemoteOperationDoorsV1_1 doorData = getRODoorData(State.UNLOCKED);
        IgniteEvent igImpl = getIgniteEvent(doorData, Constants.REMOTEOPERATIONDOORS);
        insertMongoData(igImpl);

        RemoteOperationResponseV1_1 roRes = getROResponseData(Response.FAIL_MESSAGE_DELIVERY_TIMED_OUT);

        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);
        Assert.assertTrue(roDAO.findAll().size() > 0);

        roDAO.deleteAll();
    }

    @Test
    public void testRoNotificationEngineStartedSuccess() throws Exception {

        RemoteOperationEngineV1_1 engineData =
                getROEngineData(RemoteOperationEngineV1_1.State.STARTED);
        IgniteEvent igImpl = getIgniteEvent(engineData, Constants.REMOTEOPERATIONENGINE);
        insertMongoData(igImpl);

        RemoteOperationResponseV1_1 roRes = getROResponseData(Response.SUCCESS);

        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);
        Assert.assertTrue(roDAO.findAll().size() > 0);

        roDAO.deleteAll();
    }

    @Test
    public void testRoNotificationEngineStopSuccess() throws Exception {

        RemoteOperationEngineV1_1 engineData =
                getROEngineData(RemoteOperationEngineV1_1.State.STOPPED);
        IgniteEvent igImpl = getIgniteEvent(engineData, Constants.REMOTEOPERATIONENGINE);
        insertMongoData(igImpl);

        RemoteOperationResponseV1_1 roRes = getROResponseData(Response.SUCCESS);

        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);
        Assert.assertTrue(roDAO.findAll().size() > 0);

        roDAO.deleteAll();
    }

    @Test
    public void testRoNotificationAlarm() throws Exception {

        RemoteOperationAlarmV1_1 engineData =
                getROAlarmData(RemoteOperationAlarmV1_1.State.ON);
        IgniteEvent igImpl = getIgniteEvent(engineData, Constants.REMOTEOPERATIONALARM);
        insertMongoData(igImpl);

        RemoteOperationResponseV1_1 roRes = getROResponseData(Response.SUCCESS);

        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);
        Assert.assertTrue(roDAO.findAll().size() > 0);

        roDAO.deleteAll();
    }

    @Test
    public void testRoNotificationDriverDoor() throws Exception {

        RemoteOperationDriverDoorV1_1 engineData = (RemoteOperationDriverDoorV1_1)
                getObjectOf(RemoteOperationDriverDoorV1_1.class);
        engineData.setState(RemoteOperationDriverDoorV1_1.State.LOCKED);
        engineData.setOrigin("ROSupportOwner");
        engineData.setRoRequestId("roReq123");
        engineData.setUserId("userId");

        IgniteEvent igImpl = getIgniteEvent(engineData, Constants.REMOTEOPERATIONDRIVERDOOR);
        insertMongoData(igImpl);
        RemoteOperationResponseV1_1 roRes = getROResponseData(Response.SUCCESS);
        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);
        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);
        Assert.assertTrue(roDAO.findAll().size() > 0);

        roDAO.deleteAll();
    }

    @Test
    public void testRoNotificationClimate() throws Exception {

        RemoteOperationClimateV1_1 engineData = (RemoteOperationClimateV1_1)
                getObjectOf(RemoteOperationClimateV1_1.class);
        engineData.setState(RemoteOperationClimateV1_1.State.AUTO);
        engineData.setOrigin("ROSupportOwner");
        engineData.setRoRequestId("roReq123");
        engineData.setUserId("userId");

        IgniteEvent igImpl = getIgniteEvent(engineData, Constants.REMOTEOPERATIONCLIMATE);
        insertMongoData(igImpl);
        RemoteOperationResponseV1_1 roRes = getROResponseData(Response.SUCCESS);
        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);
        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);
        Assert.assertTrue(roDAO.findAll().size() > 0);

        roDAO.deleteAll();
    }

    @Test
    public void testRoNotificationWindow() throws Exception {

        RemoteOperationWindowsV1_1 engineData = (RemoteOperationWindowsV1_1)
                getObjectOf(RemoteOperationWindowsV1_1.class);
        engineData.setState(RemoteOperationWindowsV1_1.State.CLOSED);
        engineData.setOrigin("ROSupportOwner");
        engineData.setRoRequestId("roReq123");
        engineData.setUserId("userId");

        IgniteEvent igImpl = getIgniteEvent(engineData, Constants.REMOTEOPERATIONWINDOWS);
        insertMongoData(igImpl);
        RemoteOperationResponseV1_1 roRes = getROResponseData(Response.SUCCESS);
        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);
        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);
        Assert.assertTrue(roDAO.findAll().size() > 0);

        roDAO.deleteAll();
    }

    @Test
    public void testRoNotificationHood() throws Exception {

        RemoteOperationHoodV1_1 engineData = (RemoteOperationHoodV1_1) getObjectOf(RemoteOperationHoodV1_1.class);
        engineData.setState(RemoteOperationHoodV1_1.State.LOCKED);
        engineData.setOrigin("ROSupportOwner");
        engineData.setRoRequestId("roReq123");
        engineData.setUserId("userId");

        IgniteEvent igImpl = getIgniteEvent(engineData, Constants.REMOTEOPERATIONHOOD);
        insertMongoData(igImpl);
        RemoteOperationResponseV1_1 roRes = getROResponseData(Response.SUCCESS);
        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);
        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);
        Assert.assertTrue(roDAO.findAll().size() > 0);

        roDAO.deleteAll();
    }

    @Test
    public void testRoNotificationHorn() throws Exception {

        RemoteOperationHornV1_1 engineData = (RemoteOperationHornV1_1) getObjectOf(RemoteOperationHornV1_1.class);
        engineData.setState(RemoteOperationHornV1_1.State.ON);
        engineData.setOrigin("ROSupportOwner");
        engineData.setRoRequestId("roReq123");
        engineData.setUserId("userId");

        IgniteEvent igImpl = getIgniteEvent(engineData, Constants.REMOTEOPERATIONHORN);
        insertMongoData(igImpl);
        RemoteOperationResponseV1_1 roRes = getROResponseData(Response.SUCCESS);
        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);
        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);
        Assert.assertTrue(roDAO.findAll().size() > 0);

        roDAO.deleteAll();
    }

    @Test
    public void testRoNotificationLiftGate() throws Exception {

        RemoteOperationLiftgateV1_1 engineData = (RemoteOperationLiftgateV1_1)
                getObjectOf(RemoteOperationLiftgateV1_1.class);
        engineData.setState(RemoteOperationLiftgateV1_1.State.CLOSED);
        engineData.setOrigin("ROSupportOwner");
        engineData.setRoRequestId("roReq123");
        engineData.setUserId("userId");

        IgniteEvent igImpl = getIgniteEvent(engineData, Constants.REMOTEOPERATIONLIFTGATE);
        insertMongoData(igImpl);
        RemoteOperationResponseV1_1 roRes = getROResponseData(Response.SUCCESS);
        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);
        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);
        Assert.assertTrue(roDAO.findAll().size() > 0);

        roDAO.deleteAll();
    }

    @Test
    public void testRoNotificationLights() throws Exception {

        RemoteOperationLightsV1_1 engineData = (RemoteOperationLightsV1_1) getObjectOf(RemoteOperationLightsV1_1.class);
        engineData.setState(RemoteOperationLightsV1_1.State.ON);
        engineData.setOrigin("ROSupportOwner");
        engineData.setRoRequestId("roReq123");
        engineData.setUserId("userId");

        IgniteEvent igImpl = getIgniteEvent(engineData, Constants.REMOTEOPERATIONLIGHTS);
        insertMongoData(igImpl);
        RemoteOperationResponseV1_1 roRes = getROResponseData(Response.SUCCESS);
        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);
        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);
        Assert.assertTrue(roDAO.findAll().size() > 0);

        roDAO.deleteAll();
    }

    @Test
    public void testRoNotificationTrunk() throws Exception {

        RemoteOperationTrunkV1_1 engineData = (RemoteOperationTrunkV1_1) getObjectOf(RemoteOperationTrunkV1_1.class);
        engineData.setState(RemoteOperationTrunkV1_1.State.LOCKED);
        engineData.setOrigin("ROSupportOwner");
        engineData.setRoRequestId("roReq123");
        engineData.setUserId("userId");

        IgniteEvent igImpl = getIgniteEvent(engineData, Constants.REMOTEOPERATIONTRUNK);
        insertMongoData(igImpl);
        RemoteOperationResponseV1_1 roRes = getROResponseData(Response.SUCCESS);
        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);
        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);
        Assert.assertTrue(roDAO.findAll().size() > 0);

        roDAO.deleteAll();
    }

    private void prepareProcessVehicleProfileChangedNotificationEventTestData() {
        // entity
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setVehicleId("vin666666");
        igniteEvent.setSchemaVersion(Version.V1_0);
        igniteEvent.setEventId(REMOTEOPERATIONALARMV1_1);
        igniteEvent.setMessageId("10001");
        UserContext uc = new UserContext();
        uc.setUserId("user_test");
        uc.setRole("VO");
        igniteEvent.setUserContextInfo(Arrays.asList(uc));
        RemoteOperationAlarmV1_1 data = new RemoteOperationAlarmV1_1();
        igniteEvent.setEventData(data);

        Ro entity = new Ro();
        entity.setSchemaVersion(Version.V1_0);
        entity.setRoEvent(igniteEvent);

        roDAO.save(entity);

        // entity2
        IgniteEventImpl igniteEvent2 = new IgniteEventImpl();
        igniteEvent2.setVehicleId("vin666666");
        igniteEvent2.setSchemaVersion(Version.V1_0);
        igniteEvent2.setEventId(REMOTEOPERATIONALARMV1_1);
        igniteEvent2.setMessageId("10002");
        UserContext uc2 = new UserContext();
        uc2.setUserId("user_test");
        uc2.setRole("VO");
        igniteEvent2.setUserContextInfo(Arrays.asList(uc2));
        RemoteOperationAlarmV1_1 data2 = new RemoteOperationAlarmV1_1();
        igniteEvent2.setEventData(data2);

        Ro entity2 = new Ro();
        entity2.setSchemaVersion(Version.V1_0);
        entity2.setRoEvent(igniteEvent2);

        roDAO.save(entity2);

        // entity for another vehicle

        IgniteEventImpl igniteEvent3 = new IgniteEventImpl();
        igniteEvent3.setVehicleId("vin7777777");
        igniteEvent3.setSchemaVersion(Version.V1_0);
        igniteEvent3.setEventId(REMOTEOPERATIONALARMV1_1);
        igniteEvent3.setMessageId("10003");
        UserContext uc3 = new UserContext();
        uc3.setUserId("user_test_02");
        uc3.setRole("VO");
        igniteEvent3.setUserContextInfo(Arrays.asList(uc3));
        RemoteOperationAlarmV1_1 data3 = new RemoteOperationAlarmV1_1();
        igniteEvent3.setEventData(data3);

        Ro entity3 = new Ro();
        entity2.setSchemaVersion(Version.V1_0);
        entity2.setRoEvent(igniteEvent3);

        roDAO.save(entity3);
    }

    @Test
    public void testEOLUserValidationCase() throws Exception {

        RemoteOperationDoorsV1_1 roDoor = new RemoteOperationDoorsV1_1();
        roDoor.setOrigin("EOLUser");
        roDoor.setRoRequestId("roReq123");
        roDoor.setUserId("fakeUserId");
        roDoor.setState(State.LOCKED);
        IgniteEvent igImpl = getIgniteEvent(roDoor, Constants.REMOTEOPERATIONDOORS);
        insertMongoData(igImpl);

        RemoteOperationResponseV1_1 roRes = getROResponseData(Response.SUCCESS);

        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);
        Assert.assertTrue(roDAO.findAll().size() > 0);

        roDAO.deleteAll();
    }

    @Test
    public void testROPartnerId() throws Exception {

        RemoteOperationDoorsV1_1 roDoor = new RemoteOperationDoorsV1_1();
        roDoor.setOrigin("EOLUser");
        roDoor.setRoRequestId("roReq123");
        roDoor.setUserId("fakeUserId");
        roDoor.setState(State.LOCKED);
        roDoor.setPartnerId("testPartnerId");
        IgniteEvent igImpl = getIgniteEvent(roDoor, Constants.REMOTEOPERATIONDOORS);
        insertMongoData(igImpl);

        RemoteOperationResponseV1_1 roRes = getROResponseData(Response.SUCCESS);

        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);
        Assert.assertTrue(roDAO.findAll().size() > 0);

        roDAO.deleteAll();
    }

    // when no engine status, expect one notification forward and one dff forward
    @Test
    public void testEngineStop() {

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");
        sp.init(spc);

        RemoteOperationEngineV1_1 remoteOperationEngine = getROEngineData(RemoteOperationEngineV1_1.State.STOPPED);
        IgniteEvent igImpl = getIgniteEvent(remoteOperationEngine, Constants.REMOTEOPERATIONENGINE);

        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, igImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);

        Assert.assertTrue(!roDAO.findAll().isEmpty());
        verify(spc, times(1)).forward(
                Mockito.any(Record.class));

    }

    // RO 2.0 test cases
    @Test
    public void testRoTrunkLocked() {

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");
        sp.init(spc);

        RemoteOperationTrunkV2_0 trunkData = getROTrunkData(RemoteOperationTrunkV2_0.State.LOCKED);
        IgniteEventImpl igImpl = getIgniteEvent(trunkData, Constants.REMOTEOPERATIONTRUNK);

        Record<IgniteKey<?>, IgniteEvent> kafkaRecord3 = new Record<>(igniteKey, igImpl, System.currentTimeMillis());
        sp.process(kafkaRecord3);

        RemoteOperationResponseV1_1 roRes1 = getROResponseData(Response.SUCCESS_CONTINUE);
        eventImpl = getIgniteEvent(roRes1, Constants.REMOTEOPERATIONRESPONSE);
        eventImpl.setCorrelationId(igImpl.getMessageId());
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);

        RemoteOperationResponseV1_1 roRes2 = getROResponseData(Response.SUCCESS);
        eventImpl = getIgniteEvent(roRes2, Constants.REMOTEOPERATIONRESPONSE);
        eventImpl.setCorrelationId(igImpl.getMessageId());
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord4 = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord4);

        Assert.assertTrue(!roDAO.findAll().isEmpty());
        List<Ro> roEntity = roDAO.findAll();
        Assert.assertEquals(Constants.REMOTEOPERATIONTRUNK, roDAO.findAll().get(0).getRoEvent().getEventId());
        RemoteOperationTrunkV2_0 eventData = (RemoteOperationTrunkV2_0)
                roDAO.findAll().get(0).getRoEvent().getEventData();
        Assert.assertEquals(RemoteOperationTrunkV2_0.State.LOCKED, eventData.getState());
        Assert.assertEquals(TestConstants.TWO, roEntity.get(0).getRoResponseList().size());
        List<IgniteEvent> roResponseList = roEntity.get(0).getRoResponseList();
        IgniteEvent response1 = roResponseList.get(0);
        IgniteEvent response2 = roResponseList.get(1);
        Assert.assertEquals(Constants.REMOTEOPERATIONRESPONSE, response1.getEventId());
        Assert.assertEquals(Constants.REMOTEOPERATIONRESPONSE, response2.getEventId());
        RemoteOperationResponseV1_1 response1EventData = (RemoteOperationResponseV1_1) response1.getEventData();
        RemoteOperationResponseV1_1 response2EventData = (RemoteOperationResponseV1_1) response2.getEventData();
        Assert.assertEquals(Response.SUCCESS_CONTINUE, response1EventData.getResponse());
        Assert.assertEquals(Response.SUCCESS, response2EventData.getResponse());
        roDAO.deleteAll();

    }

    @Test
    public void testRoTrunkUnLocked() {

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");
        sp.init(spc);

        RemoteOperationTrunkV2_0 trunkData = getROTrunkData(RemoteOperationTrunkV2_0.State.UNLOCKED);
        IgniteEventImpl igImpl = getIgniteEvent(trunkData, Constants.REMOTEOPERATIONTRUNK);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord33 = new Record<>(igniteKey, igImpl, System.currentTimeMillis());

        sp.process(kafkaRecord33);

        RemoteOperationResponseV1_1 roRes1 = getROResponseData(Response.SUCCESS_CONTINUE);
        eventImpl = getIgniteEvent(roRes1, Constants.REMOTEOPERATIONRESPONSE);
        eventImpl.setCorrelationId(igImpl.getMessageId());
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);

        RemoteOperationResponseV1_1 roRes2 = getROResponseData(Response.SUCCESS);
        eventImpl = getIgniteEvent(roRes2, Constants.REMOTEOPERATIONRESPONSE);
        eventImpl.setCorrelationId(igImpl.getMessageId());
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord44 =
                new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord44);

        Assert.assertTrue(!roDAO.findAll().isEmpty());
        List<Ro> roEntity = roDAO.findAll();
        Assert.assertEquals(Constants.REMOTEOPERATIONTRUNK, roDAO.findAll().get(0).getRoEvent().getEventId());
        RemoteOperationTrunkV2_0 eventData = (RemoteOperationTrunkV2_0)
                roDAO.findAll().get(0).getRoEvent().getEventData();
        Assert.assertEquals(RemoteOperationTrunkV2_0
                .State.UNLOCKED, eventData.getState());
        Assert.assertEquals(TestConstants.TWO, roEntity.get(0).getRoResponseList().size());
        List<IgniteEvent> roResponseList = roEntity.get(0).getRoResponseList();
        IgniteEvent response1 = roResponseList.get(0);
        IgniteEvent response2 = roResponseList.get(1);
        Assert.assertEquals(Constants.REMOTEOPERATIONRESPONSE, response1.getEventId());
        Assert.assertEquals(Constants.REMOTEOPERATIONRESPONSE, response2.getEventId());
        RemoteOperationResponseV1_1 response1EventData = (RemoteOperationResponseV1_1) response1.getEventData();
        RemoteOperationResponseV1_1 response2EventData = (RemoteOperationResponseV1_1) response2.getEventData();
        Assert.assertEquals(Response.SUCCESS_CONTINUE, response1EventData.getResponse());
        Assert.assertEquals(Response.SUCCESS, response2EventData.getResponse());
        roDAO.deleteAll();
    }

    @Test
    public void testRoGloveBoxLocked() {

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");
        sp.init(spc);

        RemoteOperationGloveBoxV2_0 gloveBoxData = getGloveBoxData(RemoteOperationGloveBoxV2_0.State.LOCKED);
        IgniteEventImpl igImpl = getIgniteEvent(gloveBoxData, Constants.REMOTEOPERATIONGLOVEBOX);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord0 = new Record<>(igniteKey, igImpl, System.currentTimeMillis());
        sp.process(kafkaRecord0);

        RemoteOperationResponseV1_1 roRes1 = getROResponseData(Response.SUCCESS_CONTINUE);
        eventImpl = getIgniteEvent(roRes1, Constants.REMOTEOPERATIONRESPONSE);
        eventImpl.setCorrelationId(igImpl.getMessageId());
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);

        RemoteOperationResponseV1_1 roRes2 = getROResponseData(Response.SUCCESS);
        eventImpl = getIgniteEvent(roRes2, Constants.REMOTEOPERATIONRESPONSE);
        eventImpl.setCorrelationId(igImpl.getMessageId());
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord3 = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord3);

        Assert.assertTrue(!roDAO.findAll().isEmpty());
        List<Ro> roEntity = roDAO.findAll();
        Assert.assertEquals(Constants.REMOTEOPERATIONGLOVEBOX, roDAO.findAll().get(0).getRoEvent().getEventId());
        RemoteOperationGloveBoxV2_0 eventData = (RemoteOperationGloveBoxV2_0)
                roDAO.findAll().get(0).getRoEvent().getEventData();
        Assert.assertEquals(RemoteOperationGloveBoxV2_0.State.LOCKED, eventData.getState());
        Assert.assertEquals(TestConstants.TWO, roEntity.get(0).getRoResponseList().size());
        List<IgniteEvent> roResponseList = roEntity.get(0).getRoResponseList();
        IgniteEvent response1 = roResponseList.get(0);
        IgniteEvent response2 = roResponseList.get(1);
        Assert.assertEquals(Constants.REMOTEOPERATIONRESPONSE, response1.getEventId());
        Assert.assertEquals(Constants.REMOTEOPERATIONRESPONSE, response2.getEventId());
        RemoteOperationResponseV1_1 response1EventData = (RemoteOperationResponseV1_1) response1.getEventData();
        RemoteOperationResponseV1_1 response2EventData = (RemoteOperationResponseV1_1) response2.getEventData();
        Assert.assertEquals(Response.SUCCESS_CONTINUE, response1EventData.getResponse());
        Assert.assertEquals(Response.SUCCESS, response2EventData.getResponse());
        roDAO.deleteAll();
    }

    @Test
    public void testRoGloveBoxUnLocked() {

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");
        sp.init(spc);

        RemoteOperationGloveBoxV2_0 gloveBoxData = getGloveBoxData(RemoteOperationGloveBoxV2_0.State.UNLOCKED);
        IgniteEventImpl igImpl = getIgniteEvent(gloveBoxData, Constants.REMOTEOPERATIONGLOVEBOX);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord0 = new Record<>(igniteKey, igImpl, System.currentTimeMillis());
        sp.process(kafkaRecord0);

        RemoteOperationResponseV1_1 roRes1 = getROResponseData(Response.SUCCESS_CONTINUE);
        eventImpl = getIgniteEvent(roRes1, Constants.REMOTEOPERATIONRESPONSE);
        eventImpl.setCorrelationId(igImpl.getMessageId());
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);

        RemoteOperationResponseV1_1 roRes2 = getROResponseData(Response.SUCCESS);
        eventImpl = getIgniteEvent(roRes2, Constants.REMOTEOPERATIONRESPONSE);
        eventImpl.setCorrelationId(igImpl.getMessageId());
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord2 = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord2);

        Assert.assertTrue(!roDAO.findAll().isEmpty());
        List<Ro> roEntity = roDAO.findAll();
        Assert.assertEquals(Constants.REMOTEOPERATIONGLOVEBOX, roDAO.findAll().get(0).getRoEvent().getEventId());
        RemoteOperationGloveBoxV2_0 eventData = (RemoteOperationGloveBoxV2_0)
                roDAO.findAll().get(0).getRoEvent().getEventData();
        Assert.assertEquals(RemoteOperationGloveBoxV2_0
                .State.UNLOCKED, eventData.getState());
        Assert.assertEquals(TestConstants.TWO, roEntity.get(0).getRoResponseList().size());
        List<IgniteEvent> roResponseList = roEntity.get(0).getRoResponseList();
        IgniteEvent response1 = roResponseList.get(0);
        IgniteEvent response2 = roResponseList.get(1);
        Assert.assertEquals(Constants.REMOTEOPERATIONRESPONSE, response1.getEventId());
        Assert.assertEquals(Constants.REMOTEOPERATIONRESPONSE, response2.getEventId());
        RemoteOperationResponseV1_1 response1EventData = (RemoteOperationResponseV1_1) response1.getEventData();
        RemoteOperationResponseV1_1 response2EventData = (RemoteOperationResponseV1_1) response2.getEventData();
        Assert.assertEquals(Response.SUCCESS_CONTINUE, response1EventData.getResponse());
        Assert.assertEquals(Response.SUCCESS, response2EventData.getResponse());
        roDAO.deleteAll();
    }

    @Test
    public void testRoClimateOn() {

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");
        sp.init(spc);

        RemoteOperationClimateV2_0 climateBoxData = getClimateData(RemoteOperationClimateV2_0.State.ON);
        IgniteEventImpl igImpl = getIgniteEvent(climateBoxData, Constants.REMOTEOPERATIONCLIMATE);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord0 = new Record<>(igniteKey, igImpl, System.currentTimeMillis());
        sp.process(kafkaRecord0);

        RemoteOperationResponseV1_1 roRes1 = getROResponseData(Response.SUCCESS_CONTINUE);
        eventImpl = getIgniteEvent(roRes1, Constants.REMOTEOPERATIONRESPONSE);
        eventImpl.setCorrelationId(igImpl.getMessageId());
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);

        RemoteOperationResponseV1_1 roRes2 = getROResponseData(Response.SUCCESS);
        eventImpl = getIgniteEvent(roRes2, Constants.REMOTEOPERATIONRESPONSE);
        eventImpl.setCorrelationId(igImpl.getMessageId());
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord2 = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord2);

        Assert.assertTrue(!roDAO.findAll().isEmpty());
        List<Ro> roEntity = roDAO.findAll();
        Assert.assertEquals(Constants.REMOTEOPERATIONCLIMATE, roDAO.findAll().get(0).getRoEvent().getEventId());
        RemoteOperationClimateV2_0 eventData = (RemoteOperationClimateV2_0)
                roDAO.findAll().get(0).getRoEvent().getEventData();
        Assert.assertEquals(RemoteOperationClimateV2_0.State.ON, eventData.getState());
        Assert.assertEquals(TestConstants.TWO, roEntity.get(0).getRoResponseList().size());
        List<IgniteEvent> roResponseList = roEntity.get(0).getRoResponseList();
        IgniteEvent response1 = roResponseList.get(0);
        IgniteEvent response2 = roResponseList.get(1);
        Assert.assertEquals(Constants.REMOTEOPERATIONRESPONSE, response1.getEventId());
        Assert.assertEquals(Constants.REMOTEOPERATIONRESPONSE, response2.getEventId());
        RemoteOperationResponseV1_1 response1EventData = (RemoteOperationResponseV1_1) response1.getEventData();
        RemoteOperationResponseV1_1 response2EventData = (RemoteOperationResponseV1_1) response2.getEventData();
        Assert.assertEquals(Response.SUCCESS_CONTINUE, response1EventData.getResponse());
        Assert.assertEquals(Response.SUCCESS, response2EventData.getResponse());
        roDAO.deleteAll();
    }

    @Test
    public void testRoClimateOff() {

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");
        sp.init(spc);

        RemoteOperationClimateV2_0 climateBoxData = getClimateData(RemoteOperationClimateV2_0.State.OFF);
        IgniteEventImpl igImpl = getIgniteEvent(climateBoxData, Constants.REMOTEOPERATIONCLIMATE);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord0 = new Record<>(igniteKey, igImpl, System.currentTimeMillis());
        sp.process(kafkaRecord0);

        RemoteOperationResponseV1_1 roRes1 = getROResponseData(Response.SUCCESS_CONTINUE);
        eventImpl = getIgniteEvent(roRes1, Constants.REMOTEOPERATIONRESPONSE);
        eventImpl.setCorrelationId(igImpl.getMessageId());
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);

        RemoteOperationResponseV1_1 roRes2 = getROResponseData(Response.SUCCESS);
        eventImpl = getIgniteEvent(roRes2, Constants.REMOTEOPERATIONRESPONSE);
        eventImpl.setCorrelationId(igImpl.getMessageId());
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord2 = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord2);

        Assert.assertTrue(!roDAO.findAll().isEmpty());
        List<Ro> roEntity = roDAO.findAll();
        Assert.assertEquals(Constants.REMOTEOPERATIONCLIMATE, roDAO.findAll().get(0).getRoEvent().getEventId());
        RemoteOperationClimateV2_0 eventData = (RemoteOperationClimateV2_0) roDAO
                .findAll().get(0).getRoEvent().getEventData();
        Assert.assertEquals(RemoteOperationClimateV2_0.State.OFF, eventData.getState());
        Assert.assertEquals(TestConstants.TWO, roEntity.get(0).getRoResponseList().size());
        List<IgniteEvent> roResponseList = roEntity.get(0).getRoResponseList();
        IgniteEvent response1 = roResponseList.get(0);
        IgniteEvent response2 = roResponseList.get(1);
        Assert.assertEquals(Constants.REMOTEOPERATIONRESPONSE, response1.getEventId());
        Assert.assertEquals(Constants.REMOTEOPERATIONRESPONSE, response2.getEventId());
        RemoteOperationResponseV1_1 response1EventData = (RemoteOperationResponseV1_1) response1.getEventData();
        RemoteOperationResponseV1_1 response2EventData = (RemoteOperationResponseV1_1) response2.getEventData();
        Assert.assertEquals(Response.SUCCESS_CONTINUE, response1EventData.getResponse());
        Assert.assertEquals(Response.SUCCESS, response2EventData.getResponse());
        roDAO.deleteAll();
    }

    @Test
    public void testRoClimateAutoOnComfortOff() {

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");
        sp.init(spc);

        RemoteOperationClimateV2_0 climateBoxData = getClimateData(
                RemoteOperationClimateV2_0.State.COMFORT_AUTO_ON_MODE_DISABLED);
        IgniteEventImpl igImpl = getIgniteEvent(climateBoxData, Constants.REMOTEOPERATIONCLIMATE);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord0 = new Record<>(igniteKey, igImpl, System.currentTimeMillis());
        sp.process(kafkaRecord0);

        RemoteOperationResponseV1_1 roRes1 = getROResponseData(Response.SUCCESS_CONTINUE);
        eventImpl = getIgniteEvent(roRes1, Constants.REMOTEOPERATIONRESPONSE);
        eventImpl.setCorrelationId(igImpl.getMessageId());
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);

        RemoteOperationResponseV1_1 roRes2 = getROResponseData(Response.SUCCESS);
        eventImpl = getIgniteEvent(roRes2, Constants.REMOTEOPERATIONRESPONSE);
        eventImpl.setCorrelationId(igImpl.getMessageId());
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord2 = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord2);

        Assert.assertTrue(!roDAO.findAll().isEmpty());
        List<Ro> roEntity = roDAO.findAll();
        Assert.assertEquals(Constants.REMOTEOPERATIONCLIMATE, roDAO.findAll().get(0).getRoEvent().getEventId());
        RemoteOperationClimateV2_0 eventData = (RemoteOperationClimateV2_0)
                roDAO.findAll().get(0).getRoEvent().getEventData();
        Assert.assertEquals(RemoteOperationClimateV2_0
                .State.COMFORT_AUTO_ON_MODE_DISABLED, eventData.getState());
        Assert.assertEquals(TestConstants.TWO, roEntity.get(0).getRoResponseList().size());
        List<IgniteEvent> roResponseList = roEntity.get(0).getRoResponseList();
        IgniteEvent response1 = roResponseList.get(0);
        IgniteEvent response2 = roResponseList.get(1);
        Assert.assertEquals(Constants.REMOTEOPERATIONRESPONSE, response1.getEventId());
        Assert.assertEquals(Constants.REMOTEOPERATIONRESPONSE, response2.getEventId());
        RemoteOperationResponseV1_1 response1EventData = (RemoteOperationResponseV1_1) response1.getEventData();
        RemoteOperationResponseV1_1 response2EventData = (RemoteOperationResponseV1_1) response2.getEventData();
        Assert.assertEquals(Response.SUCCESS_CONTINUE, response1EventData.getResponse());
        Assert.assertEquals(Response.SUCCESS, response2EventData.getResponse());
        roDAO.deleteAll();
    }

    @Test
    public void testRoClimateAutoOnComfortAllStart() {

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");
        sp.init(spc);

        RemoteOperationClimateV2_0 climateBoxData = getClimateData(
                RemoteOperationClimateV2_0.State.COMFORT_AUTO_ON_MODE_START_ALL);
        IgniteEventImpl igImpl = getIgniteEvent(climateBoxData, Constants.REMOTEOPERATIONCLIMATE);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord0 = new Record<>(igniteKey, igImpl, System.currentTimeMillis());
        sp.process(kafkaRecord0);

        RemoteOperationResponseV1_1 roRes1 = getROResponseData(Response.SUCCESS_CONTINUE);
        eventImpl = getIgniteEvent(roRes1, Constants.REMOTEOPERATIONRESPONSE);
        eventImpl.setCorrelationId(igImpl.getMessageId());
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);

        RemoteOperationResponseV1_1 roRes2 = getROResponseData(Response.SUCCESS);
        eventImpl = getIgniteEvent(roRes2, Constants.REMOTEOPERATIONRESPONSE);
        eventImpl.setCorrelationId(igImpl.getMessageId());
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord2 = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord2);

        Assert.assertTrue(!roDAO.findAll().isEmpty());
        List<Ro> roEntity = roDAO.findAll();
        Assert.assertEquals(Constants.REMOTEOPERATIONCLIMATE, roDAO.findAll().get(0).getRoEvent().getEventId());
        RemoteOperationClimateV2_0 eventData = (RemoteOperationClimateV2_0) roDAO
                .findAll().get(0).getRoEvent().getEventData();
        Assert.assertEquals(RemoteOperationClimateV2_0.State.COMFORT_AUTO_ON_MODE_START_ALL,
                eventData.getState());
        Assert.assertEquals(TestConstants.TWO, roEntity.get(0).getRoResponseList().size());
        List<IgniteEvent> roResponseList = roEntity.get(0).getRoResponseList();
        IgniteEvent response1 = roResponseList.get(0);
        IgniteEvent response2 = roResponseList.get(1);
        Assert.assertEquals(Constants.REMOTEOPERATIONRESPONSE, response1.getEventId());
        Assert.assertEquals(Constants.REMOTEOPERATIONRESPONSE, response2.getEventId());
        RemoteOperationResponseV1_1 response1EventData = (RemoteOperationResponseV1_1) response1.getEventData();
        RemoteOperationResponseV1_1 response2EventData = (RemoteOperationResponseV1_1) response2.getEventData();
        Assert.assertEquals(Response.SUCCESS_CONTINUE, response1EventData.getResponse());
        Assert.assertEquals(Response.SUCCESS, response2EventData.getResponse());
        roDAO.deleteAll();
    }

    @Test
    public void testCheckMongoDBStatusAfterGetSuccessResponse() {

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");
        sp.init(spc);
        //Invoke any RO request
        RemoteOperationClimateV2_0 climateBoxData = getClimateData(
                RemoteOperationClimateV2_0.State.COMFORT_AUTO_ON_MODE_START_ALL);
        IgniteEvent igImpl = getIgniteEvent(climateBoxData, Constants.REMOTEOPERATIONCLIMATE);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord0 = new Record<>(igniteKey, igImpl, System.currentTimeMillis());
        sp.process(kafkaRecord0);

        RemoteOperationResponseV1_1 roRes1 = getROResponseData(Response.SUCCESS);
        eventImpl = getIgniteEvent(roRes1, Constants.REMOTEOPERATIONRESPONSE);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);

        Assert.assertTrue(!roDAO.findAll().isEmpty());
        List<Ro> roEntity = roDAO.findAll();

        Assert.assertEquals(ROStatus.PROCESSED_SUCCESS, roEntity.get(0).getRoStatus());

        roDAO.deleteAll();

    }

    @Test
    public void testCheckMongoDBStatusAfterGetCustomExtensionResponse() {

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");
        sp.init(spc);
        //Invoke any RO request
        RemoteOperationClimateV2_0 climateBoxData = getClimateData(
                RemoteOperationClimateV2_0.State.COMFORT_AUTO_ON_MODE_START_ALL);
        IgniteEvent igImpl = getIgniteEvent(climateBoxData, Constants.REMOTEOPERATIONCLIMATE);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord0 = new Record<>(igniteKey, igImpl, System.currentTimeMillis());
        sp.process(kafkaRecord0);
        RemoteOperationResponseV1_1 roRes1 = getROResponseData(Response.CUSTOM_EXTENSION);
        GenericCustomExtension genericCustomExtension = new GenericCustomExtension();
        Map<String, Object> customData = new HashMap<>();
        customData.put("response", "FAILED_IGNITION_ON");
        genericCustomExtension.setCustomData(customData);
        roRes1.setCustomExtension(genericCustomExtension);
        eventImpl = getIgniteEvent(roRes1, Constants.REMOTEOPERATIONRESPONSE);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);

        Assert.assertTrue(!roDAO.findAll().isEmpty());
        List<Ro> roEntity = roDAO.findAll();

        Assert.assertEquals(ROStatus.PROCESSED_FAILED, roEntity.get(0).getRoStatus());

        roDAO.deleteAll();

    }

    @Test
    public void testCheckTTL_NOTEXPIREDAndForward() {
        RemoteOperationDoorsV1_1 doorData = getRODoorData(State.UNLOCKED);

        IgniteEventImpl expiredEventImpl = new IgniteEventImpl();
        expiredEventImpl.setEventId(Constants.REMOTEOPERATIONDOORS);
        expiredEventImpl.setVersion(Version.V1_0);
        expiredEventImpl.setTimestamp(System.currentTimeMillis());
        expiredEventImpl.setEventData(doorData);
        expiredEventImpl.setRequestId("roReq123");
        expiredEventImpl.setVehicleId("vehicleId");
        expiredEventImpl.setTimezone((short) 0);
        expiredEventImpl.setMessageId("1");
        expiredEventImpl.setBizTransactionId("sessionId");
        expiredEventImpl.setDeviceDeliveryCutoff(-TestConstants.MINUS_ONE_LONG);
        insertMongoData(expiredEventImpl);
        //enqueue
        RQueue<AbstractIgniteEvent> queue = redissonClient.getQueue(CachedKeyUtil.getROQueueKey(expiredEventImpl));
        //test check ttl
        queue.offer(expiredEventImpl);
        queue.offer(expiredEventImpl);

        RemoteOperationResponseV1_1 roRes = new RemoteOperationResponseV1_1();
        roRes.setOrigin("ROSupportOwner");
        roRes.setResponse(Response.SUCCESS);
        roRes.setRoRequestId("roReq123");
        roRes.setUserId("userId");

        eventImpl = getIgniteEvent(roRes, Constants.REMOTEOPERATIONRESPONSE);

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);

        Assert.assertFalse(roDAO.findAll().isEmpty());
        List<Ro> roEntity = roDAO.findAll();

        Assert.assertEquals(ROStatus.TTL_EXPIRED, roEntity.get(0).getRoStatus());

        roDAO.deleteAll();

        RQueue<AbstractIgniteEvent> queue2 = redissonClient.getQueue(CachedKeyUtil.getROQueueKey(expiredEventImpl));
        AbstractIgniteEvent headEvent = queue.peek();
        Assert.assertNotNull(headEvent);
    }

    @Test
    public void testCheckTTL_EXPIREDInRedisQueue() {
        RemoteOperationDoorsV1_1 doorData = getRODoorData(State.UNLOCKED);
        IgniteEvent igImpl = getIgniteEvent(doorData, Constants.REMOTEOPERATIONDOORS);
        insertMongoData(igImpl);

        IgniteEventImpl eventImpl = new IgniteEventImpl();
        eventImpl.setEventId(Constants.REMOTEOPERATIONALARM);
        eventImpl.setVersion(Version.V1_0);
        eventImpl.setTimestamp(System.currentTimeMillis() - TestConstants.INT_180 * TestConstants.THOUSAND_INT);
        eventImpl.setEventData(doorData);
        eventImpl.setRequestId("roReq123");
        eventImpl.setVehicleId("vehicleId");
        eventImpl.setTimezone((short) 0);
        eventImpl.setMessageId("1");
        eventImpl.setBizTransactionId("sessionId");
        eventImpl.setDeviceDeliveryCutoff(-TestConstants.MINUS_ONE_LONG);
        redissonClient.getQueue(CachedKeyUtil.getROQueueKey(eventImpl)).offer(eventImpl);
        RQueue<AbstractIgniteEvent> queue = redissonClient.getQueue(CachedKeyUtil.getROQueueKey(eventImpl));
        queue.offer(eventImpl);
        //test check ttl
        queue.offer(eventImpl);
        queue.offer(eventImpl);

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);

        Assert.assertFalse(roDAO.findAll().isEmpty());
        List<Ro> roEntity = roDAO.findAll();

        Assert.assertEquals(null, roEntity.get(0).getRoStatus());

        roDAO.deleteAll();

        RQueue<AbstractIgniteEvent> queue2 = redissonClient.getQueue(CachedKeyUtil.getROQueueKey(eventImpl));
        AbstractIgniteEvent headEvent = queue.peek();
        Assert.assertNotNull(headEvent);
    }

    @Test
    public void testNullValueAndNullKey() {
        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord0 = new Record<>(null, null, System.currentTimeMillis());
        sp.process(kafkaRecord0);
        //Invoke any RO request
        RemoteOperationClimateV2_0 climateBoxData = getClimateData(
                RemoteOperationClimateV2_0.State.COMFORT_AUTO_ON_MODE_START_ALL);
        IgniteEvent igImpl = getIgniteEvent(climateBoxData, Constants.REMOTEOPERATIONCLIMATE);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord1 = new Record<>(null, igImpl, System.currentTimeMillis());

        sp.process(kafkaRecord1);
        Assert.assertNotNull(eventImpl);
    }

    @Test
    public void testDELETE_SCHEDULE() {
        RemoteOperationScheduleV1 roEngineData = new RemoteOperationScheduleV1();
        roEngineData.setRoRequestId("schedulerKey");
        roEngineData.setUserId("userId");
        roEngineData.setSchedulerKey("schedulerKey");

        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventData(roEngineData);
        igniteEvent.setEventId(org.eclipse.ecsp.ro.constants.Constants.EVENT_ID_DELETE_SCHEDULE);
        igniteEvent.setVersion(Version.V1_1);
        igniteEvent.setVehicleId(vehicleId);
        igniteEvent.setRequestId("requestId");
        igniteEvent.setBizTransactionId("sessionId");
        igniteEvent.setTimestamp(System.currentTimeMillis());

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord1 = new Record<>(igniteKey,
                igniteEvent, System.currentTimeMillis());

        sp.process(kafkaRecord1);
        Assert.assertNotNull(eventImpl);

    }

    @Test
    public void testCreate_SCHEDULE() {
        RemoteOperationScheduleV1 roEngineData = new RemoteOperationScheduleV1();
        roEngineData.setRoRequestId("schedulerKey");
        roEngineData.setUserId("userId");
        roEngineData.setSchedulerKey("schedulerKey");
        Schedule schedule = new Schedule();
        schedule.setFirstScheduleTs(TestConstants.THRESHOLD_LONG);
        roEngineData.setSchedule(schedule);
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventData(roEngineData);
        igniteEvent.setEventId(Constants.REMOTEOPERATIONALARM);
        igniteEvent.setVersion(Version.V1_1);
        igniteEvent.setVehicleId(vehicleId);
        igniteEvent.setRequestId("requestId");
        igniteEvent.setBizTransactionId("sessionId");
        igniteEvent.setTimestamp(System.currentTimeMillis());

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord1 = new Record<>(igniteKey,
                igniteEvent, System.currentTimeMillis());

        sp.process(kafkaRecord1);
        Assert.assertNotNull(eventImpl);
    }

    @Test
    public void testDelete_SCHEDULE() {
        RemoteOperationScheduleV1 roEngineData = new RemoteOperationScheduleV1();
        roEngineData.setRoRequestId("schedulerKey");
        roEngineData.setUserId("userId");
        roEngineData.setSchedulerKey("schedulerKey");
        Schedule schedule = new Schedule();
        schedule.setFirstScheduleTs(TestConstants.THRESHOLD_LONG);
        roEngineData.setSchedule(schedule);
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventData(roEngineData);
        igniteEvent.setEventId(Constants.REMOTEOPERATIONALARM);
        igniteEvent.setVersion(Version.V1_1);
        igniteEvent.setVehicleId(vehicleId);
        igniteEvent.setRequestId("requestId");
        igniteEvent.setBizTransactionId("sessionId");
        igniteEvent.setTimestamp(System.currentTimeMillis());

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord1 = new Record<>(igniteKey,
                igniteEvent, System.currentTimeMillis());

        sp.process(kafkaRecord1);
        Assert.assertNotNull(eventImpl);
    }

    @Test
    public void testScheduleNotification() throws IOException {

        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        ScheduleNotificationEventData scheduleNotificationEventData = new ScheduleNotificationEventData();
        scheduleNotificationEventData.setScheduleIdId("scheduleUserId");
        byte[] payLoad = IOUtils
                .toString(ROStreamProcessorUnitTest.class.getResourceAsStream(
                        "/ScheduleNotificationEventData.json"), "UTF-8")
                .getBytes(StandardCharsets.UTF_8);
        scheduleNotificationEventData.setPayload(payLoad);
        igniteEvent.setEventData(scheduleNotificationEventData);
        igniteEvent.setEventId("testEventId");
        igniteEvent.setVersion(Version.V1_1);
        igniteEvent.setTimestamp(System.currentTimeMillis());
        igniteEvent.setVehicleId("bmw123");
        igniteEvent.setRequestId("requestId123");
        igniteEvent.setBizTransactionId("bizTransactionId");
        igniteEvent.setEventId(EventID.SCHEDULE_NOTIFICATION_EVENT);

        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);

        Record<IgniteKey<?>, IgniteEvent> kafkaRecord1 = new Record<>(igniteKey,
                igniteEvent, System.currentTimeMillis());

        sp.process(kafkaRecord1);
        Assert.assertNotNull(eventImpl);
    }

    @Test
    public void getRoCommandTest() {
        RemoteOperationAlarmV1_1 remoteOperationAlarmV11 = new RemoteOperationAlarmV1_1();
        remoteOperationAlarmV11.setState(RemoteOperationAlarmV1_1.State.ON);
        Object getRoCommand = ReflectionTestUtils.invokeMethod(roRequestStreamProcessor,
                "getRoCommand", remoteOperationAlarmV11);
        Assert.assertNotNull(getRoCommand);

        RemoteOperationClimateV1_1 remoteOperationClimateV11 = new RemoteOperationClimateV1_1();
        remoteOperationClimateV11.setState(RemoteOperationClimateV1_1.State.ON);
        Object getRoCommand2 = ReflectionTestUtils.invokeMethod(roRequestStreamProcessor,
                "getRoCommand", remoteOperationClimateV11);
        Assert.assertNotNull(getRoCommand2);

        RemoteOperationDriverDoorV1_1 remoteOperationDriverDoorV11 = new RemoteOperationDriverDoorV1_1();
        remoteOperationDriverDoorV11.setState(RemoteOperationDriverDoorV1_1.State.LOCKED);
        Object getRoCommand3 = ReflectionTestUtils.invokeMethod(roRequestStreamProcessor,
                "getRoCommand", remoteOperationDriverDoorV11);
        Assert.assertNotNull(getRoCommand3);

        RemoteOperationDriverWindowV1_1 remoteOperationDriverWindowV11 = new RemoteOperationDriverWindowV1_1();
        remoteOperationDriverWindowV11.setState(RemoteOperationDriverWindowV1_1.State.OPENED);
        Object getRoCommand4 = ReflectionTestUtils.invokeMethod(
                roRequestStreamProcessor, "getRoCommand", remoteOperationDriverWindowV11);
        Assert.assertNotNull(getRoCommand4);

        RemoteOperationEngineV1_1 remoteOperationEngineV11 = new RemoteOperationEngineV1_1();
        remoteOperationEngineV11.setState(RemoteOperationEngineV1_1.State.STARTED);
        Object getRoCommand5 = ReflectionTestUtils.invokeMethod(
                roRequestStreamProcessor, "getRoCommand", remoteOperationEngineV11);
        Assert.assertNotNull(getRoCommand5);

        RemoteOperationHoodV1_1 remoteOperationHoodV11 = new RemoteOperationHoodV1_1();
        remoteOperationHoodV11.setState(RemoteOperationHoodV1_1.State.OPENED);
        Object getRoCommand6 = ReflectionTestUtils.invokeMethod(
                roRequestStreamProcessor, "getRoCommand", remoteOperationHoodV11);
        Assert.assertNotNull(getRoCommand6);

        RemoteOperationHornV1_1 remoteOperationHornV11 = new RemoteOperationHornV1_1();
        remoteOperationHornV11.setState(RemoteOperationHornV1_1.State.ON);
        Object getRoCommand7 = ReflectionTestUtils.invokeMethod(
                roRequestStreamProcessor, "getRoCommand", remoteOperationHornV11);
        Assert.assertNotNull(getRoCommand7);

        RemoteOperationLiftgateV1_1 remoteOperationLiftgateV11 = new RemoteOperationLiftgateV1_1();
        remoteOperationLiftgateV11.setState(RemoteOperationLiftgateV1_1.State.OPENED);
        Object getRoCommand8 = ReflectionTestUtils.invokeMethod(
                roRequestStreamProcessor, "getRoCommand", remoteOperationLiftgateV11);
        Assert.assertNotNull(getRoCommand8);

        RemoteOperationLightsV1_1 remoteOperationLightsV11 = new RemoteOperationLightsV1_1();
        remoteOperationLightsV11.setState(RemoteOperationLightsV1_1.State.ON);
        Object getRoCommand9 = ReflectionTestUtils.invokeMethod(
                roRequestStreamProcessor, "getRoCommand", remoteOperationLightsV11);
        Assert.assertNotNull(getRoCommand9);

        RemoteOperationWindowsV1_1 remoteOperationWindowsV11 = new RemoteOperationWindowsV1_1();
        remoteOperationWindowsV11.setState(RemoteOperationWindowsV1_1.State.OPENED);
        Object getRoCommand10 = ReflectionTestUtils.invokeMethod(
                roRequestStreamProcessor, "getRoCommand", remoteOperationWindowsV11);
        Assert.assertNotNull(getRoCommand10);

        RemoteOperationTrunkV2_0 remoteOperationTrunkV20 = new RemoteOperationTrunkV2_0();
        remoteOperationTrunkV20.setState(RemoteOperationTrunkV2_0.State.OPENED);
        Object getRoCommand11 = ReflectionTestUtils.invokeMethod(
                roRequestStreamProcessor, "getRoCommand", remoteOperationTrunkV20);
        Assert.assertNotNull(getRoCommand11);

        RemoteOperationGloveBoxV2_0 remoteOperationGloveBoxV20 = new RemoteOperationGloveBoxV2_0();
        remoteOperationGloveBoxV20.setState(RemoteOperationGloveBoxV2_0.State.LOCKED);
        Object getRoCommand12 = ReflectionTestUtils.invokeMethod(roRequestStreamProcessor,
                "getRoCommand", remoteOperationGloveBoxV20);
        Assert.assertNotNull(getRoCommand12);

        RemoteOperationLiftgateV2_0 remoteOperationLiftgateV20 = new RemoteOperationLiftgateV2_0();
        remoteOperationLiftgateV20.setState(RemoteOperationLiftgateV2_0.State.LOCKED);
        Object getRoCommand14 = ReflectionTestUtils.invokeMethod(
                roRequestStreamProcessor, "getRoCommand", remoteOperationLiftgateV20);
        Assert.assertNotNull(getRoCommand14);

        RemoteOperationClimateV2_0 remoteOperationClimateV20 = new RemoteOperationClimateV2_0();
        remoteOperationClimateV20.setState(RemoteOperationClimateV2_0.State.OFF);
        Object getRoCommand15 = ReflectionTestUtils.invokeMethod(
                roRequestStreamProcessor, "getRoCommand", remoteOperationClimateV20);
        Assert.assertNotNull(getRoCommand15);
    }

    private RemoteOperationDoorsV1_1 getRODoorData(RemoteOperationDoorsV1_1.State state) {
        RemoteOperationDoorsV1_1 roDoor = new RemoteOperationDoorsV1_1();
        roDoor.setOrigin("ROSupportOwner");
        roDoor.setRoRequestId("roReq123");
        roDoor.setUserId("userId");
        roDoor.setState(state);

        return roDoor;
    }

    private RemoteOperationEngineV1_1 getROEngineData(
            RemoteOperationEngineV1_1.State state) {
        RemoteOperationEngineV1_1 roEng = new RemoteOperationEngineV1_1();
        roEng.setOrigin("THIRDPARTY2");
        roEng.setRoRequestId("roReq123");
        roEng.setUserId("userId");
        roEng.setState(state);
        roEng.setDuration(TestConstants.TEN);

        return roEng;
    }

    private RemoteOperationAlarmV1_1 getROAlarmData(RemoteOperationAlarmV1_1.State state) {
        RemoteOperationAlarmV1_1 roAlarm = new RemoteOperationAlarmV1_1();
        roAlarm.setOrigin("ROSupportOwner");
        roAlarm.setRoRequestId("roReq123");
        roAlarm.setUserId("userId");
        roAlarm.setState(state);
        roAlarm.setDuration(TestConstants.TEN);

        return roAlarm;
    }

    private RemoteOperationLightsV1_2 getROLightsONlyData(
            RemoteOperationLightsV1_2.State state) {
        RemoteOperationLightsV1_2 lightsOnly = new RemoteOperationLightsV1_2();
        lightsOnly.setOrigin("ROSupportOwner");
        lightsOnly.setRoRequestId("roReq123");
        lightsOnly.setUserId("userId");
        lightsOnly.setState(state);
        lightsOnly.setDuration(TestConstants.TEN);
        return lightsOnly;
    }

    private RemoteOperationTrunkV1_1 getROTrunkData(RemoteOperationTrunkV1_1.State state) {
        RemoteOperationTrunkV1_1 roTrunk = new RemoteOperationTrunkV1_1();
        roTrunk.setOrigin("THIRDPARTY2");
        roTrunk.setRoRequestId("roReq123");
        roTrunk.setUserId("userId");
        roTrunk.setState(state);

        return roTrunk;
    }

    private RemoteOperationTrunkV2_0 getROTrunkData(RemoteOperationTrunkV2_0.State state) {
        RemoteOperationTrunkV2_0 roTrunk = new RemoteOperationTrunkV2_0();
        roTrunk.setOrigin("THIRDPARTY2");
        roTrunk.setRoRequestId("roReq123");
        roTrunk.setUserId("userId");
        roTrunk.setState(state);

        return roTrunk;
    }

    private RemoteOperationEngineV1_1 getEngineData(RemoteOperationEngineV1_1.State state) {
        RemoteOperationEngineV1_1 roEngine = new RemoteOperationEngineV1_1();
        roEngine.setOrigin("THIRDPARTY2");
        roEngine.setRoRequestId("roReq123");
        roEngine.setUserId("userId");
        roEngine.setState(state);

        return roEngine;
    }

    private RemoteOperationGloveBoxV2_0 getGloveBoxData(
            RemoteOperationGloveBoxV2_0.State state) {
        RemoteOperationGloveBoxV2_0 roGloveBox = new RemoteOperationGloveBoxV2_0();
        roGloveBox.setOrigin("THIRDPARTY2");
        roGloveBox.setRoRequestId("roReq123");
        roGloveBox.setUserId("userId");
        roGloveBox.setState(state);

        return roGloveBox;
    }

    private RemoteOperationClimateV2_0 getClimateData(
            RemoteOperationClimateV2_0.State state) {
        RemoteOperationClimateV2_0 roClimateData = new RemoteOperationClimateV2_0();
        roClimateData.setOrigin("THIRDPARTY2");
        roClimateData.setRoRequestId("roReq123");
        roClimateData.setUserId("userId");
        roClimateData.setState(state);

        return roClimateData;
    }

    private RemoteOperationResponseV1_1 getROResponseData(
            RemoteOperationResponseV1_1.Response response) {
        RemoteOperationResponseV1_1 roRes = new RemoteOperationResponseV1_1();
        roRes.setOrigin("ROSupportOwner");
        roRes.setResponse(response);
        roRes.setRoRequestId("roReq123");
        roRes.setUserId("userId");
        return roRes;
    }

    private IgniteEventImpl getIgniteEvent(AbstractRoEventData eventData, String eventId) {

        IgniteEventImpl eventImpl = new IgniteEventImpl();

        eventImpl.setBizTransactionId("bizTransactionId");
        eventImpl.setEventData(eventData);
        eventImpl.setEventId(eventId);
        eventImpl.setSchemaVersion(Version.V1_0);
        eventImpl.setTimestamp(System.currentTimeMillis());
        eventImpl.setTimezone((short) TestConstants.THIRTY);
        eventImpl.setVehicleId("vehicleId");
        eventImpl.setMessageId("messageId");
        eventImpl.setCorrelationId("123456");
        eventImpl.setBizTransactionId("sessionId");
        eventImpl.setRequestId("roReq123");
        eventImpl.setDeviceDeliveryCutoff(-TestConstants.MINUS_ONE_LONG);

        return eventImpl;
    }

    private void insertMongoData(IgniteEvent value) {

        Ro roEntity = new Ro();
        roEntity.setSchemaVersion(value.getSchemaVersion());
        roEntity.setRoEvent(value);
        LOGGER.info("persisting to mongo ,entity :{}", roEntity.toString());
        roDAO.save(roEntity);
        LOGGER.debug("Entity inserted into MongoDB; entity :{}", roEntity.getRoEvent());
    }

    private AbstractRoEventData getObjectOf(Class c) throws InstantiationException, IllegalAccessException {
        AbstractRoEventData dd = (AbstractRoEventData) c.newInstance();
        dd.setOrigin("ROSupportOwner");
        dd.setUserId("userId");
        return dd;
    }

    /**
     * cleanUp().
     */
    @After
    public void cleanUp() {
        roDAO.deleteAll();
        cacheUtil.getCache(org.eclipse.ecsp.ro.constants.Constants.RO_CACHE_NAME).removeAll();
    }
}