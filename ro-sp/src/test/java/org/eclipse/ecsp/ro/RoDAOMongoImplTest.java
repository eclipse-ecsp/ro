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

package org.eclipse.ecsp.ro;

import io.prometheus.client.CollectorRegistry;
import org.eclipse.ecsp.domain.ro.Ro;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.Optional;

/**
 * Tests for {@link RoDAOMongoImpl}.
 */
@SpringBootTest(classes = org.eclipse.ecsp.Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
@TestPropertySource(value = {"classpath:/application-test.properties", "classpath:/application-base.properties"})
public class RoDAOMongoImplTest extends CommonTestBase {

    @Autowired
    RoDAOMongoImpl roDAOMongo;

    @BeforeAll
    public void setup() {
        super.setup();
    }

    @BeforeEach
    @AfterEach
    public void cleanup() {
        CollectorRegistry.defaultRegistry.clear();
    }

    @Test
    public void getLatesRIEntityForNotification() {
        Assertions.assertNull(roDAOMongo.getLatesRIEntityForNotification("sessionId", "vehicleId"));
    }

    @Test
    public void testGetRIEntityByFieldName_ReturnsEmpty() {
        Optional<Ro> result = roDAOMongo.getRIEntityByFieldName("someRequestID", "someVehicleId");
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testPrepareIgniteQueryForRoRequest() {
        IgniteQuery query = roDAOMongo.prepareIgniteQueryForRoRequest("roReqId", "vehicleId");
        Assertions.assertNotNull(query);
    }

    @Test
    public void testPrepareIgniteQueryBySessionIdANDMsgId() {
        IgniteQuery query = roDAOMongo.prepareIgniteQueryBySessionIdANDMsgId("vehicleId", "sessionId", "msgId");
        Assertions.assertNotNull(query);
    }

    @Test
    public void testPrepareIgniteQueryForRIRequestWithSessionId() {
        IgniteQuery query = roDAOMongo.prepareIgniteQueryForRIRequestWithSessionId("sessionId", "vehicleid");
        Assertions.assertNotNull(query);
    }

    @Test
    public void testGetROEntityByFieldNameByRoReqIdExceptACV_ReturnsEmpty() {
        Optional<Ro> result = roDAOMongo.getROEntityByFieldNameByRoReqIdExceptACV("vehicleId", "roRequestId");
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testGetROEntityByFieldNameByRoReqId_ReturnsEmpty() {
        Optional<Ro> result = roDAOMongo.getROEntityByFieldNameByRoReqId("vehicleId", "roRequestId");
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testGetROEntityByFieldNameByBizIdExceptACV_ReturnsEmpty() {
        Optional<Ro> result = roDAOMongo.getROEntityByFieldNameByBizIdExceptACV("vehicleId", "sessionId");
        Assertions.assertTrue(result.isEmpty());
    }
}