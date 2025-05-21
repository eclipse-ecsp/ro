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

import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class for DefaultNotificationResolver.
 */
public class DefaultNotificationResolverTest {

    @Test
    public void get() {
        DefaultNotificationResolver defaultNotificationResolver = new DefaultNotificationResolver();
        Map keyMap = new HashMap();
        String mappingIdentifier = "REMOTEOPERATIONENGINE_STARTED_SUCCESS";
        String ncId = "";
        keyMap.put(mappingIdentifier, ncId);
        ReflectionTestUtils.setField(defaultNotificationResolver, "notificationIdMapping", keyMap);
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventId("REMOTEOPERATIONENGINE");
        String result = defaultNotificationResolver.get(igniteEvent, "REMOTEOPERATIONENGINE", mappingIdentifier);
        Assertions.assertEquals(ncId, result);
    }
}