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

package org.eclipse.ecsp.platform.services.ro.dao;

import io.prometheus.client.CollectorRegistry;
import org.eclipse.ecsp.domain.ro.Ro;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Optional;

/**
 * test class for RoDAOMongoImpl.
 */
@SuppressWarnings({"LineLength"})
@SpringBootTest(classes = org.eclipse.ecsp.Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:/application-test.properties")
public class RoDAOMongoImplTest extends CommonTestBase {

    @Autowired
    RoDAOMongoImpl roDAOMongo;

    public void cleanup() {
        CollectorRegistry.defaultRegistry.clear();
    }

    @Test
    public void getRIResponses() {
        //test meet condition RiResponse not exists
        String vehicleId = "dummyVinId";
        String roRequestId = "dummyReqId";
        Optional<Ro> riResponses = roDAOMongo.getRIResponses(vehicleId, roRequestId);
        Assertions.assertFalse(riResponses.isPresent());
    }
}
