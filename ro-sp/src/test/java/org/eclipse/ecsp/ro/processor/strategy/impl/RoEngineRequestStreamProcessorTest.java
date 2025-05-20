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
import org.eclipse.ecsp.domain.ro.RemoteOperationResponseV1_1;
import org.eclipse.ecsp.ro.domains.ROGenericNotificationEventDataV1_1;
import org.eclipse.ecsp.ro.utils.NotificationUtil;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.stream.Stream;

/**
 * test class for RoEngineRequestStreamProcessor.
 */
@SpringBootTest(classes = org.eclipse.ecsp.Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = {"classpath:/application-test.properties", "classpath:/application-base.properties"})
public class RoEngineRequestStreamProcessorTest extends CommonTestBase {

    @Mock
    StreamProcessingContext spc;

    @Autowired
    private RoEngineRequestStreamProcessor myAbstractStreamProcessor;

    @Autowired
    private NotificationUtil notificationUtil;

    @After
    @Before
    public void cleanup() {
        CollectorRegistry.defaultRegistry.clear();
    }

    @Test
    public void process() {
        RemoteOperationResponseV1_1 remoteOperationResponseV11 = new RemoteOperationResponseV1_1();
        remoteOperationResponseV11.setRoRequestId("requestId");
        remoteOperationResponseV11.setResponse(RemoteOperationResponseV1_1.Response.SUCCESS_CONTINUE);
        remoteOperationResponseV11.setOrigin("origin");
        remoteOperationResponseV11.setUserId("userJoe");

        Stream<String> stream = Stream.of("notif-1");
        stream.forEach(failedCaseNotificationId -> {
            ROGenericNotificationEventDataV1_1 roGenericNotificationEventData1 = ReflectionTestUtils.invokeMethod(
                    notificationUtil, "generateNotificationData", remoteOperationResponseV11, failedCaseNotificationId);
            assert roGenericNotificationEventData1 != null;
            Assert.assertNull(roGenericNotificationEventData1.getStatus());
        });
        String notificationId7 = "notif-7";
        ROGenericNotificationEventDataV1_1 roGenericNotificationEventDataV7 = ReflectionTestUtils.invokeMethod(
                notificationUtil, "generateNotificationData", remoteOperationResponseV11, notificationId7);
        assert roGenericNotificationEventDataV7 != null;
        Assert.assertNull(roGenericNotificationEventDataV7.getStatus());

        String notificationId16 = "notif-6";
        ROGenericNotificationEventDataV1_1 roGenericNotificationEventDataV16 = ReflectionTestUtils.invokeMethod(
                notificationUtil, "generateNotificationData", remoteOperationResponseV11, notificationId16);
        assert roGenericNotificationEventDataV16 != null;
        Assert.assertNull(roGenericNotificationEventDataV16.getStatus());
    }
}