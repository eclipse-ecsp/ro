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
import org.eclipse.ecsp.domain.EventID;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.ro.processor.RCPDHandler;
import org.eclipse.ecsp.ro.processor.ScheduleEventDataHandler;
import org.eclipse.ecsp.services.utils.VehicleProfileChangedNotificationEventUtil;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Event Processor for VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT event.
 */
@Component(EventID.VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT)
public class VPChangedNotificationProcessor extends AbstractStreamProcessor {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(VPChangedNotificationProcessor.class);

    private static final String RO_REQUEST_VEHICLE_ID = "roEvent.vehicleId";

    private static final String RO_REQUEST_USER_ID = "roEvent.userContextInfo.userId";

    @Autowired
    private VehicleProfileChangedNotificationEventUtil vehicleProfileChangedNotificationEventUtil;

    @Autowired
    private RCPDHandler rcpdEventHandler;

    @Autowired
    private ScheduleEventDataHandler scheduleEventDataHandler;

    @Override
    public void process(
            Record<IgniteKey<?>, IgniteEvent> kafkaRecordIn, StreamProcessingContext ctxt) {

        IgniteKey<?> key = kafkaRecordIn.key();
        IgniteEvent value = kafkaRecordIn.value();

        LOGGER.debug(
                "received VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT vehicleId: {}", value.getVehicleId());

        // Disassociate the vehicle from the user
        vehicleProfileChangedNotificationEventUtil.processDisassociateEvent(
                value,
                (vehicleId, userId) -> {

                    // delete from mongo
                    vehicleProfileChangedNotificationEventUtil.deleteDataByVehicleIdAndUserId(
                            roDAOMongoImpl, RO_REQUEST_VEHICLE_ID, vehicleId, RO_REQUEST_USER_ID, userId);
                });

        // Delete all the RCPD schedules for the vehicle
        rcpdEventHandler.processRTN(key, value, vehicleProfileChangedNotificationEventUtil);

        Record<IgniteKey<?>, IgniteEvent> kafkaRecord =
                new Record<>(key, value, System.currentTimeMillis());

        // Forward the event in the event handler stream
        ctxt.forward(kafkaRecord);
    }
}
