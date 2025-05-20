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

package org.eclipse.ecsp.ro.notification;

import io.prometheus.client.CollectorRegistry;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.services.utils.SettingsManagerClient;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.HashMap;
import java.util.Map;
import static org.mockito.Mockito.when;

/**
 * Test class for StockingRuleNotificationResolver.
 */
@SpringBootTest(classes = org.eclipse.ecsp.Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = {"classpath:/application-test.properties", "classpath:/application-base.properties"})
public class StockingRuleNotificationResolverTest extends CommonTestBase {

    @Mock
    private SettingsManagerClient settingsManagerClient;

    @InjectMocks
    @Autowired
    private StockingRuleNotificationResolver stockingRuleNotificationResolver;

    @Autowired
    private NotificationCacheService notificationCacheService;

    @After
    public void cleanup() {
        CollectorRegistry.defaultRegistry.clear();
    }

    /**
     * setup method.
     *
     * @throws Exception Exception.
     */
    @Before
    public void setup() {
        CollectorRegistry.defaultRegistry.clear();
        MockitoAnnotations.openMocks(this);
        Map input = new HashMap();
        Map object = new HashMap();
        object.put("mappingName", "vehicle_archType1_precondition");
        input.put(Constants.STOCKING_RULE_CONFIGURATIONOBJECT, object);

        when(settingsManagerClient.getSettingsManagerConfigurationObject(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString())).thenReturn(input);
    }

    @Test
    public void get() {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setVehicleId("vin123");
        String result = stockingRuleNotificationResolver.get(igniteEvent, "RemoteOperationEngine",
                "REMOTEOPERATIONENGINE_STARTED_SUCCESS");
        Assert.assertNull(result);
    }

    @Test
    public void getFromCache() {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setVehicleId("vin123");
        Map object = new HashMap();
        object.put("mappingName", "vehicle_archType1_precondition");
        notificationCacheService.persistConfigMapIntoCache(igniteEvent, object);

        String result = stockingRuleNotificationResolver.get(igniteEvent, "RemoteOperationEngine",
                "REMOTEOPERATIONENGINE_STARTED_SUCCESS");
        Assert.assertNull(result);
    }

    /**
     * Spring Configuration.
     */
    @Configuration
    @ComponentScan("org.eclipse.ecsp")
    public static class SpringConfig {

    }

}