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
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.RemoteOperationScheduleV1;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.ro.RoScheduleDAOMongoImpl;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.ro.processor.ScheduleEventDataHandler;
import org.eclipse.ecsp.ro.processor.strategy.impl.AbstractStreamProcessor;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Stream Processor for RemoteOperationDeleteSchedule event.
 */
@Component(Constants.EVENT_ID_DELETE_SCHEDULE)
public class RODeleteScheduleProcessor extends AbstractStreamProcessor {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(RODeleteScheduleProcessor.class);

    @Autowired
    private ScheduleEventDataHandler scheduler;

    @Autowired
    private RoScheduleDAOMongoImpl roScheduleDAOMongoImpl;

    /**
     * Process RemoteOperationDeleteSchedule event.
     *
     * @param kafkaRecordIn kafka record
     * @param ctxt          stream processing context
     */
    @Override
    public void process(
            Record<IgniteKey<?>, IgniteEvent> kafkaRecordIn, StreamProcessingContext ctxt) {
        IgniteKey<?> key = kafkaRecordIn.key();
        IgniteEvent value = kafkaRecordIn.value();

        LOGGER.info(
                "### Process Delete Schedule events for event id: {}, data: {}",
                value.getEventId(),
                value.getEventData());

        IgniteEventImpl schedulerEvent;

        RemoteOperationScheduleV1 event = (RemoteOperationScheduleV1) value.getEventData();

        String scheduleId =
                roScheduleDAOMongoImpl.getScheduleId(
                        value.getVehicleId(),
                        event.getSchedulerKey(),
                        org.eclipse.ecsp.domain.ro.constant.Constants.REMOTEOPERATIONENGINE);

        schedulerEvent =
                scheduler.deleteSchedulerEvent(
                        key,
                        value,
                        sourceTopics,
                        serviceName,
                        event.getRoRequestId(),
                        scheduleId,
                        Version.V1_0);

        Record<? extends IgniteKey<?>, IgniteEventImpl> kafkaRecord =
                new Record<>(key, schedulerEvent, System.currentTimeMillis());

        ctxt.forward(kafkaRecord);
    }
}
