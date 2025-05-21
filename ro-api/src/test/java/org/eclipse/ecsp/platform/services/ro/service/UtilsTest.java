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

package org.eclipse.ecsp.platform.services.ro.service;

import io.prometheus.client.CollectorRegistry;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for {@link Utils}.
 */
@SpringBootTest(classes = org.eclipse.ecsp.Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:/application-test.properties")
public class UtilsTest extends CommonTestBase {

    @Autowired
    Utils utils;

    @Before
    @After
    public void cleanup() {
        CollectorRegistry.defaultRegistry.clear();
    }

    @Test
    public void testGetIgniteEventTimeStampComparator() {
        //sort by timestamp desc
        long currentTimeMillis = System.currentTimeMillis();
        List<IgniteEvent> eventList = new ArrayList<>();
        IgniteEventImpl event1 = new IgniteEventImpl();
        event1.setEventId("1st");
        eventList.add(event1);
        event1.setTimestamp(currentTimeMillis);
        IgniteEventImpl event2 = new IgniteEventImpl();
        event1.setEventId("2st");
        final int i = 100;
        event1.setTimestamp(currentTimeMillis + i);
        eventList.add(event2);
        eventList.sort(Utils.getIgniteEventTimeStampComparator());
        Assert.assertTrue(eventList.get(0).getTimestamp() > eventList.get(1).getTimestamp());
    }

    @Test
    public void testGetIgniteEventTimeStampComparatorCase2() {
        //sort by timestamp desc
        long currentTimeMillis = System.currentTimeMillis();
        List<IgniteEvent> eventList = new ArrayList<>();
        IgniteEventImpl event1 = new IgniteEventImpl();
        event1.setEventId("1st");
        eventList.add(event1);
        event1.setTimestamp(currentTimeMillis);
        IgniteEventImpl event2 = new IgniteEventImpl();
        event1.setEventId("2st");
        final int i = 100;
        event1.setTimestamp(currentTimeMillis - i);
        eventList.add(event2);
        eventList.sort(Utils.getIgniteEventTimeStampComparator());
        Assert.assertTrue(eventList.get(0).getTimestamp() > eventList.get(1).getTimestamp());
    }

}