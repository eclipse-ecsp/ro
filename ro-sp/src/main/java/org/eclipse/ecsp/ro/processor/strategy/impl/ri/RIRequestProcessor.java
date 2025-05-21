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

import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.ro.processor.RemoteInhibitDataHandler;
import org.eclipse.ecsp.ro.processor.strategy.impl.AbstractStreamProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import static org.eclipse.ecsp.domain.ro.constant.Constants.EVENT_ID_REMOTE_INHIBIT_REQUEST;

/**
 * Processor for RIRequest event.
 */
@Component(EVENT_ID_REMOTE_INHIBIT_REQUEST)
public class RIRequestProcessor extends AbstractStreamProcessor {

    @Autowired
    private RemoteInhibitDataHandler riEventHandler;

    @Value("${mqtt.ri.svas.topic.name}")
    private String riSvasMqttTopic;

    @Override
    public void process(
            Record<IgniteKey<?>, IgniteEvent> kafkaRecordIn, StreamProcessingContext ctxt) {
        IgniteKey<?> key = kafkaRecordIn.key();
        IgniteEvent value = kafkaRecordIn.value();
        IgniteEventImpl riReqImpl = riEventHandler.processUserEvent(key, value);
        riReqImpl.setDevMsgTopicSuffix(riSvasMqttTopic);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord =
                new Record<>(key, riReqImpl, System.currentTimeMillis());

        ctxt.forward(kafkaRecord);
    }
}
