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
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.RemoteOperationType;
import org.eclipse.ecsp.domain.ro.RoSchedule;
import org.eclipse.ecsp.domain.ro.ScheduleDto;
import org.eclipse.ecsp.domain.ro.ScheduleStatus;
import org.eclipse.ecsp.domain.ro.constant.TestConstants;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test class for {@link RoScheduleDAOMongoImpl}.
 */
@SpringBootTest(classes = org.eclipse.ecsp.Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = {"classpath:/application-test.properties", "classpath:/application-base.properties"})
public class RoScheduleDAOMongoImplTest extends CommonTestBase {

    RoSchedule schedule = null;

    @Autowired
    private RoScheduleDAOMongoImpl roScheduleDAOMongoImpl;

    /**
     * setup method.
     *
     * @throws Exception Exception.
     */
    @Before
    public void beforeEachTest() throws Exception {
        CollectorRegistry.defaultRegistry.clear();
        schedule = createSchedule();
        roScheduleDAOMongoImpl.save(schedule);
    }

    @After
    public void afterEachTest() {
        CollectorRegistry.defaultRegistry.clear();
    }

    private RoSchedule createSchedule() {
        RoSchedule dbRoSchedule = new RoSchedule();
        dbRoSchedule.setVehicleId("123456");
        dbRoSchedule.setSchemaVersion(Version.V2_0);


        ScheduleDto scheduleDto = new ScheduleDto();
        scheduleDto.setScheduleId("oriId");
        scheduleDto.setName("Morning");
        scheduleDto.setSchedulerKey("testRequestId");
        scheduleDto.setScheduleTs(System.currentTimeMillis() + TestConstants.THRESHOLD);
        scheduleDto.setStatus(ScheduleStatus.ACTIVE);
        scheduleDto.setCreatedOn(System.currentTimeMillis());
        scheduleDto.setUpdatedOn(System.currentTimeMillis());

        ArrayList<ScheduleDto> scheduleList = new ArrayList<>();
        scheduleList.add(scheduleDto);
        Map<String, List<ScheduleDto>> roMap = null;
        roMap = new HashMap<>();
        roMap.put(RemoteOperationType.REMOTE_OPERATION_LIGHTS.getValue(), scheduleList);
        dbRoSchedule.setSchedules(roMap);
        return dbRoSchedule;
    }

    @Test
    public void updateSchedulerId() {
        roScheduleDAOMongoImpl.updateSchedulerId("123456", "testRequestId", "scheduleIdUpdated",
                RemoteOperationType.REMOTE_OPERATION_LIGHTS.getValue());
        RoSchedule roSchedule = roScheduleDAOMongoImpl.findById("123456");
        List<ScheduleDto> dtoList = roSchedule.getSchedules()
                .get(RemoteOperationType.REMOTE_OPERATION_LIGHTS.getValue());
        ScheduleDto scheduleDto = dtoList.get(0);
        Assert.assertEquals("scheduleIdUpdated", scheduleDto.getScheduleId());
    }

    @Test
    public void deleteRoSchedule() {
        Assert.assertTrue(roScheduleDAOMongoImpl.deleteRoSchedule("123456", "oriId"));
    }

    @Test
    public void getScheduleId() {

        String result = roScheduleDAOMongoImpl.getScheduleId("123456", "testRequestId",
                RemoteOperationType.REMOTE_OPERATION_LIGHTS.getValue());
        Assert.assertEquals("oriId", result);
    }

}