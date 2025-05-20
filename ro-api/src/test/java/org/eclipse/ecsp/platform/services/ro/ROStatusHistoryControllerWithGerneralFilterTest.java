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

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.prometheus.client.CollectorRegistry;
import org.apache.commons.io.IOUtils;
import org.eclipse.ecsp.domain.ro.Ro;
import org.eclipse.ecsp.domain.ro.constant.TestConstants;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.platform.services.ro.constant.NumericConstants;
import org.eclipse.ecsp.platform.services.ro.dao.RoDAOMongoImpl;
import org.eclipse.ecsp.platform.services.ro.handler.ApiRequestHandler;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.ResponseErrorHandler;
import java.io.IOException;

/**
 * test class for ROStatusHistoryControllerWithGerneralFilter.
 *
 * @author midnani
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:/application-test.properties")
public class ROStatusHistoryControllerWithGerneralFilterTest extends CommonTestBase {

    private static final IgniteLogger LOGGER
            = IgniteLoggerFactory.getLogger(ROStatusHistoryControllerWithGerneralFilterTest.class);

    private static final String API_VERSION = "/v1.1";

    private static ObjectMapper mapper = new ObjectMapper();

    private ResponseErrorHandler preExistingErrorHandler;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RoDAOMongoImpl roDAOMongoImpl;

    @MockBean
    private ApiRequestHandler apiRequestHandler;

    @MockBean
    private KafkaService kafkaService;

    @After
    public void tearDown() {
        CollectorRegistry.defaultRegistry.clear();
        roDAOMongoImpl.deleteAll();
    }

    /**
     * setup method.
     *
     * @throws Exception Exception.
     */
    @Before
    public void setUp() throws Exception {
        CollectorRegistry.defaultRegistry.clear();
        mapper.enableDefaultTyping();
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        populateRemoteOperationsHistory();
        populateRemoteOperationsStatus();
        Mockito.when(apiRequestHandler.preHandle(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
    }

    @Test
    public void testGetRemoteOpHistory_oneResult() throws IOException {
        // Check inserted data
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(API_VERSION + "/users/testdata/vehicles/123/ro/history",
                HttpMethod.GET, entity, String.class);
        Assert.assertNotNull(response.getBody());
        JsonNode rootNode = mapper.readTree(response.getBody());
        JsonNode remoteHistoryData = rootNode.get(0);
        Assert.assertTrue(remoteHistoryData.has("roEvent"));
        Assert.assertEquals("UNLOCKED", remoteHistoryData.get("roEvent").get("Data").get("state").asText());
        Assert.assertEquals("RemoteOperationDoors", remoteHistoryData.get("roEvent").get("EventID").asText());
        Assert.assertEquals(TestConstants.TWO, remoteHistoryData.get("roResponseList").size());
        Assert.assertEquals(TestConstants.LONG_42323242342344L, remoteHistoryData
                .get("roResponseList").get(0).get("Timestamp").asLong());
    }

    // Insert total 11 records, fetch default results limited - 10
    @Test
    public void testGetRemoteOpHistory_verifyDefaultLimit() throws IOException {
        // Insert more data for vehicle 12345678
        populate10RemoteOperationsHistory();
        // Check inserted data
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(API_VERSION
                + "/users/testdata/vehicles/12345678/ro/history", HttpMethod.GET, entity, String.class);
        Assert.assertNotNull(response.getBody());
        JsonNode rootNode = mapper.readTree(response.getBody());
        Assert.assertEquals(TestConstants.TEN, rootNode.size());
        LOGGER.info("RemoteOperations History Result Size(Should be default): " + rootNode.size());
    }

    // Insert total 11 records, fetch limit
    @Test
    public void testGetRemoteOpHistory_verifyFetchLimit() throws IOException {
        // Insert more data for vehicle 12345678
        populate10RemoteOperationsHistory();
        // Check inserted data
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                API_VERSION + "/users/testdata/vehicles/12345678/ro/history?responsesLimit=10", HttpMethod.GET, entity,
                String.class);
        Assert.assertNotNull(response.getBody());
        JsonNode rootNode = mapper.readTree(response.getBody());
        Assert.assertEquals(TestConstants.TEN, rootNode.size());
        JsonNode remoteHistoryData1 = rootNode.get(0);
        Assert.assertTrue(remoteHistoryData1.has("roEvent"));
        Assert.assertEquals("UNLOCKED", remoteHistoryData1.get("roEvent").get("Data").get("state").asText());
        Assert.assertEquals(TestConstants.LONG_1555766166000L,
                remoteHistoryData1.get("roEvent").get("Timestamp").asLong());

        JsonNode remoteHistoryData5 = rootNode.get(TestConstants.FOUR);
        Assert.assertTrue(remoteHistoryData5.has("roEvent"));
        Assert.assertEquals("UNLOCKED", remoteHistoryData5.get("roEvent").get("Data").get("state").asText());
        Assert.assertEquals(TestConstants.LONG_1555766116000L,
                remoteHistoryData5.get("roEvent").get("Timestamp").asLong());
    }

    @Test
    public void testGetRemoteOpHistory_verifyFetchLimit_v2() throws IOException, JSONException {
        // Insert more data for vehicle 12345678
        populate10RemoteOperationsHistory();
        // Check inserted data
        HttpHeaders headers = createHeaders();
        headers.add("userId", "testdata");
        headers.add("vehicleId", "12345678");
        headers.add("eventId", "");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/v2/ro/history?responseCount=10", HttpMethod.GET, entity,
                String.class);
        Assert.assertNotNull(response.getBody());
        JSONObject responseObj = new JSONObject(response.getBody());
        JSONArray alertArray = (JSONArray) responseObj.get("alerts");
        Assert.assertEquals(TestConstants.TEN, alertArray.length());
        Assert.assertEquals(TestConstants.ELEVEN, responseObj.get("totalRecords"));
    }

    @Test
    public void testGetRemoteOpHistory_verify_eventid_v2() throws IOException, JSONException {
        // Insert more data for vehicle 12345678
        populate10RemoteOperationsHistory();
        // Check inserted data
        HttpHeaders headers = createHeaders();
        headers.add("userId", "testdata");
        headers.add("vehicleId", "12345678");
        headers.add("eventId", "");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/v2/ro/history?responseCount=10&eventId=RemoteOperationDoors", HttpMethod.GET, entity,
                String.class);
        Assert.assertNotNull(response.getBody());
        JSONObject responseObj = new JSONObject(response.getBody());
        JSONArray alertArray = (JSONArray) responseObj.get("alerts");
        Assert.assertEquals(TestConstants.TEN, alertArray.length());
        Assert.assertEquals(TestConstants.TEN, responseObj.get("totalRecords"));
    }

    @Test
    public void testGetRemoteOpHistory_verify_no_limit_v2() throws IOException, JSONException {
        // Insert more data for vehicle 12345678
        populate10RemoteOperationsHistory();
        // Check inserted data
        HttpHeaders headers = createHeaders();
        headers.add("userId", "testdata");
        headers.add("vehicleId", "12345678");
        headers.add("eventId", "");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/v2/ro/history", HttpMethod.GET, entity,
                String.class);
        Assert.assertNotNull(response.getBody());
        JSONObject responseObj = new JSONObject(response.getBody());
        JSONArray alertArray = (JSONArray) responseObj.get("alerts");
        Assert.assertEquals(TestConstants.ELEVEN, alertArray.length());
        Assert.assertEquals(TestConstants.ELEVEN, responseObj.get("totalRecords"));
    }

    @Test
    public void testGetRemoteOpHistory_verify_lastRecord_v2() throws IOException, JSONException {
        // Insert more data for vehicle 12345678
        populate10RemoteOperationsHistory();
        // Check inserted data
        HttpHeaders headers = createHeaders();
        headers.add("userId", "testdata");
        headers.add("vehicleId", "12345678");
        headers.add("eventId", "");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/v2/ro/history?responseCount=4&lastRecordId=06de1396-a6c9-4601-9c1e-853d14cf9ee5",
                HttpMethod.GET, entity, String.class);
        Assert.assertNotNull(response.getBody());
        JSONObject responseObj = new JSONObject(response.getBody());
        JSONArray alertArray = (JSONArray) responseObj.get("alerts");
        Assert.assertEquals(TestConstants.FOUR, alertArray.length());
        Assert.assertEquals(NumericConstants.SEVEN, responseObj.get("totalRecords"));
    }

    @Test
    public void testGetRemoteOpStatus_oneResult() throws IOException {
        // Check inserted data
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(API_VERSION
                        + "/users/111/vehicles/7777/ro/requests/P2KvQwet9A28bOrQgxQDcp60xhCP9O9a?responsesLimit=1",
                HttpMethod.GET, entity, String.class);
        Assert.assertNotNull(response.getBody());
        JsonNode rootNode = mapper.readTree(response.getBody());
        Assert.assertEquals(TestConstants.FOUR, rootNode.size());
        String remoteOpStatusData = rootNode.get("roEvent").get("Data").get("state").asText();
        String roRequestId = rootNode.get("roEvent").get("Data").get("roRequestId").asText();
        Assert.assertEquals("LOCKED", remoteOpStatusData);
        Assert.assertEquals("P2KvQwet9A28bOrQgxQDcp60xhCP9O9a", roRequestId);
    }

    @Test
    public void testEmptyHistory_v2() {
        HttpHeaders headers = createHeaders();
        headers.add("userId", "2");
        headers.add("vehicleId", "12345678");
        headers.add("eventId", "");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(API_VERSION + "/v2/ro/history",
                HttpMethod.GET, entity, String.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testEmptyHistory() {
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(API_VERSION + "/users/2/vehicles/12345678/ro/history",
                HttpMethod.GET, entity, String.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testEmptyHistoryForSingleRequest() {
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                API_VERSION + "/users/111/vehicles/7777/ro/requests/2323", HttpMethod.GET, entity, String.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // Populate Test data for History
    private void populateRemoteOperationsHistory() throws IOException {
        String requestJson = IOUtils.toString(
                ROStatusHistoryControllerWithGerneralFilterTest.class
                        .getResourceAsStream("/remoteHistoryRequest_1.json"), "UTF-8");
        Ro roHistoryData = mapper.readValue(requestJson, Ro.class);
        roDAOMongoImpl.save(roHistoryData);
        LOGGER.info("Saved History entry to DB");
    }

    // Populate test data for Remote Operation Status
    private void populateRemoteOperationsStatus() throws IOException {
        String requestJson = IOUtils.toString(
                ROStatusHistoryControllerWithGerneralFilterTest.class
                        .getResourceAsStream("/remoteStatusRequest_1.json"), "UTF-8");
        Ro roStatusData = mapper.readValue(requestJson, Ro.class);
        roDAOMongoImpl.save(roStatusData);
        requestJson = IOUtils.toString(
                ROStatusHistoryControllerWithGerneralFilterTest.class
                        .getResourceAsStream("/remoteStatusRequest_2.json"), "UTF-8");
        roStatusData = mapper.readValue(requestJson, Ro.class);
        roDAOMongoImpl.save(roStatusData);
        LOGGER.info("Saved 2 Remote Operation entries to DB");
    }

    // Populate 10 entries test data for History
    private void populate10RemoteOperationsHistory() throws IOException {
        String requestJson = IOUtils.toString(
                ROStatusHistoryControllerWithGerneralFilterTest.class
                        .getResourceAsStream("/remoteHistoryRequest_10_vehicles.json"),
                "UTF-8");
        JsonNode inputRootNode = mapper.readTree(requestJson);
        Ro roHistoryData = null;
        for (JsonNode jsonNode : inputRootNode) {
            roHistoryData = mapper.readValue(jsonNode.toString(), Ro.class);
            roDAOMongoImpl.save(roHistoryData);
        }
        LOGGER.info(inputRootNode.size() + " remote operations stored to DB");
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("ClientRequestId", "testGlobalTrackingId");
        headers.add("RequestId", "testRequestId");
        headers.add("SessionId", "testGlobalSessionId");
        headers.add("OriginId", "test");
        headers.add("PartnerId", "testPartnerId");
        return headers;
    }
}
