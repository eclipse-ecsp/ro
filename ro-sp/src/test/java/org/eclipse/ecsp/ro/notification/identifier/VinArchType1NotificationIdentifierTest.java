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
import org.eclipse.ecsp.domain.ro.RemoteOperationResponseV1_1;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Integration test for Vin_ArchType1 Notification Identifier.
 */
@SpringBootTest(classes = org.eclipse.ecsp.Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = {"classpath:/application-test.properties", "classpath:/application-base.properties"})
public class VinArchType1NotificationIdentifierTest extends CommonTestBase {

    private static final String RESPONSE = "response";
    private static final String FAILURE_REASON_CODE = "failureReasonCode";

    @Autowired
    private Vin_ArchType1NotificationIdentifier vinArchType1NotificationIdentifier;

    @Before
    @After
    public void forEachTest() {
        CollectorRegistry.defaultRegistry.clear();
    }

    /**
     * TDD case 1:
     * When send remote door UNLOCKED request, then V2C API response: NOT_AUTHORIZED and
     * RemoteOperationResponse.failureReasonCode = 20, the notification identifier should be
     * REMOTEOPERATIONDOORS_UNLOCKED_NOT_AUTHORIZED_20.
     */
    @Test
    public void getIdentifierNOT_AUTHORIZED() {

        RemoteOperationResponseV1_1 roResponse = new RemoteOperationResponseV1_1();
        roResponse.setResponse(RemoteOperationResponseV1_1.Response.SUCCESS);
        GenericCustomExtension customExtension = new GenericCustomExtension();
        Map response = new HashMap();
        response.put(RESPONSE, "NOT_AUTHORIZED");
        final int Twenty = 20;
        response.put(FAILURE_REASON_CODE, Twenty);
        customExtension.setCustomData(response);
        roResponse.setCustomExtension(customExtension);

        String result = vinArchType1NotificationIdentifier
                .getIdentifier(roResponse, "UNLOCKED", "REMOTEOPERATIONDOORS");
        Assert.assertEquals("REMOTEOPERATIONDOORS_UNLOCKED_NOT_AUTHORIZED_20", result);
    }

    /**
     * TDD case 2:
     * When send remote door UNLOCKED request, then V2C API
     * response: NOT_AUTHORIZED without failureReasonCode, the notification
     * identifier should be REMOTEOPERATIONDOORS_UNLOCKED_NOT_AUTHORIZED.
     */
    @Test
    public void getIdentifierNOT_AUTHORIZED_WithOutFailureReasonCode() {

        RemoteOperationResponseV1_1 roResponse = new RemoteOperationResponseV1_1();
        roResponse.setResponse(RemoteOperationResponseV1_1.Response.SUCCESS);
        GenericCustomExtension customExtension = new GenericCustomExtension();
        Map response = new HashMap();
        response.put(RESPONSE, "NOT_AUTHORIZED");
        customExtension.setCustomData(response);
        roResponse.setCustomExtension(customExtension);

        String result = vinArchType1NotificationIdentifier
                .getIdentifier(roResponse, "UNLOCKED", "REMOTEOPERATIONDOORS");
        Assert.assertEquals("REMOTEOPERATIONDOORS_UNLOCKED_NOT_AUTHORIZED", result);
    }

    /**
     * Spring configuration bean.
     */
    @Configuration
    @ComponentScan("org.eclipse.ecsp")
    public static class SpringConfig {

    }
}