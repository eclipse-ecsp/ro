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

package org.eclipse.ecsp.ro.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.eclipse.ecsp.domain.BlobDataV1_0;
import org.eclipse.ecsp.domain.IgniteEventSource;
import org.eclipse.ecsp.entities.IgniteBlobEvent;
import org.eclipse.ecsp.serializer.IngestionSerializerFstImpl;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import static org.eclipse.ecsp.domain.ro.constant.TestConstants.FIVE;
import static org.eclipse.ecsp.domain.ro.constant.TestConstants.FOUR;
import static org.eclipse.ecsp.domain.ro.constant.TestConstants.SIXTY;
import static org.eclipse.ecsp.domain.ro.constant.TestConstants.THREE;
import static org.eclipse.ecsp.domain.ro.constant.TestConstants.TWO;


/**
 * Test Utility class for generating RO messages.
 */
public class ROMessageGenerator {

    private ROMessageGenerator() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ROMessageGenerator.class);

    private static ScheduledExecutorService exec = null;

    /**
     * Usage of the class.
     */
    public static void usage() {
        LOGGER.debug("""
                Requires 4 arguments.
                1) Kafka topic name\s
                2) Key\s
                3) Value\s
                4) bootstrap server""");
    }

    /**
     * Produce the message to the Kafka topic.
     *
     * @param args - arguments provided from command line while running the Generator.
     * @throws ExecutionException   - Exception while executing the task.
     * @throws InterruptedException - Exception when task interrupted.
     */
    public static void produce(String[] args) throws ExecutionException, InterruptedException {
        String topicName = args[0];
        String key = args[1];
        String value = args[TWO];
        String bootstrapServers = args[THREE];

        LOGGER.debug("Sending key={}, value={} to the topic {}", key, value, topicName);

        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.ACKS_CONFIG, "all");
        producerProps.put(ProducerConfig.RETRIES_CONFIG, 0);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                Serdes.ByteArray().serializer().getClass().getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                Serdes.ByteArray().serializer().getClass().getName());

        IgniteBlobEvent igniteBlobData = new IgniteBlobEvent();
        igniteBlobData.setSourceDeviceId(key);
        BlobDataV1_0 blobDataV1 = new BlobDataV1_0();
        blobDataV1.setEventSource(IgniteEventSource.IGNITE);
        blobDataV1.setPayload(value.getBytes());
        igniteBlobData.setEventData(blobDataV1);
        igniteBlobData.setVersion(org.eclipse.ecsp.domain.Version.V1_0);
        igniteBlobData.setRequestId(key + "-id");
        byte[] serializedBytes = new IngestionSerializerFstImpl().serialize(igniteBlobData);

        try (Producer<byte[], byte[]> producer = new KafkaProducer<>(producerProps)) {
            ProducerRecord<byte[], byte[]> data = new ProducerRecord<>(
                    topicName, (key).getBytes(), serializedBytes);

            producer.send(data);
            LOGGER.debug("data:" + data);
        }
    }

    /**
     * Main method to run the message generator.
     *
     * @param args - arguments provided from command line while running the Generator.
     * @throws ExecutionException   - Exception while executing the task.
     * @throws InterruptedException - Exception when task interrupted.
     */
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        exec = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            String name = Thread.currentThread().getName();
            t.setName("pool:" + name);
            t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    LOGGER.error("Uncaught exception for pool thread " + t.getName(), e);
                }
            });
            return t;
        });

        // Push data to every 5 seconds
        exec.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    produce(args);
                } catch (Exception e) {
                    LOGGER.error("Exception in punctuateData", e);
                }
            }
        }, SIXTY, FIVE, TimeUnit.SECONDS);

        int argsLength = args.length;

        if (argsLength != FOUR) {
            LOGGER.error("Expecting 4 arguments, but received {}", argsLength);
            usage();
            return;
        }
        produce(args);

    }

}