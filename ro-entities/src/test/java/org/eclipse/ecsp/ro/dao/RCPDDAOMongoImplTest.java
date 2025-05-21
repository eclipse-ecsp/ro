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

package org.eclipse.ecsp.ro.dao;

import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.RCPD;
import org.eclipse.ecsp.domain.ro.RCPDRequestV1_0;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.domain.ro.dao.RCPDDAOMongoImpl;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.entities.UserContext;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

/**
 * Test class for RCPDDAOMongoImpl.
 */
@SpringBootTest(classes = org.eclipse.ecsp.Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = {"classpath:/application-test.properties", "classpath:/application-base.properties"})
public class RCPDDAOMongoImplTest extends CommonTestBase {

    private static final String VEHICLE_ID = "123456";
    private static final String USER_ID = "abc";
    private static final String REQUEST_ID = "74125911-7a89-41be-b3e6-831b5fca27a4";
    private static final String SCHEDULE_ID = "s12ad11-33dff";

    //    @ClassRule
    //    public static final EmbeddedMongoDB embeddedMongoDB = new EmbeddedMongoDB();

    @Autowired
    private RCPDDAOMongoImpl rcpdDAO;


    @Test
    public void getRCPDRequest() throws IOException {
        saveRCPDRequest();
        Optional<RCPD> result = rcpdDAO.getRCPDRequest(VEHICLE_ID, REQUEST_ID, null);
        Assert.assertTrue(result.isPresent());
        Optional<RCPD> resultWithCcheduleId = rcpdDAO.getRCPDRequest(VEHICLE_ID, null, SCHEDULE_ID);
        Assert.assertTrue(resultWithCcheduleId.isPresent());
    }

    @Test
    public void getRCPDRequestWithScheduleId() throws IOException {
        saveRCPDRequest();
        Optional<RCPD> result = rcpdDAO.getRCPDRequest(VEHICLE_ID, REQUEST_ID, "s12ad11-33dff");
        Assert.assertTrue(result.isPresent());

        Optional<RCPD> resultWithCcheduleId = rcpdDAO.getRCPDRequest(VEHICLE_ID, null, SCHEDULE_ID);
        RCPDRequestV1_0 rcpdRequestV10 = (RCPDRequestV1_0) resultWithCcheduleId.get().getRcpdEvent().getEventData();
        Assert.assertEquals("s12ad11-33dff", rcpdRequestV10.getScheduleRequestId());
    }

    @Test
    public void prepareIgniteQueryForRcpdRequest() {
        Assert.assertNotNull(rcpdDAO.prepareIgniteQueryForRcpdRequest(REQUEST_ID, VEHICLE_ID));
    }

    private void saveRCPDRequest() {
        rcpdDAO.deleteAll();

        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setVehicleId(VEHICLE_ID);
        igniteEvent.setSchemaVersion(Version.V1_0);
        igniteEvent.setEventId(Constants.RCPDREQUEST);
        igniteEvent.setMessageId("10001");
        igniteEvent.setRequestId("eventRequestId");

        UserContext uc = new UserContext();
        uc.setUserId(USER_ID);
        uc.setRole("VO");

        igniteEvent.setUserContextInfo(Arrays.asList(uc));

        RCPDRequestV1_0 data = new RCPDRequestV1_0();
        data.setScheduleRequestId(SCHEDULE_ID);
        data.setRcpdRequestId(REQUEST_ID);
        igniteEvent.setEventData(data);

        RCPD entity = new RCPD();
        entity.setSchemaVersion(Version.V1_0);
        entity.setRcpdEvent(igniteEvent);

        rcpdDAO.save(entity);
    }

    /**
     * spring configuration.
     */
    @Configuration
    @ComponentScan("org.eclipse.ecsp")
    public static class SpringConfig {
    }
}