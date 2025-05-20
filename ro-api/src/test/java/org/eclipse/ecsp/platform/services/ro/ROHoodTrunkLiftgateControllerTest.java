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
import org.eclipse.ecsp.platform.services.ro.rest.ROHoodTrunkLiftgateController;
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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResponseErrorHandler;
import java.io.IOException;

/**
 * Test class for {@link ROHoodTrunkLiftgateController}.
 *
 * @author midnani
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:/application-test.properties")
public class ROHoodTrunkLiftgateControllerTest extends CommonTestBase {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ROHoodTrunkLiftgateControllerTest.class);
    private static final String API_VERSION = "/v1.1";
    private static final String API_V2 = "/v2";
    private static final String REST_PATH_TRUNK = "/ro/trunk";
    private static final String REST_PATH_LIFTGATE = "/ro/liftgate";

    private ResponseErrorHandler preExistingErrorHandler;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private KafkaService kafkaService;

    @MockBean
    private ApiRequestHandler apiRequestHandler;

    /**
     * Set up the test.
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
    public void testCreateRemoteHoodRequest() throws IOException {
        LOGGER.debug("testing CreateRemoteHoodRequest");
        String requestJson = IOUtils.toString(
                ROHoodTrunkLiftgateControllerTest.class
                        .getResourceAsStream("/remoteHoodAndLiftgateRequest.json"), "UTF-8");
        HttpEntity<String> entity = new HttpEntity<String>(requestJson, createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(API_VERSION + "/users/123Hood/vehicles/123456/ro/hood",
                HttpMethod.PUT,
                entity, String.class);
        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        LOGGER.debug("RemoteOperationHoodReq :response body " + response.getBody());
        RemoteOperationResponse res = JsonUtils.parseInputJson(response.getBody(), RemoteOperationResponse.class);
        Assert.assertEquals("RO Command Sent Successfully", res.getMessage());
        Assert.assertEquals("testRequestId", res.getRoRequestId());
    }

    @Test
    public void testCreateRemoteHoodRequest_invalidCommand() throws IOException {
        LOGGER.debug("testing CreateRemoteHoodRequest_invalidCommand");
        Assert.assertThrows(HttpClientErrorException.BadRequest.class, () -> {
            String requestJson = IOUtils.toString(
                    ROHoodTrunkLiftgateControllerTest
                            .class.getResourceAsStream("/remoteHoodAndLiftgateRequest_invalid_command.json"),
                    "UTF-8");
            HttpHeaders headers = createHeaders();
            headers.set("RequestId", StringUtils.EMPTY);
            HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
            restTemplate.exchange(API_VERSION + "/users/123Hood/vehicles/123456/ro/hood",
                    HttpMethod.PUT,
                    entity, String.class);
        });
    }

    @Test
    public void testCreateRemoteTrunkRequest() throws IOException {
        LOGGER.debug("testing CreateRemoteTrunkRequest");
        String requestJson = IOUtils.toString(
                ROHoodTrunkLiftgateControllerTest.class.getResourceAsStream("/remoteTrunkRequest.json"), "UTF-8");
        HttpEntity<String> entity = new HttpEntity<String>(requestJson, createHeaders());
        ResponseEntity<String> response = restTemplate
                .exchange(API_VERSION + "/users/123Trunk/vehicles/123456/ro/trunk",
                        HttpMethod.PUT,
                        entity, String.class);
        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        LOGGER.debug("RemoteOperationTrunkRequest :response body " + response.getBody());
        RemoteOperationResponse res = JsonUtils.parseInputJson(response.getBody(), RemoteOperationResponse.class);
        Assert.assertEquals("RO Command Sent Successfully", res.getMessage());
        Assert.assertEquals("testRequestId", res.getRoRequestId());
    }

    @Test
    public void testCreateRemoteTrunkRequest_invalidCommand() throws IOException {
        LOGGER.debug("testing CreateRemoteTrunkRequest_invalidCommand");
        Assert.assertThrows(HttpClientErrorException.BadRequest.class, () -> {
            String requestJson = IOUtils.toString(
                    ROHoodTrunkLiftgateControllerTest.class
                            .getResourceAsStream("/remoteTrunkRequest_invalid_command.json"),
                    "UTF-8");
            HttpEntity<String> entity = new HttpEntity<String>(requestJson, createHeaders());
            restTemplate.exchange(API_VERSION + "/users/123Trunk/vehicles/123456/ro/trunk",
                    HttpMethod.PUT,
                    entity, String.class);
        });
    }

    @Test
    public void testCreateRemoteTrunkRequest_V2() throws IOException {
        LOGGER.debug("testing CreateRemoteTrunkRequest");
        String requestJson = IOUtils.toString(
                ROHoodTrunkLiftgateControllerTest.class.getResourceAsStream("/remoteTrunkRequest.json"), "UTF-8");
        HttpEntity<String> entity = new HttpEntity<String>(requestJson, createV2Headers());
        ResponseEntity<String> response = restTemplate.exchange(API_V2 + REST_PATH_TRUNK,
                HttpMethod.PUT,
                entity, String.class);
        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        LOGGER.debug("RemoteOperationTrunkRequest :response body " + response.getBody());
        RemoteOperationResponse res = JsonUtils.parseInputJson(response.getBody(), RemoteOperationResponse.class);
        Assert.assertEquals("RO Command Sent Successfully", res.getMessage());
        Assert.assertEquals("testRequestId", res.getRoRequestId());
    }

    @Test
    public void testCreateRemoteTrunkRequest_V2_invalidCommand() throws IOException {
        LOGGER.debug("testing CreateRemoteTrunkRequest_invalidCommand");
        Assert.assertThrows(HttpStatusCodeException.class, () -> {
            String requestJson = IOUtils.toString(
                    ROHoodTrunkLiftgateControllerTest.class
                            .getResourceAsStream("/remoteTrunkRequest_invalid_command.json"),
                    "UTF-8");
            HttpEntity<String> entity = new HttpEntity<String>(requestJson, createV2Headers());
            restTemplate.exchange(API_V2 + REST_PATH_TRUNK,
                    HttpMethod.PUT,
                    entity, String.class);
        });
    }

    @Test
    public void testCreateRemoteLiftgateRequest() throws IOException {
        LOGGER.debug("testing CreateRemoteLiftgateRequest");
        String requestJson = IOUtils.toString(
                ROHoodTrunkLiftgateControllerTest.class
                        .getResourceAsStream("/remoteHoodAndLiftgateRequest.json"), "UTF-8");
        HttpEntity<String> entity = new HttpEntity<String>(requestJson, createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(API_VERSION
                        + "/users/123Liftgate/vehicles/123456/ro/liftgate",
                HttpMethod.PUT,
                entity, String.class);
        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        LOGGER.debug("RemoteOperationLiftgateRequest :response body " + response.getBody());
        RemoteOperationResponse res = JsonUtils.parseInputJson(response.getBody(), RemoteOperationResponse.class);
        Assert.assertEquals("RO Command Sent Successfully", res.getMessage());
        Assert.assertEquals("testRequestId", res.getRoRequestId());
    }

    @Test
    public void testCreateRemoteLiftgateRequest_invalidCommand() throws IOException {
        LOGGER.debug("testing CreateRemoteLiftgateRequest_invalidCommand");
        Assert.assertThrows(HttpClientErrorException.BadRequest.class, () -> {
            String requestJson = IOUtils.toString(
                    ROHoodTrunkLiftgateControllerTest.class
                            .getResourceAsStream("/remoteHoodAndLiftgateRequest_invalid_command.json"),
                    "UTF-8");
            HttpEntity<String> entity = new HttpEntity<String>(requestJson, createHeaders());
            restTemplate.exchange(API_VERSION + "/users/123Liftgate/vehicles/123456/ro/liftgate",
                    HttpMethod.PUT,
                    entity, String.class);
        });
    }

    @Test
    public void testCreateRemoteLiftgateRequest_V2() throws IOException {
        LOGGER.debug("testing CreateRemoteLiftgateRequest");
        String requestJson = IOUtils.toString(
                ROHoodTrunkLiftgateControllerTest.class
                        .getResourceAsStream("/remoteHoodAndLiftgateRequest.json"), "UTF-8");
        HttpEntity<String> entity = new HttpEntity<String>(requestJson, createV2Headers());
        ResponseEntity<String> response = restTemplate.exchange(API_V2 + REST_PATH_LIFTGATE,
                HttpMethod.PUT,
                entity, String.class);
        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        LOGGER.debug("RemoteOperationLiftgateRequest :response body " + response.getBody());
        RemoteOperationResponse res = JsonUtils.parseInputJson(response.getBody(), RemoteOperationResponse.class);
        Assert.assertEquals("RO Command Sent Successfully", res.getMessage());
        Assert.assertEquals("testRequestId", res.getRoRequestId());
    }

    @Test
    public void testCreateRemoteLiftgateRequest_V2_invalidCommand() throws IOException {
        LOGGER.debug("testing CreateRemoteLiftgateRequest_invalidCommand");
        Assert.assertThrows(HttpClientErrorException.BadRequest.class, () -> {
            String requestJson = IOUtils.toString(
                    ROHoodTrunkLiftgateControllerTest.class
                            .getResourceAsStream("/remoteHoodAndLiftgateRequest_invalid_command.json"),
                    "UTF-8");
            HttpEntity<String> entity = new HttpEntity<String>(requestJson, createV2Headers());
            restTemplate.exchange(API_V2 + REST_PATH_LIFTGATE,
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
        return headers;
    }

    private HttpHeaders createV2Headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("ClientRequestId", "testGlobalTrackingId");
        headers.add("RequestId", "testRequestId");
        headers.add("SessionId", "testGlobalSessionId");
        headers.add("OriginId", "test");
        headers.add("userId", "123Engine");
        headers.add("vehicleId", "123456789");
        return headers;
    }
}
