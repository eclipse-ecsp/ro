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

package org.eclipse.ecsp.ro.processor.strategy.impl;

import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.ro.AbstractRoEventData;
import org.eclipse.ecsp.entities.AbstractIgniteEvent;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.ro.processor.ScheduleEventDataHandler;
import org.eclipse.ecsp.ro.queue.QueueHandler;
import org.eclipse.ecsp.services.utils.ServiceUtil;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RoRequestStreamProcessor.
 */
@Component(Constants.REMOTE_OPERATION_REQUEST_EVENT_ID)
public class RoRequestStreamProcessor extends AbstractStreamProcessor {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(RoRequestStreamProcessor.class);

    @Autowired
    protected ServiceUtil serviceUtil;

    @Value("${dma.service.max.retry}")
    private long retryCount;

    @Value("${dma.service.retry.interval.millis}")
    private long retryInterval;

    @Value("${dma.service.ttl.buffer}")
    private long ttlBuffer;

    @Autowired
    private ScheduleEventDataHandler scheduler;

    @Autowired
    @Qualifier("requestQueueHandler")
    private QueueHandler queueHandler;

    @Override
    public void process(
            Record<IgniteKey<?>, IgniteEvent> kafkaRecordIn, StreamProcessingContext ctxt) {
        IgniteEvent value = kafkaRecordIn.value();

        LOGGER.debug(value, "Processing RemoteOperationRequest event id: {}", value.getEventId());

        AbstractIgniteEvent event = (AbstractIgniteEvent) value;

        // Generate Unique message ID
        event.setMessageId(globalMessageIdGenerator.generateUniqueMsgId(event.getVehicleId()));

        event.setBizTransactionId(value.getBizTransactionId());
        event.setResponseExpected(true);
        event.setDeviceRoutable(true);
        event.setShoulderTapEnabled(true);
        AbstractRoEventData roEvent = (AbstractRoEventData) value.getEventData();

        IgniteKey<?> key = kafkaRecordIn.key();

        if (null == roEvent.getSchedule()) {
            LOGGER.debug("Processing roEvent(eventId={}) without schedule", value.getEventId());

            // Upon receiving the event, persist PENDING as the initial state
            // with other event data in the database and cache.
            persistEntity(event);

            // if queue-handlers are enabled for custom checks,
            // then forward the event to the queue handler
            // which in turn will forward the event then to kafka
            if (roQueueEnable) {
                queueHandler.process(key, value, ctxt);
            } else {
                // queue-handlers are disabled, forward the event to kafka
                // for next processor in the stream.
                Record<IgniteKey<?>, IgniteEvent> kafkaRecord =
                        new Record<>(key, value, System.currentTimeMillis());
                ctxt.forward(kafkaRecord);
            }
        } else {

            LOGGER.info(
                    "### Process Scheduling events for event id: {}, data: {}",
                    value.getEventId(),
                    value.getEventData());

            IgniteEventImpl schedulerEvent =
                    scheduler.createSchedulerEvent(
                            key, value, event, sourceTopics, serviceName, serviceName, Integer.MAX_VALUE);

            Record<IgniteKey<?>, IgniteEvent> kafkaRecord =
                    new Record<>(key, schedulerEvent, System.currentTimeMillis());

            ctxt.forward(kafkaRecord);
        }
    }
}
