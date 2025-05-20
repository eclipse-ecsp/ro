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

package org.eclipse.ecsp.ro.processor.rcpd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.cache.IgniteCache;
import org.eclipse.ecsp.cache.PutStringRequest;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.RCPD;
import org.eclipse.ecsp.domain.ro.RCPDRequestV1_0;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.domain.ro.dao.RCPDDAOMongoImpl;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.entities.UserContext;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.ro.processor.ROStreamProcessor;
import org.eclipse.ecsp.ro.utils.CacheUtil;
import org.eclipse.ecsp.services.utils.SettingsManagerClient;
import org.eclipse.ecsp.services.utils.VehicleProfileClient;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit Tests for RCPD.
 */
@SpringBootTest(classes = org.eclipse.ecsp.Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = {"classpath:/application-test.properties", "classpath:/application-base.properties"})
public class RCPDUnitTest extends CommonTestBase {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(RCPDUnitTest.class);

    private static String sourceTopicName;

    private static String sinkTopicName;

    private static int i = 0; // will be used to have unique source and sink

    @Autowired
    IgniteCache igniteCache;

    String key = "HUTSQGU6HS2949";

    @Autowired
    ROStreamProcessor sp;

    @Mock
    StreamProcessingContext spc;

    @Mock
    VehicleProfileClient vehicleProfileClient;

    @Mock
    SettingsManagerClient settingsManagerClient;

    @Value("${source.topic.name}")
    private String[] sourceTopics;

    @Value("${sink.topic.name}")
    private String[] sinkTopics;

    @Value("${kafka.sink.scheduler.topic}")
    private String schedulertopic;

    @Autowired
    private RCPDDAOMongoImpl rcpdDAO;

    @Autowired
    private CacheUtil cacheUtil;

    @Test
    public void testRCPDRequest() throws Exception {
        String rcpdResponseData = IOUtils.toString(RCPDUnitTest.class.getResourceAsStream("/rcpdRequest.json"),
                "UTF-8");
        IgniteEventImpl valueImpl = new IgniteEventImpl();
        RCPDRequestV1_0 eventData = new RCPDRequestV1_0();
        valueImpl.setEventId("RCPDRequest");
        valueImpl.setVehicleId("HUTSQGU6HS2949");
        eventData.setRcpdRequestId("zmvKoWxzI9g3U7VztcLzWFCNSoeXSbBii");
        eventData.setOrigin("RCPDSupportOwner");
        eventData.setUserId("TestUser");
        valueImpl.setEventData(eventData);

        rcpdDAO.deleteAll();
        sp.init(spc);
        IgniteStringKey igniteKey = new IgniteStringKey();
        igniteKey.setKey("Key");
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(igniteKey, valueImpl, System.currentTimeMillis());
        sp.process(kafkaRecord);

        List<RCPD> rcpdList = rcpdDAO.findAll();
        Assert.assertEquals(1, rcpdList.size());
        rcpdDAO.deleteAll();
    }

    private void saveRCPRequest(String vehcileId, String userId) {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setVehicleId(vehcileId);
        igniteEvent.setSchemaVersion(Version.V1_0);
        igniteEvent.setEventId(Constants.RCPDREQUEST);
        igniteEvent.setMessageId("10001");
        UserContext uc = new UserContext();
        uc.setUserId(userId);
        uc.setRole("VO");
        igniteEvent.setUserContextInfo(Arrays.asList(uc));
        RCPDRequestV1_0 data = new RCPDRequestV1_0();
        igniteEvent.setEventData(data);

        RCPD entity = new RCPD();
        entity.setSchemaVersion(Version.V1_0);
        entity.setRcpdEvent(igniteEvent);

        rcpdDAO.save(entity);

    }

    private void saveDataInRedis() {
        ObjectMapper mapper = new ObjectMapper();
        String value = "";
        Map<String, Object> mapOfVehicleStatus = new HashMap<String, Object>();
        mapOfVehicleStatus.put("vehicleStatus", "SUCCESS");
        mapOfVehicleStatus.put("userId", "user_test");
        mapOfVehicleStatus.put("offBoardStatus", "11111");
        mapOfVehicleStatus.put("timestamp", System.currentTimeMillis());

        // convert map to JSON string
        try {
            value = mapper.writeValueAsString(mapOfVehicleStatus);
            LOGGER.debug("Map value as Json string : " + value);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        igniteCache.putString(new PutStringRequest().withKey(
                Constants.getRedisKey(Constants.RCPD_SERVICE, "vin666666",
                        Constants.RCPD_STATUS)).withValue(value));

    }

    private void prepareProcessVehicleProfileChangedNotificationEventTestData() {
        saveDataInRedis();
        // entity
        saveRCPRequest("vin666666", "user_test");
        // entity2
        saveRCPRequest("vin666666", "user_test");

        // entity for another vehicle
        saveRCPRequest("vin7777777", "user_test_02");
    }
}
