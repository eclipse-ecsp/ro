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

package org.eclipse.ecsp.ro.processor.ri;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.AuthorizedPartnerDetail;
import org.eclipse.ecsp.domain.AuthorizedPartnerDetailItem;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.remoteInhibit.CrankNotificationDataV1_0;
import org.eclipse.ecsp.domain.remoteInhibit.RemoteInhibitRequestV1_1;
import org.eclipse.ecsp.domain.remoteInhibit.RemoteInhibitRequestV1_1.CrankInhibit;
import org.eclipse.ecsp.domain.remoteInhibit.RemoteInhibitResponseV1_1;
import org.eclipse.ecsp.domain.remoteInhibit.RemoteInhibitResponseV1_1.Response;
import org.eclipse.ecsp.domain.ro.Ro;
import org.eclipse.ecsp.domain.ro.constant.TestConstants;
import org.eclipse.ecsp.entities.EventData;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.ro.RoDAOMongoImpl;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.ro.processor.ROStreamProcessor;
import org.eclipse.ecsp.ro.processor.RemoteInhibitDataHandler;
import org.eclipse.ecsp.ro.utils.CacheUtil;
import org.eclipse.ecsp.services.constants.VehicleProfileAttribute;
import org.eclipse.ecsp.services.utils.ServiceUtil;
import org.eclipse.ecsp.services.utils.SettingsManagerClient;
import org.eclipse.ecsp.services.utils.VehicleProfileClient;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import scala.util.control.Exception;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * test class for RemoteInhibit.
 */
@SpringBootTest(classes = org.eclipse.ecsp.Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = {"classpath:/application-test.properties", "classpath:/application-base.properties"})
public class RemoteInhibitUnitTest extends CommonTestBase {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(RemoteInhibitUnitTest.class);

    @Mock
    Properties props;

    String key = "Device123";

    @Value("${source.topic.name}")
    private String[] sourceTopics;

    @Value("${sink.topic.name}")
    private String[] sinkTopics;

    private static String sourceTopicName;

    private static String sinkTopicName;

    @Autowired
    private RoDAOMongoImpl riDAO;

    @Autowired
    private ROStreamProcessor sp;

    @Autowired
    private RemoteInhibitDataHandler riEventHandler;

    @Mock
    StreamProcessingContext spc;

    private IgniteEventImpl eventImpl;

    private IgniteStringKey igniteKey;

    private Ro remoteInhibit;

    @MockBean
    VehicleProfileClient vehicleProfileClient;

    @Autowired
    private CacheUtil cacheUtil;

    @Mock
    SettingsManagerClient settingsManagerClient;

    @MockBean
    private ServiceUtil serviceUtil;

