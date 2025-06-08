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

package org.eclipse.ecsp.ro.utils;

import io.prometheus.client.CollectorRegistry;
import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.ecsp.domain.ro.RCPDResponseV1_0;
import org.eclipse.ecsp.domain.ro.constant.TestConstants;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.entities.UserContext;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * test class for Utils.
 */
@SpringBootTest(classes = org.eclipse.ecsp.Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:/application-test.properties")
public class UtilsTest extends CommonTestBase {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(UtilsTest.class);

    List<UserContext> userContexts;

    Utils utils;

    RCPDResponseV1_0 eventDataData;

    @Value("#{${notification.responsemessage.notificationid.mapping}}")
    private Map<String, String> notificationIdMap;

    @Value("#{${rcpd.notificationId.mapping}}")
    private Map<String, String> rcpdNotificationMap;

    @After
    public void cleanup() {
        CollectorRegistry.defaultRegistry.clear();
    }

    /**
     * setup test.
     */
    @Before
    public void setUp() throws Exception {
        CollectorRegistry.defaultRegistry.clear();
        userContexts = new ArrayList<>();
        UserContext userContext1 = new UserContext();
        userContext1.setUserId("user1");
        UserContext userContext2 = new UserContext();
        userContext2.setUserId("user2");
        userContexts.add(userContext1);
        userContexts.add(userContext2);

        eventDataData = new RCPDResponseV1_0();
        eventDataData.setResponse(RCPDResponseV1_0.Response.SUCCESS);
        eventDataData.setRcpdRequestId("requestId");
    }


    @Test
    public void createIgniteEvent() {
        String vehicleId = "dummyVinId";
        String eventId = "dummyEventId";
        IgniteEventImpl igniteEvent = Utils.createIgniteEvent(vehicleId, eventDataData, eventId, userContexts);
        Assert.assertEquals(TestConstants.TWO, igniteEvent.getUserContextInfo().size());
    }

    @Test
    public void testNotificationMapConfig() {
        Assert.assertFalse(notificationIdMap.isEmpty());
        Assert.assertFalse(rcpdNotificationMap.isEmpty());

    }

    @Test
    public void testUTCConvert() {
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        String time = "2023/03/31 18:31:10";
        LOGGER.info("timestamp: {}", TimeZoneUtils.getUTCTimestamp(zoneId, time, TimeZoneUtils.TIME_PATTERN));
        LOGGER.info("utc timestamp: {}", TimeZoneUtils.getCurrentUTCTimestamp());
        Assert.assertNotNull(zoneId);
    }

    @Test
    public void testUTCConvert2() {
        long time = TimeZoneUtils.getCurrentUTCTimestamp();
        SimpleDateFormat sdf = new SimpleDateFormat(TimeZoneUtils.TIME_PATTERN);
        sdf.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
        Date date = DateUtils.addMinutes(new Date(time * TestConstants.THOUSAND_LONG), TestConstants.FIVE);
        LOGGER.info("utc timestamp: {}", time);
        LOGGER.info("utc timestamp: {}", sdf.format(date));
        Assert.assertNotNull(date);
    }

    @Test
    public void testUTCConvert3() {
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        long firstScheduleTs = TimeZoneUtils.getUTCTimestamp(zoneId, "2023/03/30 12:00:00", TimeZoneUtils.TIME_PATTERN)
                - TimeZoneUtils.getCurrentUTCTimestamp();
        LOGGER.debug("firstScheduleTs: {}", firstScheduleTs);
        Assert.assertNotNull(zoneId);
    }

    @Test
    public void testLogForging_withInput() {
        String input = "Hello\nWorld\rTab\tEnd";
        String expected = "Hello_World_Tab_End";

        String result = Utils.logForging(input);
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void testLogForging_withNull() {
        String result = Utils.logForging(null);
        Assertions.assertNull(result);
    }
}