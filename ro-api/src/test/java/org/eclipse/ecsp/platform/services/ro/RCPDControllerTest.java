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
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import io.prometheus.client.CollectorRegistry;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.cache.IgniteCache;
import org.eclipse.ecsp.cache.PutStringRequest;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.RCPD;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.domain.ro.constant.TestConstants;
import org.eclipse.ecsp.domain.ro.dao.RCPDDAOMongoImpl;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.platform.services.ro.domain.RCPDResponse;
import org.eclipse.ecsp.platform.services.ro.handler.ApiRequestHandler;
import org.eclipse.ecsp.platform.services.ro.rest.RCPDController;
import org.eclipse.ecsp.platform.services.ro.service.IgniteEventImplBuilder;
import org.eclipse.ecsp.services.utils.ServiceUtil;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.eclipse.ecsp.utils.JsonUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test class for {@link RCPDController}.
 */
@SpringBootTest(classes = org.eclipse.ecsp.Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:/application-test.properties")
public class RCPDControllerTest extends CommonTestBase {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(RCPDControllerTest.class);

    private static final String API_VERSION = "/v1";

    private static ObjectMapper mapper = ServiceUtil.createJsonMapperForIgniteEvent();

    private ResponseErrorHandler preExistingErrorHandler;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private KafkaService kafkaService;

    @MockBean
    private ApiRequestHandler apiRequestHandler;

    @Autowired
    private IgniteCache igniteCache;

    @Autowired
    private RCPDDAOMongoImpl rcpdDAO;


    public void setup() {
        super.setup();
    }

    /**
     * Setup each test.
     *
     * @throws Exception Exception
     */
    @Before
    public void setUpTest() throws Exception {
        CollectorRegistry.defaultRegistry.clear();
        mapper.enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION.mappedFeature());
        mapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.WRAPPER_ARRAY
        );
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        preExistingErrorHandler = restTemplate.getRestTemplate().getErrorHandler();
        restTemplate.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler());
        rcpdDAO.deleteAll();
        Mockito.when(apiRequestHandler.preHandle(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
    }


    public void cleanup() {
        restTemplate.getRestTemplate().setErrorHandler(preExistingErrorHandler);
        CollectorRegistry.defaultRegistry.clear();
    }

    /**
     * Utility method: convertJSonStringToMap().
     *
     * @param responseBody responseBody
     * @return Map
     */
    public static Map<String, Object> convertJSonStringToMap(String responseBody) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            map = mapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            LOGGER.debug("Error occured while converting string to map");
        }
        return map;
    }

    @Ignore
    @Test
    public void testCreateRCPDRequest() throws Exception {
        setUpTest();
        LOGGER.debug("testing CreateRCPDRequest");
        HttpEntity<String> entity = new HttpEntity<String>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange("/v1/rcpd",
                HttpMethod.PUT,
                entity, String.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        //Assert.assertNotNull(response.getBody());
        LOGGER.debug("RCPD :response body " + response.getBody());
        //RCPDResponse res = JsonUtils.parseInputJson(response.getBody(), RCPDResponse.class);
        //Assert.assertEquals("RCPD Command Sent Successfully", res.getMessage());
        //Assert.assertEquals("testRequestId", res.getRcpdRequestId());
    }

    @Ignore
    @Test
    public void testGetRCPDDataWithRequestId() throws IOException {
        populateRCPDData();
        // Check inserted data
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                "/v1/rcpd/requests/02294900-bb85-4657-92a5-4b12a2201149?responsesLimit=1", HttpMethod.GET,
                entity, String.class);
        Assert.assertNotNull(response.getBody());
        JsonNode rootNode = mapper.readTree(response.getBody());
        Assert.assertEquals(TestConstants.TWO, rootNode.size());
        String rcpdRequestId = rootNode.get("rcpdEvent").get("Data").get("rcpdRequestId").asText();
        Assert.assertEquals("02294900-bb85-4657-92a5-4b12a2201149", rcpdRequestId);
    }

    @Test
    public void testGetRCPDDataWithRequestIdFor404() throws IOException {

        // Check inserted data
        Assert.assertThrows(HttpClientErrorException.class, () -> {
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            restTemplate.exchange(
                    "/v1/rcpd/requests/02294900-bb85-4657-92a5-4b12a2201149?responsesLimit=1", HttpMethod.GET,
                    entity, String.class);
        });
    }

    @Ignore
    @Test
    public void testGetRCPDDataWithMultiResponse() throws IOException {

        populateNoResponseRCPDData();
        // Check inserted data
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                "/v1/rcpd/requests/02294900-bb85-4657-92a5-4b12a2201149?responsesLimit=1", HttpMethod.GET,
                entity, String.class);
        Assert.assertNotNull(response.getBody());
        JsonNode rootNode = mapper.readTree(response.getBody());
        Assert.assertEquals(TestConstants.TWO, rootNode.size());
        String rcpdRequestId = rootNode.get("rcpdEvent").get("Data").get("rcpdRequestId").asText();
        Assert.assertEquals("02294900-bb85-4657-92a5-4b12a2201149", rcpdRequestId);
    }

    @Test
    public void testGetRCPDStatus() {
        String value = "";
        Map<String, Object> mapOfVehicleStatus = new HashMap<String, Object>();
        mapOfVehicleStatus.put("vehicleStatus", "SUCCESS");
        mapOfVehicleStatus.put("userId", "11111");
        mapOfVehicleStatus.put("offBoardStatus", "11111");
        mapOfVehicleStatus.put("timestamp", System.currentTimeMillis());

        // convert map to JSON string
        try {
            value = mapper.writeValueAsString(mapOfVehicleStatus);
            LOGGER.debug("Map value as Json string : " + value);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());

        igniteCache.putString(
                new PutStringRequest().withKey(Constants.getRedisKey(Constants
                                .RCPD_SERVICE, "12345678", Constants.RCPD_STATUS))
                        .withValue(value));
        ResponseEntity<String> response = restTemplate.exchange(
                "/v1/rcpd/status",
                HttpMethod.GET, entity, String.class);
        String responseBody = response.getBody();
        LOGGER.debug("Response : " + response + " Response Body : " + responseBody);
        Map<String, Object> map = convertJSonStringToMap(responseBody);
        Assert.assertNotNull(response);
        //Assert.assertEquals("SUCCESS", map.get("vehicleStatus"));
    }

    @Test
    public void testGetRCPDStatusNotFoundException() {
        Assert.assertThrows(HttpClientErrorException.class, () -> {
            String value = "";
            Map<String, Object> mapOfVehicleStatus = new HashMap<String, Object>();
            mapOfVehicleStatus.put("vehicleStatus", "SUCCESS");
            mapOfVehicleStatus.put("userId", "11111");
            mapOfVehicleStatus.put("offBoardStatus", "11111");
            mapOfVehicleStatus.put("timestamp", System.currentTimeMillis());

            // convert map to JSON string
            try {
                value = mapper.writeValueAsString(mapOfVehicleStatus);
                LOGGER.debug("Map value as Json string : " + value);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            HttpHeaders headers = createHeaders();
            headers.set("userId", StringUtils.EMPTY);
            headers.set("vehicleId", StringUtils.EMPTY);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            igniteCache.putString(
                    new PutStringRequest().withKey(Constants.getRedisKey(Constants
                                    .RCPD_SERVICE, "12345678", Constants.RCPD_STATUS))
                            .withValue(value));
            ResponseEntity<String> response = restTemplate.exchange(
                    "/v1/rcpd/status",
                    HttpMethod.GET, entity, String.class);
            String responseBody = response.getBody();
            LOGGER.debug("Response : " + response + " Response Body : " + responseBody);
            Map<String, Object> map = convertJSonStringToMap(responseBody);
            Assert.assertNotNull(response);
            Assert.assertEquals("SUCCESS", map.get("vehicleStatus"));
        });
    }

    @Test
    public void testGetRCPDHistory() throws IOException, JSONException {
        // Insert more data for vehicle 12345678
        populateRCPDData();
        // Check inserted data
        HttpHeaders headers = createHeaders();
        headers.add("eventId", "");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "/v1/rcpd/history?responseCount=4", HttpMethod.GET, entity,
            String.class);
        Assert.assertNotNull(response.getBody());
        JSONObject responseObj = new JSONObject(response.getBody());
        JSONArray alertArray = (JSONArray) responseObj.get("alerts");
        Assert.assertEquals(TestConstants.ONE, alertArray.length());
        Assert.assertEquals(TestConstants.ONE, responseObj.get("totalRecords"));
    }

    @Test
    public void testGetRCPDHistoryWithRequestID() throws IOException, JSONException {
        // Insert more data for vehicle 12345678
        populateRCPDData();
        // Check inserted data
        HttpHeaders headers = createHeaders();
        headers.add("eventId", "");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "/v1/rcpd/history", HttpMethod.GET, entity,
            String.class);
        Assert.assertNotNull(response.getBody());
        JSONObject responseObj = new JSONObject(response.getBody());
        JSONArray alertArray = (JSONArray) responseObj.get("alerts");
        Assert.assertEquals(TestConstants.ONE, alertArray.length());
        Assert.assertEquals(TestConstants.ONE, responseObj.get("totalRecords"));
    }

    @Test
    public void testMultipleCreateRCPDRequest() throws IOException {
        Assert.assertThrows(HttpClientErrorException.TooManyRequests.class, () -> {
            igniteCache.delete(Constants.getRedisKey(Constants.RCPD_SERVICE, "12345678", Constants.RCPD_STATUS));
            LOGGER.debug("testing CreateRCPDRequest");
            HttpEntity<String> entity = new HttpEntity<String>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange("/v1/rcpd",
                    HttpMethod.PUT,
                    entity, String.class);
            Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
            Assert.assertNotNull(response.getBody());
            LOGGER.debug("RCPD :response body " + response.getBody());
            RCPDResponse res = JsonUtils.parseInputJson(response.getBody(), RCPDResponse.class);
            Assert.assertEquals("RCPD Command Sent Successfully", res.getMessage());
            Assert.assertEquals("testRequestId", res.getRcpdRequestId());
            response = restTemplate.exchange("/v1/rcpd",
                    HttpMethod.PUT,
                    entity, String.class);
        });
    }

    private void populateRCPDData() throws IOException {
        String requestJson = IOUtils.toString(
                ROStatusHistoryControllerTest.class.getResourceAsStream("/rcpdHistoryData.json"),
                "UTF-8");

        //        JsonNode inputRootNode = mapper.readTree(requestJson);
        //        List<RCPD> rcpdHistoryList = mapper.readValue(requestJson, new TypeReference<List<RCPD>>() {});
        //        for (RCPD rcpdHistoryData : rcpdHistoryList) {
        //            rcpdDAO.save(rcpdHistoryData);
        //        }
        IgniteEvent rcpdEvent = new IgniteEventImplBuilder()
            .withEventId("RCPDRequest")
            .withVersion(Version.V1_1)
            .withRequestId("38f20cb3-cdfa-45e5-8a3a-4e6e3087a914")
            .withVehicleId("12345678")
            .build();
        IgniteEvent rcpdResponse = new IgniteEventImplBuilder()
            .withEventId("RCPDResponse")
            .withVersion(Version.V1_1)
            .withRequestId("38f20cb3-cdfa-45e5-8a3a-4e6e3087a914")
            .withVehicleId("12345678")
            .build();
        List<IgniteEvent> rcpdResponseList = List.of(rcpdResponse);
        RCPD rcpdHistory = new RCPD();
        rcpdHistory.setRcpdEvent(rcpdEvent);
        rcpdHistory.setRcpdResponseList(rcpdResponseList);
        rcpdDAO.save(rcpdHistory);
        //LOGGER.info(inputRootNode.size() + " remote operations stored to DB");
    }

    private void populateNoResponseRCPDData() throws IOException {
        String requestJson = IOUtils.toString(
            ROStatusHistoryControllerTest.class.getResourceAsStream("/rcpdHistoryDataNoResponseList.json"),
            "UTF-8");
        //        JsonNode inputRootNode = mapper.readTree(requestJson);
        //        RCPD rcpdHistoryData = null;
        //        for (JsonNode jsonNode : inputRootNode) {
        //           rcpdHistoryData = mapper.readValue(jsonNode.toString(), RCPD.class);
        //            rcpdDAO.save(rcpdHistoryData);
        //        }
        //        LOGGER.info(inputRootNode.size() + " remote operations stored to DB");
        IgniteEvent rcpdEvent = new IgniteEventImplBuilder()
            .withEventId("RCPDRequest")
            .withVersion(Version.V1_1)
            .withRequestId("38f20cb3-cdfa-45e5-8a3a-4e6e3087a914")
            .withVehicleId("12345678")
            .build();
        IgniteEvent rcpdResponse = new IgniteEventImplBuilder()
            .withEventId("RCPDResponse")
            .withVersion(Version.V1_1)
            .withRequestId("38f20cb3-cdfa-45e5-8a3a-4e6e3087a914")
            .withVehicleId("12345678")
            .build();
        List<IgniteEvent> rcpdResponseList = List.of(rcpdResponse);
        RCPD rcpdHistory = new RCPD();
        rcpdHistory.setRcpdEvent(rcpdEvent);
        rcpdHistory.setRcpdResponseList(rcpdResponseList);
        rcpdDAO.save(rcpdHistory);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("ClientRequestId", "testGlobalTrackingId");
        headers.add("RequestId", "testRequestId");
        headers.add("SessionId", "testGlobalSessionId");
        headers.add("OriginId", "test");
        headers.add("UserId", "testdata");
        headers.add("VehicleId", "12345678");

        return headers;
    }
}
