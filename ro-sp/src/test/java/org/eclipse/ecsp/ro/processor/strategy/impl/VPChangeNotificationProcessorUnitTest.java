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

package org.eclipse.ecsp.ro.processor.strategy.impl;

import io.prometheus.client.CollectorRegistry;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.constant.TestConstants;
import org.eclipse.ecsp.entities.AbstractEventData;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.entities.UserContext;
import org.eclipse.ecsp.events.scheduler.ScheduleNotificationEventData;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.ro.RoScheduleV2DAOMongoImpl;
import org.eclipse.ecsp.ro.processor.ROStreamProcessor;
import org.eclipse.ecsp.ro.processor.ScheduleEventDataHandler;
import org.eclipse.ecsp.ro.processor.strategy.impl.schedule.ScheduleNotificationProcessor;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

/**
 * test class for VPChangeNotificationProcessor.
 */
@SpringBootTest(classes = org.eclipse.ecsp.Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = {"classpath:/application-test.properties", "classpath:/application-base.properties"})
public class VPChangeNotificationProcessorUnitTest extends CommonTestBase {
    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(VPChangeNotificationProcessorUnitTest.class);

    @Mock
    StreamProcessingContext spc;

    @InjectMocks
    @Autowired
    ROStreamProcessor sp;

    @Mock
    ScheduleNotificationProcessor scheduleNotificationProcessor;

    @Value("${source.topic.name}")
    private String[] sourceTopics;

    @Value("${sink.topic.name}")
    private String[] sinkTopics;

    @Value("#{${notification.responsemessage.notificationid.mapping}}")
    private Map<String, String> notificationIdMapping;

    private static String sourceTopicName;

    private static String sinkTopicName;

    private IgniteEventImpl eventImpl;

    private IgniteStringKey igniteKey;

    @Autowired
    private ScheduleEventDataHandler scheduleEventDataHandler;

    @Autowired
    private RoScheduleV2DAOMongoImpl roScheduleV2DAOMongoImpl;

    /**
     * setup().
     *
     * @throws Exception Exception
     */
    @Before
    public void setupEachTest() throws Exception {
        CollectorRegistry.defaultRegistry.clear();
        roScheduleV2DAOMongoImpl.deleteAll();
        eventImpl = new IgniteEventImpl();
        sp.init(spc);
        sp.setNotificationIdMapping(notificationIdMapping);

        sourceTopicName = sourceTopics[0];
        sinkTopicName = sinkTopics[0];
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void cleanup() {
        CollectorRegistry.defaultRegistry.clear();
    }

    @Test
    public void acvScheduleRTNTest() {
    }

    private ScheduleNotificationEventData createScheduleNotificationEventData() {
        ScheduleNotificationEventData roACVData = new ScheduleNotificationEventData();
        roACVData.setTriggerTimeMs(TestConstants.HUNDRED_LONG);
        JSONObject object = new JSONObject();
        roACVData.setPayload(object.toString().getBytes(StandardCharsets.UTF_8));
        return roACVData;
    }

    private IgniteEventImpl getIgniteEvent(AbstractEventData eventData, String eventId) {

        IgniteEventImpl eventImpl = new IgniteEventImpl();
        eventImpl.setBizTransactionId("bizTransactionId");
        eventImpl.setEventData(eventData);
        eventImpl.setEventId(eventId);
        eventImpl.setSchemaVersion(Version.V1_0);
        eventImpl.setRequestId("123456");
        eventImpl.setTimestamp(System.currentTimeMillis());
        eventImpl.setTimezone((short) TestConstants.THIRTY);
        eventImpl.setVehicleId("123456789");
        eventImpl.setMessageId("messageId");
        eventImpl.setCorrelationId("123456");
        eventImpl.setBizTransactionId("sessionId");
        eventImpl.setRequestId("roReq123");
        eventImpl.setDeviceDeliveryCutoff(TestConstants.MINUS_ONE_LONG);
        UserContext usr = new UserContext();
        usr.setRole("role");
        usr.setUserId("userId");
        eventImpl.setUserContextInfo(new ArrayList<>() {
            {
                add(usr);
            }
        });

        return eventImpl;
    }

}
