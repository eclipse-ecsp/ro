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
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationResponse;
import org.eclipse.ecsp.platform.services.ro.handler.ApiRequestHandler;
import org.eclipse.ecsp.platform.services.ro.rest.ROHornLightsAlarmController;
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
import java.io.IOException;

/**
 * Test class for {@link ROHornLightsAlarmController}.
 *
 * @author midnani
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:/application-test.properties")
public class ROHornLightsAlarmControllerTest extends CommonTestBase {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ROHornLightsAlarmControllerTest.class);
    private static final String API_VERSION = "/v1.1";
    private static final String API_VERSION_V12 = "/v1.2";

    private ResponseErrorHandler preExistingErrorHandler;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private KafkaService kafkaService;

    @MockBean
    private ApiRequestHandler apiRequestHandler;

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
        Mockito.when(apiRequestHandler.preHandle(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
    }

    @After
    public void tearDown() {
        CollectorRegistry.defaultRegistry.clear();
        restTemplate.getRestTemplate().setErrorHandler(preExistingErrorHandler);
    }

    @Test
    public void testCreateRemoteHornRequest() throws IOException {
        LOGGER.debug("testing CreateRemoteHornRequest");
        String requestJson = IOUtils.toString(
                ROHornLightsAlarmControllerTest.class.getResourceAsStream("/remoteHornAndLightsRequest.json"), "UTF-8");
        HttpEntity<String> entity = new HttpEntity<String>(requestJson, createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(API_VERSION + "/users/123Horn/vehicles/123456/ro/horn",
                HttpMethod.PUT,
                entity, String.class);
        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        LOGGER.debug("RemoteOperationHornReq :response body " + response.getBody());
        RemoteOperationResponse res = JsonUtils.parseInputJson(response.getBody(), RemoteOperationResponse.class);
        Assert.assertEquals("RO Command Sent Successfully", res.getMessage());
        Assert.assertEquals("testRequestId", res.getRoRequestId());

    }

    @Test
    public void testCreateRemoteAlarmRequest() throws IOException {
        LOGGER.debug("testing CreateRemoteHornAndLightsRequest");
        String requestJson = IOUtils.toString(
                ROHornLightsAlarmControllerTest.class.getResourceAsStream("/remoteHornAndLightsRequest.json"), "UTF-8");
        HttpEntity<String> entity = new HttpEntity<String>(requestJson, createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(API_VERSION
                + "/users/123Alarm/vehicles/123456/ro/alarm", HttpMethod.PUT, entity, String.class);
        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        LOGGER.debug("RemoteHornAndLightsRequest :response body " + response.getBody());
        RemoteOperationResponse res = JsonUtils.parseInputJson(response.getBody(), RemoteOperationResponse.class);
        Assert.assertEquals("RO Command Sent Successfully", res.getMessage());
        Assert.assertEquals("testRequestId", res.getRoRequestId());

    }

    @Test
    public void testCreateRemoteLightsRequest() throws IOException {
        LOGGER.debug("testing CreateRemoteLightsRequest");
        String requestJson = IOUtils.toString(
                ROHornLightsAlarmControllerTest.class.getResourceAsStream("/remoteHornAndLightsRequest.json"), "UTF-8");
        HttpEntity<String> entity = new HttpEntity<String>(requestJson, createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(API_VERSION
                        + "/users/123Lights/vehicles/123456/ro/lights",
                HttpMethod.PUT,
                entity, String.class);
        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        LOGGER.debug("RemoteOperationLightsReq :response body " + response.getBody());
        RemoteOperationResponse res = JsonUtils.parseInputJson(response.getBody(), RemoteOperationResponse.class);
        Assert.assertEquals("RO Command Sent Successfully", res.getMessage());
        Assert.assertEquals("testRequestId", res.getRoRequestId());

    }

    @Test
    public void testCreateRemoteHornRequestWithoutRequestId() throws IOException {
        LOGGER.debug("testing CreateRemoteHornRequestWithoutRequestId");
        String requestJson = IOUtils.toString(
                ROHornLightsAlarmControllerTest.class.getResourceAsStream("/remoteHornAndLightsRequest.json"), "UTF-8");
        HttpHeaders headers = createHeaders();
        headers.set("RequestId", StringUtils.EMPTY);
        HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
        ResponseEntity<String> response = restTemplate.exchange(API_VERSION + "/users/123Horn/vehicles/123456/ro/horn",
                HttpMethod.PUT,
                entity, String.class);
        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        LOGGER.debug("RemoteOperationHornReq :response body " + response.getBody());
        RemoteOperationResponse res = JsonUtils.parseInputJson(response.getBody(), RemoteOperationResponse.class);
        Assert.assertEquals("RO Command Sent Successfully", res.getMessage());
        Assert.assertEquals("-\\0274\\302]\\325\\324 \\354K", res.getRoRequestId());

    }

    @Test
    public void testCreateRemoteHornRequest_invalidCommand() throws IOException {
        LOGGER.debug("testing CreateRemoteHornRequest_invalidCommand");
        Assert.assertThrows(HttpClientErrorException.BadRequest.class, () -> {
            String requestJson = IOUtils.toString(
                    ROHornLightsAlarmControllerTest.class
                            .getResourceAsStream("/remoteHornAndLightsRequest_invalid_command.json"), "UTF-8");
            HttpEntity<String> entity = new HttpEntity<String>(requestJson, createHeaders());
            restTemplate.exchange(API_VERSION + "/users/123Horn/vehicles/123456/ro/horn",
                    HttpMethod.PUT,
                    entity, String.class);
        });
    }


    @Test
    public void testCreateRemoteLightsRequestWithoutRequestId() throws IOException {
        LOGGER.debug("testing testCreateRemoteLightsRequestWithoutRequestId");
        String requestJson = IOUtils.toString(
                ROHornLightsAlarmControllerTest.class.getResourceAsStream("/remoteHornAndLightsRequest.json"), "UTF-8");
        HttpHeaders headers = createHeaders();
        headers.set("RequestId", StringUtils.EMPTY);
        HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
        ResponseEntity<String> response = restTemplate.exchange(API_VERSION
                        + "/users/123Lights/vehicles/123456/ro/lights",
                HttpMethod.PUT,
                entity, String.class);
        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        LOGGER.debug("RemoteOperationLightsReq :response body " + response.getBody());
        RemoteOperationResponse res = JsonUtils.parseInputJson(response.getBody(), RemoteOperationResponse.class);
        Assert.assertEquals("RO Command Sent Successfully", res.getMessage());
        Assert.assertEquals("-\\0274\\302]\\325\\324 \\354K", res.getRoRequestId());

    }

    @Test
    public void testCreateRemoteLightsOnlyRequest() throws IOException {
        LOGGER.debug("testing CreateRemoteLightsRequest");
        String requestJson = IOUtils.toString(
                ROHornLightsAlarmControllerTest.class.getResourceAsStream("/remoteHornAndLightsRequest.json"), "UTF-8");
        HttpEntity<String> entity = new HttpEntity<String>(requestJson, createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(API_VERSION_V12
                        + "/users/123Lights/vehicles/123456/ro/lights",
                HttpMethod.PUT,
                entity, String.class);
        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        LOGGER.debug("RemoteOperationLightsReq :response body " + response.getBody());
        RemoteOperationResponse res = JsonUtils.parseInputJson(response.getBody(), RemoteOperationResponse.class);
        Assert.assertEquals("RO Command Sent Successfully", res.getMessage());
        Assert.assertEquals("testRequestId", res.getRoRequestId());

    }

    @Test
    public void testCreateRemoteLightsOnlyRequestWithoutRequestId() throws IOException {
        LOGGER.debug("testing testCreateRemoteLightsOnlyRequestWithoutRequestId");
        String requestJson = IOUtils.toString(
                ROHornLightsAlarmControllerTest.class.getResourceAsStream("/remoteHornAndLightsRequest.json"), "UTF-8");
        HttpHeaders headers = createHeaders();
        headers.set("requestId", StringUtils.EMPTY);
        HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
        ResponseEntity<String> response = restTemplate.exchange(API_VERSION_V12
                        + "/users/123Lights/vehicles/123456/ro/lights",
                HttpMethod.PUT,
                entity, String.class);
        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        LOGGER.debug("RemoteOperationLightsReq :response body " + response.getBody());
        RemoteOperationResponse res = JsonUtils.parseInputJson(response.getBody(), RemoteOperationResponse.class);
        Assert.assertEquals("RO Command Sent Successfully", res.getMessage());
        Assert.assertEquals("-\\0274\\302]\\325\\324 \\354K", res.getRoRequestId());

    }

    @Test
    public void testCreateRemoteLightsRequest_invalidCommand() throws IOException {
        LOGGER.debug("testing CreateRemoteLightsRequest_invalidCommand");
        Assert.assertThrows(HttpClientErrorException.BadRequest.class, () -> {
            String requestJson = IOUtils.toString(
                    ROHornLightsAlarmControllerTest.class
                            .getResourceAsStream("/remoteHornAndLightsRequest_invalid_command.json"), "UTF-8");
            HttpEntity<String> entity = new HttpEntity<String>(requestJson, createHeaders());
            restTemplate.exchange(API_VERSION + "/users/123Lights/vehicles/123456/ro/lights",
                    HttpMethod.PUT,
                    entity, String.class);
        });
    }

    @Test
    public void testCreateRemoteAlarmRequest_invalidCommand() throws IOException {
        LOGGER.debug("testing CreateRemoteHornAndLightsRequest_invalidCommand");
        Assert.assertThrows(HttpClientErrorException.BadRequest.class, () -> {
            String requestJson = IOUtils.toString(
                    ROHornLightsAlarmControllerTest.class.getResourceAsStream(
                            "/remoteHornAndLightsRequest_invalid_command.json"), "UTF-8");
            HttpEntity<String> entity = new HttpEntity<String>(requestJson, createHeaders());
            restTemplate.exchange(API_VERSION + "/users/123Alarm/vehicles/123456/ro/alarm",
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
