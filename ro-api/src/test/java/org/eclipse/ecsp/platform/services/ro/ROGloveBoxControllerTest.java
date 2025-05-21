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
import org.eclipse.ecsp.platform.services.ro.rest.ROGloveBoxController;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResponseErrorHandler;
import java.io.IOException;

/**
 * Test class for {@link ROGloveBoxController}.
 *
 * @author Arnold
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:/application-test.properties")
public class ROGloveBoxControllerTest extends CommonTestBase {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ROGloveBoxControllerTest.class);
    private static final String API_VERSION = "/v2";
    private static final String REST_PATH = "/ro/glovebox";

    private ResponseErrorHandler preExistingErrorHandler;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private KafkaService kafkaService;

    @MockBean
    private ApiRequestHandler apiRequestHandler;

    /**
     * Set up the test.
     *
     * @throws Exception if an error occurs
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
    public void testCreateRemoteGloveBoxRequest() throws IOException {
        LOGGER.debug("testing CreateRemoteGloveRequest");
        String requestJson = IOUtils.toString(
                ROGloveBoxControllerTest.class.getResourceAsStream("/remoteGloveBoxRequest.json"), "UTF-8");
        HttpEntity<String> entity = new HttpEntity<String>(requestJson, createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(API_VERSION + REST_PATH,
                HttpMethod.PUT,
                entity, String.class);
        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        LOGGER.debug("RemoteOperationGloveBoxReq :response body " + response.getBody());
        RemoteOperationResponse res = JsonUtils.parseInputJson(response.getBody(), RemoteOperationResponse.class);
        Assert.assertEquals("RO Command Sent Successfully", res.getMessage());
        Assert.assertEquals("testRequestId", res.getRoRequestId());
    }

    @Test
    public void testCreateRemoteGloveBoxRequest_invalidCommand() throws IOException {
        LOGGER.debug("testing CreateRemoteDoorRequest_invalidCommand");
        Assert.assertThrows(HttpStatusCodeException.class, () -> {
            String requestJson = IOUtils.toString(
                    ROGloveBoxControllerTest.class
                            .getResourceAsStream("/remoteGloveBoxRequest_invalid_command.json"), "UTF-8");
            HttpHeaders headers = createHeaders();
            headers.set("RequestId", StringUtils.EMPTY);
            HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
            restTemplate.exchange(API_VERSION + REST_PATH,
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
        headers.add("userId", "123Engine");
        headers.add("vehicleId", "123456789");
        return headers;
    }
}
