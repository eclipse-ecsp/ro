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

package org.eclipse.ecsp.platform.services.ro;

import io.prometheus.client.CollectorRegistry;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.RemoteOperationType;
import org.eclipse.ecsp.domain.ro.RoSchedule;
import org.eclipse.ecsp.domain.ro.ScheduleDto;
import org.eclipse.ecsp.domain.ro.ScheduleStatus;
import org.eclipse.ecsp.domain.ro.constant.TestConstants;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.platform.services.ro.dao.RoScheduleDAOMongoImpl;
import org.eclipse.ecsp.platform.services.ro.handler.ApiRequestHandler;
import org.eclipse.ecsp.platform.services.ro.rest.ROScheduleController;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseErrorHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * test class for {@link ROScheduleController}.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:/application-test.properties")
public class ROScheduleControllerTest extends CommonTestBase {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ROEngineControllerTest.class);

    private static final String API_VERSION = "/v1";

    private ResponseErrorHandler preExistingErrorHandler;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private KafkaService kafkaService;

    @MockBean
    private ApiRequestHandler apiRequestHandler;

    @Autowired
    private RoScheduleDAOMongoImpl roScheduleDAOMongoImpl;

    @After
    public void tearDown() {
        CollectorRegistry.defaultRegistry.clear();
        restTemplate.getRestTemplate().setErrorHandler(preExistingErrorHandler);
    }

    /**
     * setUp().
     *
     * @throws Exception Exception
     */
    @Before
    public void setUp() throws Exception {
        CollectorRegistry.defaultRegistry.clear();
        preExistingErrorHandler = restTemplate.getRestTemplate().getErrorHandler();
        restTemplate.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler());
        roScheduleDAOMongoImpl.update(createSchedule());
        Mockito.when(apiRequestHandler.preHandle(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
    }

    @Test
    public void testGetROSchedules() throws IOException {
        LOGGER.debug("testing getROSchedules");
        HttpEntity<String> schedulEntity = new HttpEntity<String>("",
                createHeaders());
        ResponseEntity<String> scheduleResponse = restTemplate.exchange(
                API_VERSION + "/users/123Engine/vehicles/123456/ro/REMOTE_OPERATION_ENGINE/schedules",
                HttpMethod.GET,
                schedulEntity, String.class);
        Assert.assertEquals(HttpStatus.OK, scheduleResponse.getStatusCode());
        Assert.assertNotNull(scheduleResponse.getBody());
    }

    @Test
    public void testDeleteROSchedules() throws IOException {
        LOGGER.debug("testing deleteROSchedules");
        HttpEntity<String> scheduleEntity = new HttpEntity<String>("{\"schedulerKey\": \"testRequestId\"}",
                createHeaders());
        ResponseEntity<String> scheduleResponse = restTemplate.exchange(
                API_VERSION + "/users/123Engine/vehicles/123456/ro/REMOTE_OPERATION_ENGINE/schedules",
                HttpMethod.DELETE,
                scheduleEntity, String.class);
        Assert.assertEquals(HttpStatus.OK, scheduleResponse.getStatusCode());
        Assert.assertNotNull(scheduleResponse.getBody());
    }

    @Test
    public void testDeleteROSchedulesWithEmptyResp() throws IOException {
        roScheduleDAOMongoImpl.deleteAll();
        LOGGER.debug("testing deleteROSchedules");
        Assert.assertThrows(HttpClientErrorException.class, () -> {
            HttpEntity<String> scheduleEntity = new HttpEntity<String>("{\"schedulerKey\": \"testRequestId\"}",
                    createHeaders());
            ResponseEntity<String> scheduleResponse = restTemplate.exchange(
                    API_VERSION + "/users/123Engine/vehicles/123456/ro/REMOTE_OPERATION_ENGINE/schedules",
                    HttpMethod.DELETE,
                    scheduleEntity, String.class);
        });
    }

    @Test
    public void testGetROSchedulesWithEmptyResp() throws IOException {
        LOGGER.debug("Testing getROSchedules");
        Assert.assertThrows(HttpClientErrorException.class, () -> {
            roScheduleDAOMongoImpl.deleteAll();
            HttpEntity<String> schedulEntity = new HttpEntity<String>("",
                    createHeaders());
            restTemplate.exchange(
                    API_VERSION + "/users/123Engine/vehicles/123456/ro/REMOTE_OPERATION_ENGINE/schedules",
                    HttpMethod.GET,
                    schedulEntity,
                    String.class);
        });
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("ClientRequestId", "testGlobalTrackingId");
        headers.add("RequestId", "testRequestId");
        headers.add("SessionId", "testGlobalSessionId");
        headers.add("OriginId", "test");
        return headers;
    }

    private RoSchedule createSchedule() {
        RoSchedule dbRoSchedule = new RoSchedule();
        dbRoSchedule.setVehicleId("123456");
        dbRoSchedule.setSchemaVersion(Version.V2_0);

        ScheduleDto scheduleDto = new ScheduleDto();
        scheduleDto.setName("Morning");
        scheduleDto.setSchedulerKey("testRequestId");
        scheduleDto.setScheduleTs(System.currentTimeMillis() + TestConstants.THRESHOLD);
        scheduleDto.setStatus(ScheduleStatus.ACTIVE);
        scheduleDto.setCreatedOn(System.currentTimeMillis());
        scheduleDto.setUpdatedOn(System.currentTimeMillis());

        ScheduleDto scheduleDto1 = new ScheduleDto();
        scheduleDto1.setName("Morning");
        scheduleDto1.setSchedulerKey("testRequestId1");
        scheduleDto1.setScheduleTs(System.currentTimeMillis() + TestConstants.THRESHOLD);
        scheduleDto1.setStatus(ScheduleStatus.ACTIVE);
        scheduleDto1.setCreatedOn(System.currentTimeMillis());
        scheduleDto1.setUpdatedOn(System.currentTimeMillis());

        ArrayList<ScheduleDto> scheduleList = new ArrayList<>();
        scheduleList.add(scheduleDto);
        scheduleList.add(scheduleDto1);
        Map<String, List<ScheduleDto>> roMap = null;
        roMap = new HashMap<>();
        roMap.put(RemoteOperationType.REMOTE_OPERATION_ENGINE.getValue(), scheduleList);
        dbRoSchedule.setSchedules(roMap);
        return dbRoSchedule;
    }


}
