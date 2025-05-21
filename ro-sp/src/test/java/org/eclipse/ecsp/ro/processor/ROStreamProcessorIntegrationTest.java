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

package org.eclipse.ecsp.ro.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.prometheus.client.CollectorRegistry;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.eclipse.ecsp.analytics.stream.base.Launcher;
import org.eclipse.ecsp.analytics.stream.base.PropertyNames;
import org.eclipse.ecsp.analytics.stream.base.discovery.PropBasedDiscoveryServiceImpl;
import org.eclipse.ecsp.analytics.stream.base.utils.KafkaTestUtils;
import org.eclipse.ecsp.domain.EventID;
import org.eclipse.ecsp.domain.ro.Ro;
import org.eclipse.ecsp.domain.ro.constant.TestConstants;
import org.eclipse.ecsp.ro.RoDAOMongoImpl;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.ro.kafka.producer.ROMessageGenerator;
import org.eclipse.ecsp.ro.utils.CacheUtil;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.eclipse.ecsp.transform.GenericIgniteEventTransformer;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * test class for ROStreamProcessorIntegration.
 */
@ComponentScan(basePackages = {"com.eclipse.ecsp", "org.eclipse.ecsp.serializer.IngestionSerializerFstImpl"})
@SpringBootTest(classes = org.eclipse.ecsp.Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = {"classpath:/application-test.properties", "classpath:/application-base.properties"})
public class ROStreamProcessorIntegrationTest extends CommonTestBase {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ROStreamProcessorIntegrationTest.class);

    private static int i = 0; // will be used to have unique source and sink

    private static String dffTopic = "https-integ-high";

    String key = "Device123";

    // topic to each test case
    @Value("${source.topic.name}")
    private String[] sourceTopicName;

    @Value("${sink.topic.name}")
    private String[] sinkTopicName;

    @Autowired
    private RoDAOMongoImpl roDAOMongoImpl;

    @Autowired
    private GenericIgniteEventTransformer transformer;

    @Value("${service.name}")
    private String serviceName;

    @Autowired
    private CacheUtil cacheUtil;

    @Value("${launcher.impl.class.fqn:org.ecsp.analytics.stream.base.LauncherImpl}")
    String launcherClass;

    @Autowired
    protected ApplicationContext ctx;
    private Launcher launcher;

    protected void launchApplication() throws Exception {
        LOGGER.info("Launching Application");
        this.launcher = (Launcher) this.ctx.getBean(Launcher.class);
        this.launcher.setExecuteShutdownHook(false);
        this.launcher.launch();
    }

    /**
     * Method to set the values of Kafka Broker.
     */
    @Before
    public void setup() {
        super.setup();
        CollectorRegistry.defaultRegistry.clear();
        createTopics(sourceTopicName[i], sinkTopicName[i]);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "tc-consumer");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                Serdes.String().deserializer().getClass().getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                Serdes.String().deserializer().getClass().getName());

        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                Serdes.ByteArray().serializer().getClass().getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                Serdes.ByteArray().serializer().getClass().getName());

        consumerProps.put(PropertyNames.DISCOVERY_SERVICE_IMPL, PropBasedDiscoveryServiceImpl.class.getName());
        consumerProps.put(PropertyNames.SOURCE_TOPIC_NAME, sourceTopicName);
        producerProps.put("sink.topic.name", sinkTopicName);
        consumerProps.put(PropertyNames.APPLICATION_ID, "test-sp-123");

    }

    @Test
    public void testScheduleOPSACK() throws Exception {

        consumerProps.put(PropertyNames.APPLICATION_ID, "test-sp" + System.currentTimeMillis());

        //launchApplication();

        // Using Message Generator send the data to the Kafka.
        String[] args = new String[TestConstants.FOUR];
        args[0] = sourceTopicName[0];
        args[1] = key;
        String roEventData = IOUtils
                .toString(ROStreamProcessorIntegrationTest.class
                        .getResourceAsStream("/roScheduleStatus.json"), "UTF-8");
        args[TestConstants.TWO] = roEventData;
        args[TestConstants.THREE] = KAFKA_CLUSTER.getKafkaBrokerList();
        ROMessageGenerator.produce(args);
        Thread.sleep(TestConstants.INT_5000);

        List<String[]> messages = KafkaTestUtils.getMessages(sourceTopicName[i],
                consumerProps, 1, TestConstants.INT_15000);
        Assert.assertEquals(key, messages.get(0)[0]);

    }

    @Test
    public void firstTestROStreamProcessorAlarm() throws Exception {
        try {
            testGenericStreamProcessor("/remoteOperationAlarmRequest.json", "/remoteOperationAlarmResponse.json");
        } catch (IOException | ExecutionException | InterruptedException | TimeoutException e) {
            LOGGER.error("Error in firstTestROStreamProcessorAlarm: {}", e.getMessage());
        }

    }

    @Test
    public void firstTestROStreamProcessorRemoteOperationWithoutCorrelationId() throws Exception {

        try {
            testGenericStreamProcessorForAck("/remoteOperationWithoutCorrelationId.json", true);
        } catch (IOException | ExecutionException | InterruptedException | TimeoutException e) {
            LOGGER.error("Error in firstTestROStreamProcessorRemoteOperationWithoutCorrelationId: {}", e.getMessage());
        }

    }

    /**
     * testGenericStreamProcessor().
     *
     * @param request  request
     * @param response response
     * @throws Exception Exception
     */
    public void testGenericStreamProcessor(String request, String response)
            throws Exception {


        consumerProps.put(PropertyNames.SERVICE_NAME, ROStreamProcessor.class.getName());
        consumerProps.put(PropertyNames.APPLICATION_ID, "test-sp" + System.currentTimeMillis());

        launchApplication();

        // Using Message Generator send the data to the Kafka.
        String[] args = new String[TestConstants.FOUR];
        args[0] = sourceTopicName[i];
        args[1] = key;
        String roEventData = IOUtils.toString(ROStreamProcessorIntegrationTest
                .class.getResourceAsStream(request), "UTF-8");
        args[TestConstants.TWO] = roEventData;
        args[TestConstants.THREE] = KAFKA_CLUSTER.bootstrapServers();
        ROMessageGenerator.produce(args);

        Thread.sleep(TestConstants.THOUSAND_LONG);

        List<String[]> messages = KafkaTestUtils.getMessages(sourceTopicName[i],
                consumerProps, 1, TestConstants.INT_15000);
        Assert.assertEquals(key, messages.get(0)[0]);
        Assert.assertTrue(messages.get(0)[1].replaceAll("\\s", "").contains(roEventData.replaceAll("\\s", "")));

    }

    /**
     * testGenericStreamProcessorForAck().
     *
     * @param request              request
     * @param withoutCorrelationID withoutCorrelationID
     * @throws Exception Exception
     */
    public void testGenericStreamProcessorForAck(String request, Boolean withoutCorrelationID)
            throws Exception {

        consumerProps.put(PropertyNames.SERVICE_NAME, ROStreamProcessor.class.getName());
        consumerProps.put(PropertyNames.APPLICATION_ID, "test-sp" + System.currentTimeMillis());

        //launchApplication();

        // Using Message Generator send the data to the Kafka.
        String[] args = new String[TestConstants.FOUR];
        args[0] = sourceTopicName[i];
        args[1] = key;
        String roEventData = IOUtils.toString(ROStreamProcessorIntegrationTest.class
                .getResourceAsStream(request), "UTF-8");
        args[TestConstants.TWO] = roEventData;
        args[TestConstants.THREE] = KAFKA_CLUSTER.bootstrapServers();
        ROMessageGenerator.produce(args);

        Thread.sleep(TestConstants.TEN_THOUSAND);

        List<String[]> messages = null;

        if (withoutCorrelationID) {
            messages = KafkaTestUtils.getMessages(sinkTopicName[i], consumerProps, 1, TestConstants.INT_1500);

        } else {
            messages = KafkaTestUtils.getMessages(sinkTopicName[i + 1], consumerProps, 1, TestConstants.INT_1500);
            if (messages.size() == 0) {
                Assert.assertEquals(0, messages.size());
            }
        }

        if (messages.size() > 0) {
            Assert.assertEquals(key, messages.get(0)[0]);
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
            };
            HashMap<String, Object> mapResponse = objectMapper.readValue(messages.get(0)[1], typeRef);
            if (withoutCorrelationID) {
                Assert.assertEquals(mapResponse.get("EventID"), EventID.ACKNOWLEDGEMENT.toString());
            } else {
                Assert.assertEquals(Constants.REMOTE_OPERATION_RESPONSE_EVENT_ID, mapResponse.get("EventID"));
            }
        }

    }

    /* Mongo save testing */
    @Test
    public void testROMongoDBSaveAlarm() throws Exception {

        try {
            testROMongoDBSave("/remoteOperationAlarmRequest.json", true);
        } catch (IOException | ExecutionException | InterruptedException | TimeoutException e) {
            LOGGER.error("Error in testROMongoDBSaveAlarm: {}", e.getMessage());
        }

    }

    @Test
    public void testROMongoDBSaveClimate() throws Exception {
        try {
            testROMongoDBSave("/remoteOperationClimateRequest.json", true);
        } catch (IOException | ExecutionException | InterruptedException | TimeoutException e) {
            LOGGER.error("Error in testROMongoDBSaveClimate: {}", e.getMessage());
        }

    }

    String convertEventDataToJsonString(String eventData) {
        int index = eventData.indexOf('[');
        String substr = eventData.substring(index);
        substr = substr.replace("[", "{\"");
        substr = substr.replace("]", "\"}");
        substr = substr.replace("=", "\":\"");
        substr = substr.replace(",", "\",\"");
        return substr;
    }

    /**
     * testROMongoDBSave().
     *
     * @param request request
     * @param isValid isValid
     * @throws Exception Exception
     */
    public void testROMongoDBSave(String request, Boolean isValid)
            throws Exception {

        consumerProps.put(PropertyNames.SERVICE_NAME, ROStreamProcessor.class.getName());
        consumerProps.put(PropertyNames.APPLICATION_ID, "test-sp-123" + System.currentTimeMillis());

        //launchApplication();

        String[] args = new String[TestConstants.FOUR];
        args[0] = sourceTopicName[i];
        args[1] = key;
        String roEventData = IOUtils.toString(ROStreamProcessorIntegrationTest
                .class.getResourceAsStream(request), "UTF-8");
        args[TestConstants.TWO] = roEventData;
        args[TestConstants.THREE] = KAFKA_CLUSTER.getKafkaBrokerList();
        ROMessageGenerator.produce(args);

        Thread.sleep(TestConstants.INT_35000);

        List<Ro> roList = roDAOMongoImpl.findAll();

        LOGGER.debug("Number of entries found on EmbedMongo are ::::  " + roList.size());
        if (isValid && !roList.isEmpty()) {
            Assert.assertTrue(!roList.isEmpty());
            Ro roEntity = roList.get(0);
            LOGGER.debug("Schema Version of the stored entity  ::::  " + roEntity.getSchemaVersion().getValue());
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
            };
            HashMap<String, Object> mapExpectedResponse = objectMapper.readValue(roEventData, typeRef);
            Assert.assertEquals(mapExpectedResponse.get("Version"), roEntity.getSchemaVersion().getValue());
            Assert.assertEquals(mapExpectedResponse.get("EventID"), roEntity.getRoEvent().getEventId());

            HashMap<String, Object> mapDataResponse = objectMapper
                    .readValue(convertEventDataToJsonString(roEntity.getRoEvent().getEventData().toString()), typeRef);

            Assert.assertEquals(mapExpectedResponse.get("Data"), mapDataResponse);
        } else {
            if (!roList.isEmpty()) {
                Ro roEntity = roList.get(0);
                Assert.assertNull(roEntity.getRoEvent().getEventId());
                Assert.assertNull(roEntity.getRoEvent().getEventData());

            } else {
                Assert.assertTrue(roList.isEmpty());
            }
        }
    }

    /**
     * tearDown method.
     */
    @After
    public void tearDown() throws Exception {
        roDAOMongoImpl.deleteAll();
        cacheUtil.getCache(Constants.RO_CACHE_NAME).removeAll();
        CollectorRegistry.defaultRegistry.clear();
        super.teardown();
    }

}