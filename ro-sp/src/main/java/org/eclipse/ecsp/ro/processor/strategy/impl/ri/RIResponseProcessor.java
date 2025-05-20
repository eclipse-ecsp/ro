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

package org.eclipse.ecsp.ro.processor.strategy.impl.ri;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.ro.processor.RemoteInhibitDataHandler;
import org.eclipse.ecsp.ro.processor.strategy.impl.AbstractStreamProcessor;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static org.eclipse.ecsp.domain.ro.constant.Constants.EVENT_ID_REMOTE_INHIBIT_RESPONSE;

/**
 * Processor for RIResponse event.
 */
@Component(EVENT_ID_REMOTE_INHIBIT_RESPONSE)
public class RIResponseProcessor extends AbstractStreamProcessor {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(RIResponseProcessor.class);

    @Autowired
    private RemoteInhibitDataHandler riEventHandler;

    @Override
    public void process(
            Record<IgniteKey<?>, IgniteEvent> kafkaRecordIn, StreamProcessingContext ctxt) {
        IgniteKey<?> key = kafkaRecordIn.key();
        IgniteEvent value = kafkaRecordIn.value();

        IgniteEventImpl riResImpl = riEventHandler.processDeviceEvent(key, value);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord =
                new Record<>(key, riResImpl, System.currentTimeMillis());

        if (StringUtils.isNotEmpty(riResImpl.getDFFQualifier())) {
            ctxt.forward(kafkaRecord);
        } else {
            LOGGER.debug(
                    "vehicleId: {}, sessionId: {} qualifier is empty, not forward",
                    riResImpl.getVehicleId(),
                    riResImpl.getBizTransactionId());
        }
    }
}
