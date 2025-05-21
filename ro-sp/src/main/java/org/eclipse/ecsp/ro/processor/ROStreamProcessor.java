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

import jakarta.annotation.PostConstruct;
import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.IgniteEventStreamProcessor;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.analytics.stream.base.stores.HarmanPersistentKVStore;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.ro.processor.strategy.SubStreamProcessor;
import org.eclipse.ecsp.ro.processor.strategy.impl.SubStreamProcessorFactory;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

/**
 * RO stream processor.
 *
 * @author Neerajkumar
 */
@Component
public class ROStreamProcessor implements IgniteEventStreamProcessor {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ROStreamProcessor.class);

    private static final String MAX_NUMBER_RO_RESPONSE = "max.number.ro.response";

    private StreamProcessingContext ctxt;

    @Value("${source.topic.name}")
    private String[] sourceTopics;

    @Value("${sink.topic.name}")
    private String[] sinkTopics;

    @Value("${max.number.ro.response}")
    private int maxNumberRoResponse;

    @Autowired
    private RemoteInhibitDataHandler riEventHandler;

    @Autowired
    private RCPDHandler rcpdEventHandler;

    @Value("#{${notification.responsemessage.notificationid.mapping}}")
    private Map<String, String> notificationIdMapping;

    @Autowired
    private SubStreamProcessorFactory subStreamProcessorFactory;

    @Override
    public void init(StreamProcessingContext<IgniteKey<?>, IgniteEvent> spc) {
        LOGGER.info("Setting up RO stream processor");
        this.ctxt = spc;
        riEventHandler.init();
        rcpdEventHandler.init();
    }

    @PostConstruct
    public void postConstruct() {
    }

    public void setNotificationIdMapping(Map<String, String> notificationIdMapping) {
        this.notificationIdMapping = notificationIdMapping;
    }

    @Override
    public String name() {
        return Constants.RO_STREAM_PROCESSOR;
    }

    @Override
    public String[] sources() {
        LOGGER.debug("source topics {}", Arrays.toString(sourceTopics));
        return Arrays.copyOf(sourceTopics, sourceTopics.length);
    }

    @Override
    public String[] sinks() {
        LOGGER.debug("sink topics {}", Arrays.toString(sinkTopics));
        return Arrays.copyOf(sinkTopics, sinkTopics.length);
    }

    @SuppressWarnings({"rawtypes"})
    @Override
    public void process(Record<IgniteKey<?>, IgniteEvent> kafkaRecord) {
        if (null == kafkaRecord) {
            LOGGER.error("kafka record is null, no further processing.");
            return;
        }
        IgniteKey<?> key = kafkaRecord.key();
        IgniteEvent value = kafkaRecord.value();

        if (null == value) {
            LOGGER.error("Value is null, no further processing.");
            return;
        }

        if (null == key) {
            LOGGER.error(value, "Key is null, no further processign.");
            return;
        }

        LOGGER.info("process Key={}, Value={}", key, value);

        SubStreamProcessor subStreamProcessor = subStreamProcessorFactory.get(value.getEventId());
        if (null != subStreamProcessor) {
            subStreamProcessor.process(kafkaRecord, ctxt);
        }
    }

    @Override
    public void punctuate(long timestamp) {
    }

    @Override
    public void close() {
    }

    @Override
    public void configChanged(Properties props) {
    }

    @Override
    public void initConfig(Properties props) {
        LOGGER.debug("property :{} ,value :{}", MAX_NUMBER_RO_RESPONSE, maxNumberRoResponse);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public HarmanPersistentKVStore createStateStore() {
        return null;
    }
}
