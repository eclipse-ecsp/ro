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

package org.eclipse.ecsp.ro.notification.identifier;

import io.prometheus.client.CollectorRegistry;
import org.eclipse.ecsp.domain.GenericCustomExtension;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.RemoteOperationResponseV1_1;
import org.eclipse.ecsp.domain.ro.constant.TestConstants;
import org.eclipse.ecsp.entities.AbstractEventData;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.entities.UserContext;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class for NotificationArchAndECUTypeResolver.
 */
@SpringBootTest(classes = org.eclipse.ecsp.Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = {"classpath:/application-test.properties", "classpath:/application-base.properties"})
public class NotificationArchAndECUTypeResolverTest extends CommonTestBase {

    private static final String RESPONSE = "response";

    private static final String FAILURE_REASON_CODE = "failureReasonCode";

    @Autowired
    private NotificationArchAndECUTypeResolver notificationArchAndECUTypeResolver;

    @Before
    @After
    public void cleanup() {
        CollectorRegistry.defaultRegistry.clear();
    }

    @Test
    public void getNotificationVin_ArchType1() {

        RemoteOperationResponseV1_1 roResponse = new RemoteOperationResponseV1_1();
        roResponse.setResponse(RemoteOperationResponseV1_1.Response.SUCCESS);
        GenericCustomExtension customExtension = new GenericCustomExtension();
        Map response = new HashMap();
        response.put(RESPONSE, "NOT_AUTHORIZED");
        response.put(FAILURE_REASON_CODE, TestConstants.TWENTY);
        customExtension.setCustomData(response);
        roResponse.setCustomExtension(customExtension);

        String result = notificationArchAndECUTypeResolver.getNotification(
                getIgniteEvent(roResponse, Constants.REMOTE_OPERATION_RESPONSE_EVENT_ID),
                "UNLOCKED", org.eclipse.ecsp.domain.ro.constant.Constants.REMOTEOPERATIONDOORS,
                "VEHICLE_ARCHTYPE1", "ECU1");
        Assert.assertNull(result);
    }

    @Test
    public void getNotificationVin_ArchType1ForUnknownCode() {

        RemoteOperationResponseV1_1 roResponse = new RemoteOperationResponseV1_1();
        roResponse.setResponse(RemoteOperationResponseV1_1.Response.SUCCESS);
        GenericCustomExtension customExtension = new GenericCustomExtension();
        Map response = new HashMap();
        response.put(RESPONSE, "NOT_AUTHORIZED");
        response.put(FAILURE_REASON_CODE, TestConstants.INT_3333);
        customExtension.setCustomData(response);
        roResponse.setCustomExtension(customExtension);

        String result = notificationArchAndECUTypeResolver.getNotification(
                getIgniteEvent(roResponse, Constants.REMOTE_OPERATION_RESPONSE_EVENT_ID),
                "UNLOCKED", org.eclipse.ecsp.domain.ro.constant.Constants.REMOTEOPERATIONDOORS,
                "VEHICLE_ARCHTYPE1", "ECU1");
        Assert.assertNull(result);
    }

    @Test
    public void getNotificationTHIRDPARTY1() {

        RemoteOperationResponseV1_1 roResponse = new RemoteOperationResponseV1_1();
        roResponse.setResponse(RemoteOperationResponseV1_1.Response.SUCCESS);
        GenericCustomExtension customExtension = new GenericCustomExtension();
        Map response = new HashMap();
        response.put(RESPONSE, "NOT_AUTHORIZED");
        customExtension.setCustomData(response);
        roResponse.setCustomExtension(customExtension);

        String result = notificationArchAndECUTypeResolver.getNotification(
                getIgniteEvent(roResponse, Constants.REMOTE_OPERATION_RESPONSE_EVENT_ID),
                "UNLOCKED", org.eclipse.ecsp.domain.ro.constant.Constants.REMOTEOPERATIONDOORS,
                "VEHICLE_ARCHTYPE3", "TELEMATICS");
        Assert.assertNull(result);
    }

    @Test
    public void getNotificationTHIRDPARTY1WithNullECUTypeAndArchType() {

        RemoteOperationResponseV1_1 roResponse = new RemoteOperationResponseV1_1();
        roResponse.setResponse(RemoteOperationResponseV1_1.Response.SUCCESS);
        GenericCustomExtension customExtension = new GenericCustomExtension();
        Map response = new HashMap();
        response.put(RESPONSE, "NOT_AUTHORIZED");
        customExtension.setCustomData(response);
        roResponse.setCustomExtension(customExtension);

        String result = notificationArchAndECUTypeResolver.getNotification(
                getIgniteEvent(roResponse, Constants.REMOTE_OPERATION_RESPONSE_EVENT_ID),
                "UNLOCKED", org.eclipse.ecsp.domain.ro.constant.Constants.REMOTEOPERATIONDOORS,
                null, null);
        Assert.assertNull(result);
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
        eventImpl.setVehicleId("vehicleId");
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

    /**
     * Spring Config.
     */
    @Configuration
    @ComponentScan("org.eclipse.ecsp")
    public static class SpringConfig {

    }
}