    /**
     * setup().
     *
     * @throws Exception Exception
     */
    @Before
    public void setup() {
        Field f1 = null;
        try {
            f1 = riEventHandler.getClass().getDeclaredField("vehicleProfileClient");

            f1.setAccessible(true);
            f1.set(riEventHandler, vehicleProfileClient);
            Optional<String> user = Optional.of("User123");
            Mockito.when(vehicleProfileClient.getVehicleProfileAttribute("vehicleId",
                    VehicleProfileAttribute.USERID)).thenReturn(user);
            Mockito.when(vehicleProfileClient.getVehicleProfileAttribute(key,
                    VehicleProfileAttribute.VIN)).thenReturn(user);

            Field f2 = riEventHandler.getClass().getDeclaredField("settingsManagerClient");
            f2.setAccessible(true);
            f2.set(riEventHandler, settingsManagerClient);

            Map<String, Object> response = new HashMap<>();
            String configObject = IOUtils
                    .toString(RemoteInhibitUnitTest.class.getResourceAsStream("/callCenterInfo.json"), "UTF-8");
            response = new ObjectMapper().readValue(configObject, HashMap.class);

            Mockito.when(settingsManagerClient.getSettingsManagerConfigurationObject(RemoteInhibitDataHandler
                                    .UNKNOWN, "vehicleId",
                            RemoteInhibitDataHandler.ASSIGN_CALL_CENTER,
                            "settings[0].Data.configurationObject"))
                    .thenReturn(response);

            sourceTopicName = sourceTopics[0];
            sinkTopicName = sinkTopics[0];
            MockitoAnnotations.initMocks(this);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testRemoteInhibitRequest() {
        RemoteInhibitRequestV1_1 riRequestData = new RemoteInhibitRequestV1_1();
        riRequestData.setCrankInhibit(CrankInhibit.INHIBIT);
        riRequestData.setOrigin("SVL"); // SVL,
        // FleetRemoteInhibitOwner
        riRequestData.setUserId("pkumar16");

        riDAO.deleteAll();

        eventImpl = getIgniteEvent(riRequestData, Constants.EVENT_ID_REMOTE_INHIBIT_REQUEST);
        eventImpl.setMessageId("messageId");
        eventImpl.setCorrelationId("correlationId");
        eventImpl.setBizTransactionId("sessionId");
        eventImpl.setRequestId("123456");
        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());

        sp.process(kafkaRecord);

        List<Ro> riList = riDAO.findAll();
        if (!riList.isEmpty() && !riList.isEmpty()) {
            remoteInhibit = riList.get(0);
        }
        RemoteInhibitRequestV1_1 eventData = (RemoteInhibitRequestV1_1) remoteInhibit.getRoEvent().getEventData();
        LOGGER.debug("Number of entries found on EmbedMongo are ::::  " + riList.size());
        Assert.assertFalse(riList.isEmpty());
        Assert.assertEquals("SVL", eventData.getOrigin());
        Assert.assertEquals("INHIBIT", eventData.getCrankInhibit().toString());

        riDAO.deleteAll();

        riRequestData.setCrankInhibit(CrankInhibit.END_INHIBIT);
        riRequestData.setOrigin("FleetRemoteInhibitOwner"); // SVL,
        // FleetRemoteInhibitOwner
        riRequestData.setUserId("pkumar16");

        eventImpl = getIgniteEvent(riRequestData, Constants.EVENT_ID_REMOTE_INHIBIT_REQUEST);
        eventImpl.setMessageId("messageId");
        eventImpl.setCorrelationId("correlationId");
        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);

        Record<IgniteKey<?>, IgniteEvent> kafkaRecord2 = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());

        sp.process(kafkaRecord2);
        List<Ro> riList2 = riDAO.findAll();
        if (!riList2.isEmpty() && riList2.size() > 0) {
            remoteInhibit = riList2.get(0);
        }

        RemoteInhibitRequestV1_1 eventData2 = (RemoteInhibitRequestV1_1) remoteInhibit.getRoEvent().getEventData();
        LOGGER.debug("Number of entries found on EmbedMongo are ::::  " + riList2.size());
        Assert.assertFalse(riList2.isEmpty());
        Assert.assertEquals("END_INHIBIT", eventData2.getCrankInhibit().toString());
        Assert.assertEquals("FleetRemoteInhibitOwner", eventData2.getOrigin().toString());

