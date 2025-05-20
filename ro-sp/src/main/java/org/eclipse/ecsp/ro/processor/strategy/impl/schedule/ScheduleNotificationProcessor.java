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

package org.eclipse.ecsp.ro.processor.strategy.impl.schedule;

import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.EventID;
import org.eclipse.ecsp.entities.AbstractIgniteEvent;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.events.scheduler.ScheduleNotificationEventData;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.ro.RoScheduleV2DAOMongoImpl;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.ro.processor.RCPDHandler;
import org.eclipse.ecsp.ro.processor.ScheduleEventDataHandler;
import org.eclipse.ecsp.ro.processor.strategy.impl.AbstractStreamProcessor;
import org.eclipse.ecsp.services.utils.ServiceUtil;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Processor for ScheduleNotificationEvent event.
 */
@Component(EventID.SCHEDULE_NOTIFICATION_EVENT)
public class ScheduleNotificationProcessor extends AbstractStreamProcessor {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(ScheduleNotificationProcessor.class);

    @Value("${rcpd.service.name}")
    private String rcpdServiceName;

    @Value("${service.name}")
    private String roServiceName;

    @Autowired
    private RCPDHandler rcpdEventHandler;

    @Autowired
    private ScheduleEventDataHandler scheduler;

    @Autowired
    private ServiceUtil serviceUtil;

    @Value("${dma.service.max.retry}")
    private long retryCount;

    @Value("${dma.service.retry.interval.millis}")
    private long retryInterval;

    @Value("${dma.service.ttl.buffer}")
    private long ttlBuffer;

    @Autowired
    private RoScheduleV2DAOMongoImpl roScheduleV2DAOMongoImpl;

    @Override
    public void process(
            Record<IgniteKey<?>, IgniteEvent> kafkaRecordIn, StreamProcessingContext ctxt) {
        IgniteKey<?> key = kafkaRecordIn.key();
        IgniteEvent value = kafkaRecordIn.value();

        LOGGER.info(
                "### Processing schedule notification event, event id: {}, data: {}",
                value.getEventId(),
                value.getEventData());

        ScheduleNotificationEventData scheduleNotificationEventData =
                (ScheduleNotificationEventData) value.getEventData();

        long triggerTimeMs = scheduleNotificationEventData.getTriggerTimeMs();

        // Process it as RCPD event if service name is RCPD in the event data
        try {
            JSONObject object =
                    new JSONObject(
                            new String(scheduleNotificationEventData.getPayload(), StandardCharsets.UTF_8));

            if (Objects.nonNull(object)
                    && rcpdServiceName.equalsIgnoreCase(object.optString(Constants.SERVICE_NAME))) {

                rcpdEventHandler.processScheduleNotification(key, value, ctxt);
                return;
            }
        } catch (JSONException ex) {
            LOGGER.error("JSONException :{} ", ex);
        }

        AbstractIgniteEvent event = null;

        // Else process the event as RO event
        try {
            event =
                    scheduler.processScheduledROEvent(
                            key,
                            value,
                            serviceUtil.getEventTtl(retryCount, retryInterval, ttlBuffer, triggerTimeMs));
        } catch (JSONException e) {
            LOGGER.error("Exception in processing RO schedule notification event: {} ", e);
        }

        // If it was an RO event, persist it and forward it to the next processor
        if (null != event) {
            LOGGER.info("### Scheduler RO event {}", event);
            persistEntity(event);

            Record<IgniteKey<?>, IgniteEvent> kafkaRecord =
                    new Record<>(key, event, System.currentTimeMillis());

            ctxt.forward(kafkaRecord);
        } else {
            LOGGER.info("### Unable to process null scheduler event");
        }
    }
}
