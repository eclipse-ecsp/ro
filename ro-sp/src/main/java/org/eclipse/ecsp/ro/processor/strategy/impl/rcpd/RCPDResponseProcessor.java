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

package org.eclipse.ecsp.ro.processor.strategy.impl.rcpd;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.ro.processor.RCPDHandler;
import org.eclipse.ecsp.ro.processor.strategy.impl.AbstractStreamProcessor;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * RCPDResponseProcessor - This class is responsible for processing the RCPD response. Extends the
 * AbstractStreamProcessor class.
 */
@Component(Constants.RCPDRESPONSE)
public class RCPDResponseProcessor extends AbstractStreamProcessor {
    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(RCPDResponseProcessor.class);

    @Autowired
    private RCPDHandler rcpdEventHandler;

    @Override
    public void process(
            Record<IgniteKey<?>, IgniteEvent> kafkaRecordIn, StreamProcessingContext ctxt) {
        IgniteKey<?> key = kafkaRecordIn.key();
        IgniteEvent value = kafkaRecordIn.value();
        IgniteEventImpl rcpdResImpl = rcpdEventHandler.processRCPDResponse(key, value, ctxt);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord =
                new Record<>(key, rcpdResImpl, System.currentTimeMillis());

        if (rcpdResImpl != null) {
            if (StringUtils.isNotEmpty(rcpdResImpl.getDFFQualifier())) {
                ctxt.forward(kafkaRecord);
            } else {
                LOGGER.debug(
                        "RCPD response vehicleId: {}, sessionId: {} qualifier is empty, not forward",
                        rcpdResImpl.getVehicleId(),
                        rcpdResImpl.getBizTransactionId());
            }
        }
    }
}