        riDAO.deleteAll();
    }

    @Test
    public void testRemoteInhibitResponse() {
        insertTestEventData();

        RemoteInhibitResponseV1_1 riResponse = new RemoteInhibitResponseV1_1();
        riResponse.setResponse(Response.SUCCESS);
        riResponse.setUserId("pkumar16");
        riResponse.setRoRequestId("rorequestId");
        riResponse.setOrigin("SVL");

        eventImpl = getIgniteEvent(riResponse, Constants.EVENT_ID_REMOTE_INHIBIT_RESPONSE);
        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);

        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());

        sp.process(kafkaRecord);

        List<Ro> riList = riDAO.findAll();

        if (!riList.isEmpty() && riList.size() > 0) {
            remoteInhibit = riList.get(0);
        }

        RemoteInhibitResponseV1_1 eventData = (RemoteInhibitResponseV1_1) remoteInhibit
                .getRoResponseList().get(0).getEventData();
        LOGGER.debug("Number of entries found on EmbedMongo are ::::  " + riList.size());
        Assert.assertFalse(riList.isEmpty());
        Assert.assertEquals("SVL", eventData.getOrigin());
        Assert.assertEquals("QUALIFIER_RIRESPONSE_CALLCENTER_1", remoteInhibit
                .getRoResponseList().get(0).getDFFQualifier());
        Assert.assertEquals("SUCCESS", eventData.getResponse().toString());

    }

    @Test
    public void testRemoteInhibitResponseWithCache() {
        insertTestEventData();
        RemoteInhibitResponseV1_1 riResponse = new RemoteInhibitResponseV1_1();
        riResponse.setResponse(Response.SUCCESS);
        riResponse.setUserId("pkumar16");
        riResponse.setRoRequestId("rorequestId");
        riResponse.setOrigin("SVL");

        eventImpl = getIgniteEvent(riResponse, Constants.EVENT_ID_REMOTE_INHIBIT_RESPONSE);
        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);

        // Insert RIRequest fields in to the cache
        IgniteEventImpl riRequestimpl = createRIEvents(Constants.EVENT_ID_REMOTE_INHIBIT_REQUEST);
        cacheUtil.getCache(Constants.RO_CACHE_NAME).put(riEventHandler.buildElementForCache(riRequestimpl));

        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());

        sp.process(kafkaRecord);
        //        sp.process(igniteKey, eventImpl);

        List<Ro> riList = riDAO.findAll();

        if (!riList.isEmpty() && riList.size() > 0) {
            remoteInhibit = riList.get(0);
        }

        RemoteInhibitResponseV1_1 eventData = (RemoteInhibitResponseV1_1) remoteInhibit
                .getRoResponseList().get(0).getEventData();
        LOGGER.debug("Number of entries found on EmbedMongo are ::::  " + riList.size());
        Assert.assertFalse(riList.isEmpty());
        Assert.assertEquals("SVL", eventData.getOrigin());
        Assert.assertEquals("SUCCESS", eventData.getResponse().toString());

    }

    @Test
    public void testCrankNotificationData() {
        AuthorizedPartnerDetail authorizedPartnerDetail = new AuthorizedPartnerDetail();
        List<AuthorizedPartnerDetailItem> outboundDetails = new ArrayList();
        AuthorizedPartnerDetailItem authorizedPartnerDetailItem =
                new AuthorizedPartnerDetailItem("PARTNER_ID", "QUALIFIER_CRANKNOTIFICATIONDATA_PARTNER_ID");
        outboundDetails.add(authorizedPartnerDetailItem);
        authorizedPartnerDetail.setChannelOutboundRequired(true);
        authorizedPartnerDetail.setSoldRegion("EU");
        Mockito.when(serviceUtil.getAuthorizedPartnerDetail(Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString()))
                .thenReturn(authorizedPartnerDetail);
        insertTestEventData();
        CrankNotificationDataV1_0 crankNotiData = new CrankNotificationDataV1_0();
        crankNotiData.setRoRequestId("requestId");
        crankNotiData.setOrigin("SVL");

        crankNotiData.setCrankAttempted(true);
        crankNotiData.setLatitude(TestConstants.LATITUDE);
        crankNotiData.setLongitude(TestConstants.LONGITUDE);
        crankNotiData.setHorPosError(TestConstants.SEVEN);
        crankNotiData.setBearing(TestConstants.DOUBLE_2);
        crankNotiData.setAltitude(TestConstants.DOUBLE_12_45);

        eventImpl = getIgniteEvent(crankNotiData, Constants.EVENT_ID_CRANK_NOTIFICATION_DATA);
        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);

        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());

        sp.process(kafkaRecord);

        List<Ro> riList = riDAO.findAll();

        if (!riList.isEmpty() && riList.size() > 0) {
            remoteInhibit = riList.get(0);
        }

        CrankNotificationDataV1_0 eventData = (CrankNotificationDataV1_0)
                remoteInhibit.getRoResponseList().get(0).getEventData();
        LOGGER.debug("Number of entries found on EmbedMongo are ::::  " + riList.size());
        Assert.assertFalse(riList.isEmpty());
        Assert.assertEquals("SVL", eventData.getOrigin());
        Assert.assertEquals("12.45", eventData.getAltitude().toString());
        Assert.assertEquals("23.45", eventData.getLatitude().toString());
        Assert.assertEquals("45.67", eventData.getLongitude().toString());
        Assert.assertEquals("requestId", eventData.getRoRequestId().toString());
        Assert.assertEquals("2.0", eventData.getBearing().toString());
    }

    @Test
    public void testCrankNotificationDataToAuthorizedPartner() {
        AuthorizedPartnerDetail authorizedPartnerDetail = new AuthorizedPartnerDetail();
        List<AuthorizedPartnerDetailItem> outboundDetails = new ArrayList();
        AuthorizedPartnerDetailItem authorizedPartnerDetailItem =
                new AuthorizedPartnerDetailItem("PARTNER_ID", "QUALIFIER_CRANKNOTIFICATIONDATA_PARTNER_ID");
        outboundDetails.add(authorizedPartnerDetailItem);
        authorizedPartnerDetail.setOutboundDetails(outboundDetails);
        authorizedPartnerDetail.setChannelOutboundRequired(true);
        authorizedPartnerDetail.setSoldRegion("EU");
        Mockito.when(serviceUtil.getAuthorizedPartnerDetail(Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString()))
                .thenReturn(authorizedPartnerDetail);
        insertTestEventData();
        CrankNotificationDataV1_0 crankNotiData = new CrankNotificationDataV1_0();
        crankNotiData.setRoRequestId("requestId");
        crankNotiData.setOrigin("SVL");

        crankNotiData.setCrankAttempted(true);
        crankNotiData.setLatitude(TestConstants.DOUBLE_23_45);
        crankNotiData.setLongitude(TestConstants.DOUBLE_45_67);
        crankNotiData.setHorPosError(TestConstants.SEVEN);
        crankNotiData.setBearing(TestConstants.DOUBLE_2);
        crankNotiData.setAltitude(TestConstants.ALTITUDE);

        eventImpl = getIgniteEvent(crankNotiData, Constants.EVENT_ID_CRANK_NOTIFICATION_DATA);
        igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");

        sp.init(spc);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, eventImpl, System.currentTimeMillis());

        sp.process(kafkaRecord);

        List<Ro> riList = riDAO.findAll();

        if (!riList.isEmpty() && riList.size() > 0) {
            remoteInhibit = riList.get(0);
        }

        CrankNotificationDataV1_0 eventData = (CrankNotificationDataV1_0) remoteInhibit
                .getRoResponseList().get(0).getEventData();
        LOGGER.debug("Number of entries found on EmbedMongo are ::::  " + riList.size());
        Assert.assertFalse(riList.isEmpty());
        Assert.assertEquals("SVL", eventData.getOrigin());
        Assert.assertEquals("12.45", eventData.getAltitude().toString());
        Assert.assertEquals("23.45", eventData.getLatitude().toString());
        Assert.assertEquals("45.67", eventData.getLongitude().toString());
        Assert.assertEquals("requestId", eventData.getRoRequestId().toString());
        Assert.assertEquals("2.0", eventData.getBearing().toString());
    }

    @Test
    public void setQualifierTest() {
        eventImpl = new IgniteEventImpl();
        eventImpl.setEventId("eventId");
        eventImpl.setSchemaVersion(Version.V1_0);
        eventImpl.setDeviceDeliveryCutoff(TestConstants.MINUS_ONE_LONG);
        eventImpl.setVehicleId("vin");
        eventImpl.setMessageId("messageId");

        IgniteKey igniteKey = new IgniteStringKey("deleteKey");
        Assert.assertNotNull(riEventHandler.setQualifier(igniteKey, eventImpl, "origin"));
    }

    @Test
    public void setQualifierTest2() {
        eventImpl = new IgniteEventImpl();
        eventImpl.setEventId("eventId");
        eventImpl.setSchemaVersion(Version.V1_0);
        eventImpl.setDeviceDeliveryCutoff(-TestConstants.MINUS_ONE_LONG);
        eventImpl.setVehicleId("vin");
        eventImpl.setMessageId("messageId");
        IgniteKey igniteKey = new IgniteStringKey("deleteKey");
        Assert.assertNull(riEventHandler.setQualifier(igniteKey, eventImpl, "SVL"));
    }

    private void insertTestEventData() {
        IgniteEventImpl riRequestimpl = createRIEvents(Constants.EVENT_ID_REMOTE_INHIBIT_REQUEST);

        insertRIEventInMongo(riRequestimpl);

    }

    private void insertRIEventInMongo(IgniteEventImpl value) {

        Ro riEntity = new Ro();
        riEntity.setSchemaVersion(value.getSchemaVersion());
        riEntity.setRoEvent(value);
        LOGGER.debug("persisting to mongo ,entity :{}", riEntity);
        riDAO.save(riEntity);
        LOGGER.debug("Number of entries inserted into MongoDB ,entity :{}", riEntity);
    }

    private IgniteEventImpl createRIEvents(String eventType) {
        IgniteEventImpl eventImpl = new IgniteEventImpl();

        switch (eventType) {

            case Constants.EVENT_ID_REMOTE_INHIBIT_REQUEST: {
                RemoteInhibitRequestV1_1 riRequestData = new RemoteInhibitRequestV1_1();
                riRequestData.setCrankInhibit(CrankInhibit.INHIBIT);
                riRequestData.setOrigin("SVL");
                riRequestData.setRoRequestId("rorequestId");
                riRequestData.setUserId("pkumar16");

                eventImpl = new IgniteEventImpl();
                eventImpl.setBizTransactionId("bizTransactionId");
                eventImpl.setEventData(riRequestData);
                eventImpl.setEventId(Constants.EVENT_ID_REMOTE_INHIBIT_REQUEST);
                eventImpl.setSchemaVersion(Version.V1_1);
                eventImpl.setMessageId("123456");
                eventImpl.setRequestId("rorequestId");
                eventImpl.setTimestamp(System.currentTimeMillis());
                eventImpl.setTimezone((short) TestConstants.THIRTY);
                eventImpl.setVehicleId("vehicleId");

                break;
            }
            case Constants.EVENT_ID_REMOTE_INHIBIT_RESPONSE: {
                RemoteInhibitResponseV1_1 riResponseData = new RemoteInhibitResponseV1_1();
                riResponseData.setResponse(Response.SUCCESS);
                riResponseData.setUserId("pkumar16");
                riResponseData.setRoRequestId("requestId");
                riResponseData.setOrigin("SVL");

                eventImpl = getIgniteEvent(riResponseData);

                break;
            }

            case Constants.EVENT_ID_CRANK_NOTIFICATION_DATA: {
                CrankNotificationDataV1_0 crankNotiData = new CrankNotificationDataV1_0();
                crankNotiData.setRoRequestId("requestId");
                crankNotiData.setUserId("pkumar16");
                crankNotiData.setRoRequestId("requestId");

                crankNotiData.setCrankAttempted(true);
                crankNotiData.setLatitude(TestConstants.DOUBLE_23_45);
                crankNotiData.setLongitude(TestConstants.DOUBLE_45_67);
                crankNotiData.setHorPosError(TestConstants.SEVEN);
                crankNotiData.setBearing(TestConstants.DOUBLE_2);
                crankNotiData.setAltitude(TestConstants.ALTITUDE);

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

    @NotNull
    private static IgniteEventImpl getIgniteEvent(RemoteInhibitResponseV1_1 riResponseData) {
        IgniteEventImpl eventImpl;
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
        return eventImpl;
    }

    private IgniteEventImpl getIgniteEvent(EventData eventData, String eventId) {
        eventImpl = new IgniteEventImpl();
        eventImpl.setBizTransactionId("bizTransactionId");
        eventImpl.setEventData(eventData);
        eventImpl.setEventId(eventId);
        eventImpl.setSchemaVersion(Version.V1_1);
        eventImpl.setRequestId("123456");
        eventImpl.setTimestamp(System.currentTimeMillis());
        eventImpl.setTimezone((short) TestConstants.THIRTY);
        eventImpl.setVehicleId("vehicleId");
        eventImpl.setCorrelationId("123456");

        return eventImpl;
    }

    /**
     * cleanUp().
     */
    @After
    public void cleanUp() {
        riDAO.deleteAll();
        cacheUtil.getCache(Constants.RO_CACHE_NAME).removeAll();
    }
}
