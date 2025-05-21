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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.ro.RoSchedule;
import org.eclipse.ecsp.domain.ro.ScheduleDto;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.platform.services.ro.client.DeviceAssociationApiClient;
import org.eclipse.ecsp.platform.services.ro.dao.RoScheduleDAOMongoImpl;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationResponse;
import org.eclipse.ecsp.platform.services.ro.rest.ROEngineController;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.eclipse.ecsp.utils.JsonUtils;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseErrorHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test class for {@link ROEngineController}.
 *
 * @author midnani
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:/application-test.properties")
public class ROEngineControllerTest extends CommonTestBase {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ROEngineControllerTest.class);
    private static final String API_VERSION = "/v1.1";
    private static final String API_VERSION_V_2 = "/v2";

    private ResponseErrorHandler preExistingErrorHandler;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RoScheduleDAOMongoImpl roScheduleDAOMongoImpl;

    @MockBean
    private KafkaService kafkaService;

    @MockBean
    private DeviceAssociationApiClient deviceAssociationApiClient;

    /**
     * setup().
     *
     * @throws Exception Exception
     */
    @Before
    public void setUp() throws Exception {
        CollectorRegistry.defaultRegistry.clear();
        preExistingErrorHandler = restTemplate.getRestTemplate().getErrorHandler();
        restTemplate.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler());
    }

    @After
    public void tearDown() {
        CollectorRegistry.defaultRegistry.clear();
        restTemplate.getRestTemplate().setErrorHandler(preExistingErrorHandler);
    }

    @Test
    public void testCreateRemoteEngineRequest() throws Exception {
        LOGGER.debug("testing CreateRemoteEngineRequest");
        String requestJson = IOUtils.toString(
                ROEngineControllerTest.class.getResourceAsStream("/remoteEngineRequest.json"), "UTF-8");
        HttpEntity<String> entity = new HttpEntity<String>(requestJson, createHeaders());
        Mockito.when(deviceAssociationApiClient.getAssociatedUsers(Mockito.anyString())).thenReturn("123Engine");
        ResponseEntity<String> response = restTemplate
                .exchange(API_VERSION + "/users/123Engine/vehicles/123456/ro/engine",
                        HttpMethod.PUT,
                        entity, String.class);
        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        LOGGER.debug("RemoteOperationEngineReq :response body " + response.getBody());
        RemoteOperationResponse res = JsonUtils.parseInputJson(response.getBody(), RemoteOperationResponse.class);
        Assert.assertEquals("RO Command Sent Successfully", res.getMessage());
        Assert.assertEquals("testRequestId", res.getRoRequestId());
    }

    @Test
    public void testCreateRemoteEngineRequestWithoutRequestId() throws Exception {
        LOGGER.debug("testing CreateRemoteEngineRequest");
        String requestJson = IOUtils.toString(
                ROEngineControllerTest.class.getResourceAsStream("/remoteEngineRequest.json"), "UTF-8");
        HttpHeaders headers = createHeaders();
        headers.set("RequestId", StringUtils.EMPTY);
        HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
        Mockito.when(deviceAssociationApiClient.getAssociatedUsers(Mockito.anyString())).thenReturn("123Engine");
        ResponseEntity<String> response = restTemplate
                .exchange(API_VERSION + "/users/123Engine/vehicles/123456/ro/engine",
                        HttpMethod.PUT,
                        entity, String.class);
        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        LOGGER.debug("RemoteOperationEngineReq :response body " + response.getBody());
        RemoteOperationResponse res = JsonUtils.parseInputJson(response.getBody(), RemoteOperationResponse.class);
        Assert.assertEquals("RO Command Sent Successfully", res.getMessage());
        Assert.assertEquals("-\\0274\\302]\\325\\324 \\354K", res.getRoRequestId());
    }

    @Test
    public void testCreateRemoteEngineRequest_v2() throws Exception {
        LOGGER.debug("testing CreateRemoteEngineRequest_v2");
        String requestJson = IOUtils.toString(
                ROEngineControllerTest.class.getResourceAsStream("/remoteEngineScheduleRequest.json"), "UTF-8");
        HttpEntity<String> entity = new HttpEntity<String>(requestJson, createHeaders());
        Mockito.when(deviceAssociationApiClient.getAssociatedUsers(Mockito.anyString())).thenReturn("123Engine");
        ResponseEntity<String> response = restTemplate
                .exchange(API_VERSION_V_2 + "/users/123Engine/vehicles/123456/ro/engine",
                        HttpMethod.PUT,
                        entity, String.class);
        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        LOGGER.debug("RemoteOperationEngineReq :response body " + response.getBody());
        RemoteOperationResponse res = JsonUtils.parseInputJson(response.getBody(), RemoteOperationResponse.class);
        Assert.assertEquals("RO Command Sent Successfully", res.getMessage());
        Assert.assertEquals("testRequestId", res.getRoRequestId());
    }

    @Test
    public void testCreateRemoteEngineRequest_v2WithoutRequestId() throws Exception {
        LOGGER.debug("testing CreateRemoteEngineRequest_v2");
        String requestJson = IOUtils.toString(
                ROEngineControllerTest.class.getResourceAsStream("/remoteEngineScheduleRequest.json"), "UTF-8");
        HttpHeaders headers = createHeaders();
        headers.set("RequestId", StringUtils.EMPTY);
        HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
        Mockito.when(deviceAssociationApiClient.getAssociatedUsers(Mockito.anyString())).thenReturn("123Engine");
        ResponseEntity<String> response = restTemplate
                .exchange(API_VERSION_V_2 + "/users/123Engine/vehicles/123456/ro/engine",
                        HttpMethod.PUT,
                        entity, String.class);
        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        LOGGER.debug("RemoteOperationEngineReq :response body " + response.getBody());
        RemoteOperationResponse res = JsonUtils.parseInputJson(response.getBody(), RemoteOperationResponse.class);
        Assert.assertEquals("RO Command Sent Successfully", res.getMessage());
        Assert.assertEquals("-\\0274\\302]\\325\\324 \\354K", res.getRoRequestId());
    }

    @Test
    public void testCreateRemoteEngineRequest_v2_badRequest() throws Exception {
        LOGGER.debug("Test CreateRemoteEngineRequest_v2");
        Mockito.when(deviceAssociationApiClient.getAssociatedUsers(Mockito.anyString())).thenReturn("123Engine");
        Assert.assertThrows(HttpClientErrorException.class, () -> {
            String requestJson = IOUtils.toString(
                    ROEngineControllerTest.class.getResourceAsStream("/remoteEngineScheduleRequest2.json"), "UTF-8");
            HttpEntity<String> entity = new HttpEntity<String>(requestJson, createHeaders());
            ResponseEntity<String> response = restTemplate
                    .exchange(API_VERSION_V_2 + "/users/123Engine/vehicles/123456/ro/engine",
                            HttpMethod.PUT,
                            entity, String.class);
        });
    }

    @Test
    public void testCreateRemoteEngineRequest_v2_branch() throws Exception {

        RoSchedule dbRoSchedule = new RoSchedule();
        Map<String, List<ScheduleDto>> schedules = new HashMap<>();
        dbRoSchedule.setSchedules(schedules);
        dbRoSchedule.setVehicleId("123456");
        roScheduleDAOMongoImpl.save(dbRoSchedule);

        LOGGER.debug("testing CreateRemoteEngineRequest_v2");
        String requestJson = IOUtils.toString(
                ROEngineControllerTest.class.getResourceAsStream("/remoteEngineScheduleRequest.json"), "UTF-8");
        HttpEntity<String> entity = new HttpEntity<String>(requestJson, createHeaders());
        Mockito.when(deviceAssociationApiClient.getAssociatedUsers(Mockito.anyString())).thenReturn("123Engine");
        ResponseEntity<String> response = restTemplate
                .exchange(API_VERSION_V_2 + "/users/123Engine/vehicles/123456/ro/engine",
                        HttpMethod.PUT,
                        entity, String.class);
        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    }

    @Test
    public void testCreateRemoteEngineRequest_v2_branch2() throws Exception {

        RoSchedule dbRoSchedule = new RoSchedule();
        Map<String, List<ScheduleDto>> schedules = new HashMap<>();
        schedules.put("key", new ArrayList<>());
        dbRoSchedule.setSchedules(schedules);
        dbRoSchedule.setVehicleId("123456");
        roScheduleDAOMongoImpl.save(dbRoSchedule);

        LOGGER.debug("testing CreateRemoteEngineRequest_v2");
        String requestJson = IOUtils.toString(
                ROEngineControllerTest.class.getResourceAsStream("/remoteEngineScheduleRequest.json"), "UTF-8");
        Mockito.when(deviceAssociationApiClient.getAssociatedUsers(Mockito.anyString())).thenReturn("123Engine");
        HttpEntity<String> entity = new HttpEntity<String>(requestJson, createHeaders());
        ResponseEntity<String> response = restTemplate
                .exchange(API_VERSION_V_2 + "/users/123Engine/vehicles/123456/ro/engine",
                        HttpMethod.PUT,
                        entity, String.class);
        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    }

    @Test
    public void testCreateRemoteEngineRequest_invalidCommand() throws Exception {
        LOGGER.debug("testing CreateRemoteEngineRequest_invalidCommand");
        Mockito.when(deviceAssociationApiClient.getAssociatedUsers(Mockito.anyString())).thenReturn("123Engine");
        Assert.assertThrows(HttpClientErrorException.BadRequest.class, () -> {
            String requestJson = IOUtils.toString(
                    ROEngineControllerTest.class
                            .getResourceAsStream("/remoteEngineRequest_invalid_command.json"), "UTF-8");
            HttpEntity<String> entity = new HttpEntity<String>(requestJson, createHeaders());
            restTemplate.exchange(API_VERSION + "/users/123Engine/vehicles/123456/ro/engine",
                    HttpMethod.PUT,
                    entity, String.class);
        });
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("ClientRequestId", "testGlobalTrackingId");
        headers.add("RequestId", "testRequestId");
        headers.add("SessionId", "testGlobalSessionId");
        headers.add("OriginId", "test");
        headers.add("ecuType", "ECU1");
        return headers;
    }
}
