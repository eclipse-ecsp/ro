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
import org.eclipse.ecsp.entities.AbstractEventData;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.events.scheduler.ScheduleOpStatusEventData;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.ro.processor.RCPDHandler;
import org.eclipse.ecsp.ro.processor.ScheduleEventDataHandler;
import org.eclipse.ecsp.ro.processor.strategy.impl.AbstractStreamProcessor;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Optional;

/**
 * Processor for ScheduleOpStatusEvent event.
 */
@Component(EventID.SCHEDULE_OP_STATUS_EVENT)
public class ScheduleOpStatusProcessor extends AbstractStreamProcessor {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ScheduleOpStatusProcessor.class);

    @Value("${rcpd.service.name}")
    private String rcpdServiceName;

    @Autowired
    private RCPDHandler rcpdEventHandler;

    @Autowired
    private ScheduleEventDataHandler scheduler;

    @Override
    public void process(Record<IgniteKey<?>, IgniteEvent> kafkaRecordIn, StreamProcessingContext ctxt) {
        IgniteKey<?> key = kafkaRecordIn.key();
        IgniteEvent value = kafkaRecordIn.value();

        LOGGER.info("### Process scheduler Ack event id: {}, data: {}", value.getEventId(), value.getEventData());
        try {
            ScheduleOpStatusEventData eventData = (ScheduleOpStatusEventData) value.getEventData();
            AbstractEventData createScheduleEventData = (AbstractEventData) eventData.getIgniteEvent().getEventData();
            Optional<Object> customExtension = createScheduleEventData.getCustomExtension();

            if (customExtension.isPresent()
                    && customExtension.get() instanceof String customExtVal
                    && customExtVal.equalsIgnoreCase(rcpdServiceName)) {

                rcpdEventHandler.processSchedulerAckEvent(key, value);

            } else {
                scheduler.processSchedulerAckEvent(key, value);
            }

        } catch (IOException | JSONException e) {
            LOGGER.info("### Unable to create schedule, {}", e);
        }
    }
}
