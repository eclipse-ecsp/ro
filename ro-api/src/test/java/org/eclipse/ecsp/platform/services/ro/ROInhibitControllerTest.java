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
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.remoteInhibit.CrankNotificationDataV1_0;
import org.eclipse.ecsp.domain.remoteInhibit.RemoteInhibitRequestV1_1;
import org.eclipse.ecsp.domain.remoteInhibit.RemoteInhibitRequestV1_1.CrankInhibit;
import org.eclipse.ecsp.domain.remoteInhibit.RemoteInhibitResponseV1_1;
import org.eclipse.ecsp.domain.remoteInhibit.RemoteInhibitResponseV1_1.Response;
import org.eclipse.ecsp.domain.ro.Ro;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.domain.ro.constant.TestConstants;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.platform.services.ro.dao.RoDAOMongoImpl;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationResponse;
import org.eclipse.ecsp.platform.services.ro.handler.ApiRequestHandler;
import org.eclipse.ecsp.platform.services.ro.rest.RemoteInhibitController;
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
import java.util.ArrayList;

/**
 * Test class for {@link RemoteInhibitController}.
 *
 * @author pkumar16
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:/application-test.properties")
public class ROInhibitControllerTest extends CommonTestBase {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ROInhibitControllerTest.class);
    private static final String API_VERSION = "/v1.1";

    private ResponseErrorHandler preExistingErrorHandler;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private KafkaService kafkaService;

    @Autowired
    private RoDAOMongoImpl roDAO;

    @MockBean
    private ApiRequestHandler apiRequestHandler;


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
        roDAO.deleteAll();
        insertTestEventData();
        Mockito.when(apiRequestHandler.preHandle(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
    }

    @Test
    public void testCreateRemoteInhibitRequest() throws IOException {
        LOGGER.debug("testing CreateRemoteInhibitRequest");
        String requestJson = IOUtils.toString(
                ROInhibitControllerTest.class.getResourceAsStream("/roInhibitRequest.json"), "UTF-8");
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                API_VERSION + "/users/pkumar/vehicles/abc123/ro/inhibit",
                HttpMethod.POST,
                entity, String.class);
        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        LOGGER.debug("RemoteInhibitRequest :response body " + response.getBody());
        RemoteOperationResponse res = JsonUtils.parseInputJson(response.getBody(), RemoteOperationResponse.class);
        Assert.assertEquals("RI Command Sent Successfully", res.getMessage());
    }

    @Test
    public void testCreateRemoteInhibitRequestWithOutRequestId() throws IOException {
        LOGGER.debug("testing CreateRemoteInhibitRequest");
        String requestJson = IOUtils.toString(
                ROInhibitControllerTest.class.getResourceAsStream("/roInhibitRequest.json"), "UTF-8");
        HttpHeaders headers = createHeaders();
        headers.set("RequestId", StringUtils.EMPTY);
        headers.set("vehicleArchType", "VEHICLE_ARCHTYPE2");
        headers.set("ecuType", "ECU1");

        HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                API_VERSION + "/users/pkumar/vehicles/abc123/ro/inhibit",
                HttpMethod.POST,
                entity, String.class);
        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        LOGGER.debug("RemoteInhibitRequest :response body " + response.getBody());
        RemoteOperationResponse res = JsonUtils.parseInputJson(response.getBody(), RemoteOperationResponse.class);
        Assert.assertEquals("RI Command Sent Successfully", res.getMessage());
        Assert.assertEquals(null, res.getRoRequestId());
    }

    @Test
    public void testGetRemoteInhibitResponse() throws IOException {
        LOGGER.debug("testing CreateRemoteInhibitResponse");
        HttpEntity<String> entity = new HttpEntity<String>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                "/v2" + "/users/pkumar16/vehicles/vehicleId/remoteinhibit/requests/123456",
                HttpMethod.GET, entity, String.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
    }

    /**
     * testGetEmptyRemoteInhibitResponse().
     *
     * @throws IOException IOException
     */
    public void testGetEmptyRemoteInhibitResponse() throws IOException {
        LOGGER.debug("testing GetEmptyRemoteInhibitResponse");
        Assert.assertThrows(HttpClientErrorException.class, () -> {
            roDAO.deleteAll();
            HttpEntity<String> entity = new HttpEntity<String>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    "/v2" + "/users/pkumar16/vehicles/vehicleId/remoteinhibit/requests/123456",
                    HttpMethod.GET, entity, String.class);
            Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
            Assert.assertNotNull(response.getBody());
        });
    }

    @Test
    public void testGetRemoteInhibitResponsePartial() throws IOException {
        LOGGER.debug("testing CreateRemoteInhibitResponsePartial");
        HttpEntity<String> entity = new HttpEntity<String>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                API_VERSION + "/users/pkumar16/vehicles/vehicleId/remoteinhibit/requests/123456",
                HttpMethod.GET, entity, String.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
    }

    @Test
    public void testGetEmptyRemoteInhibitResponsePartial() throws IOException {
        LOGGER.debug("testing CreateRemoteInhibitResponsePartial");
        Assert.assertThrows(HttpClientErrorException.class, () -> {
            roDAO.deleteAll();
            HttpEntity<String> entity = new HttpEntity<String>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(API_VERSION
                            + "/users/pkumar16/vehicles/vehicleId/remoteinhibit/requests/123456",
                    HttpMethod.GET, entity, String.class);
            Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
            Assert.assertNotNull(response.getBody());
        });
    }

    private IgniteEventImpl createRIEvents(String eventType) {
        IgniteEventImpl eventImpl = new IgniteEventImpl();

        switch (eventType) {

            case Constants.EVENT_ID_REMOTE_INHIBIT_REQUEST: {
                RemoteInhibitRequestV1_1 riRequestData = new RemoteInhibitRequestV1_1();
                riRequestData.setCrankInhibit(CrankInhibit.INHIBIT);
                riRequestData.setOrigin("StolenRemoteInhibitSupportOwner"); // StolenRemoteInhibitSupportOwner,
                // FleetRemoteInhibitOwner
                riRequestData.setRoRequestId("123456");
                riRequestData.setUserId("pkumar16");

                eventImpl = new IgniteEventImpl();
                getIgniteEvent(eventImpl, riRequestData);

                break;
            }
            case Constants.EVENT_ID_REMOTE_INHIBIT_RESPONSE: {
                RemoteInhibitResponseV1_1 riResponseData = new RemoteInhibitResponseV1_1();
                riResponseData.setResponse(Response.SUCCESS);
                riResponseData.setUserId("pkumar16");
                riResponseData.setRoRequestId("requestId");
                riResponseData.setOrigin("StolenRemoteInhibitSupportOwner");

                eventImpl = new IgniteEventImpl();
                eventImpl.setBizTransactionId("bizTransactionId");
                eventImpl.setEventData(riResponseData);
                eventImpl.setEventId(Constants.EVENT_ID_REMOTE_INHIBIT_RESPONSE);
                eventImpl.setSchemaVersion(Version.V1_1);
                eventImpl.setRequestId("123456");
                eventImpl.setTimestamp(System.currentTimeMillis());
                eventImpl.setTimezone((short) TestConstants.THIRTY);
                eventImpl.setVehicleId("vehicleId");
                eventImpl.setCorrelationId("123456");

                break;
            }

            case Constants.EVENT_ID_CRANK_NOTIFICATION_DATA: {
                CrankNotificationDataV1_0 crankNotiData = new CrankNotificationDataV1_0();
                crankNotiData.setRoRequestId("requestId");
                crankNotiData.setUserId("pkumar16");
                crankNotiData.setRoRequestId("requestId");

                crankNotiData.setCrankAttempted(true);
                crankNotiData.setLatitude(TestConstants.LATITUDE);
                crankNotiData.setLongitude(TestConstants.LONGITUDE);
                crankNotiData.setHorPosError(TestConstants.SEVEN);
                crankNotiData.setBearing(TestConstants.DOUBLE_2);
                crankNotiData.setAltitude(TestConstants.DOUBLE_12_45);

                eventImpl = new IgniteEventImpl();
                eventImpl.setBizTransactionId("bizTransactionId");
                eventImpl.setEventData(crankNotiData);
                eventImpl.setEventId(Constants.EVENT_ID_CRANK_NOTIFICATION_DATA);
                eventImpl.setSchemaVersion(Version.V1_1);
                eventImpl.setRequestId("123456");
                eventImpl.setTimestamp(System.currentTimeMillis());
                eventImpl.setTimezone((short) TestConstants.THIRTY);
                eventImpl.setVehicleId("vehicleId");

                break;
            }
            default: {
                break;
            }

        }

        return eventImpl;
    }

    private static void getIgniteEvent(IgniteEventImpl eventImpl, RemoteInhibitRequestV1_1 riRequestData) {
        eventImpl.setBizTransactionId("bizTransactionId");
        eventImpl.setEventData(riRequestData);
        eventImpl.setEventId(Constants.EVENT_ID_REMOTE_INHIBIT_REQUEST);
        eventImpl.setSchemaVersion(Version.V1_1);
        eventImpl.setMessageId("123456");
        eventImpl.setRequestId("123456");
        eventImpl.setTimestamp(System.currentTimeMillis());
        eventImpl.setTimezone((short) TestConstants.THIRTY);
        eventImpl.setVehicleId("vehicleId");
    }

    private void insertTestEventData() {
        IgniteEventImpl riRequestimpl = createRIEvents(Constants.EVENT_ID_REMOTE_INHIBIT_REQUEST);
        IgniteEventImpl riResponseimpl = createRIEvents(Constants.EVENT_ID_REMOTE_INHIBIT_RESPONSE);
        IgniteEventImpl crankNotimpl = createRIEvents(Constants.EVENT_ID_CRANK_NOTIFICATION_DATA);
        insertRIResopnseInMongo(riRequestimpl, riResponseimpl);
        insertRIEventInMongo(crankNotimpl);
    }

    private void insertRIEventInMongo(IgniteEventImpl value) {

        Ro riEntity = new Ro();
        riEntity.setSchemaVersion(value.getSchemaVersion());
        riEntity.setRoEvent(value);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("persisting to mongo ,entity :{}", riEntity);
        }
        roDAO.save(riEntity);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Number of entries inserted into MongoDB ,entity :{}", riEntity);
        }
    }

    private void insertRIResopnseInMongo(IgniteEventImpl request, IgniteEventImpl response) {

        Ro riEntity = new Ro();
        riEntity.setSchemaVersion(request.getSchemaVersion());
        riEntity.setRoEvent(request);
        riEntity.setRoResponseList(new ArrayList<IgniteEvent>() {
            {
                add(response);
            }
        });
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("persisting to mongo ,entity :{}", riEntity);
        }
        roDAO.save(riEntity);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Number of entries inserted into MongoDB ,entity :{}", riEntity);
        }
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
}
