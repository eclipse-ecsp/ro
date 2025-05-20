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
import org.eclipse.ecsp.domain.AcknowledgementV1_1;
import org.eclipse.ecsp.domain.AuthorizedPartnerDetail;
import org.eclipse.ecsp.domain.AuthorizedPartnerDetailItem;
import org.eclipse.ecsp.domain.EventID;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.ro.processor.RemoteInhibitDataHandler;
import org.eclipse.ecsp.ro.processor.strategy.impl.AbstractStreamProcessor;
import org.eclipse.ecsp.services.utils.ServiceUtil;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import static org.eclipse.ecsp.domain.ro.constant.Constants.EVENT_ID_CRANK_NOTIFICATION_DATA;

/**
 * Processor for CrankNotificationData event.
 */
@Component(EVENT_ID_CRANK_NOTIFICATION_DATA)
public class RINotificationDataProcessor extends AbstractStreamProcessor {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(RINotificationDataProcessor.class);

    @Autowired
    private RemoteInhibitDataHandler riEventHandler;

    @Value("${mqtt.ri.svas.topic.name}")
    private String riSvasMqttTopic;

    @Autowired
    private ServiceUtil serviceUtil;

    @Value("${ri.partner.service.id}")
    private String riPartnerServiceId;

    @Override
    public void process(
            Record<IgniteKey<?>, IgniteEvent> kafkaRecordIn, StreamProcessingContext ctxt) {
        IgniteKey<?> key = kafkaRecordIn.key();
        IgniteEvent value = kafkaRecordIn.value();

        IgniteEventImpl crankNotiImpl = riEventHandler.processNotificationEvent(key, value);
        if (isSendEventToPartnerOutbound(key, crankNotiImpl, ctxt, riPartnerServiceId)) {
            if (StringUtils.isNotEmpty(crankNotiImpl.getDFFQualifier())) {
                LOGGER.debug("sending to outbound - getDFFQualifier :: {}", value.getDFFQualifier());
                Record<IgniteKey<?>, IgniteEvent> kafkaRecord =
                        new Record<>(key, crankNotiImpl, System.currentTimeMillis());

                ctxt.forward(kafkaRecord);

            } else {
                LOGGER.debug(
                        "vehicleId: {}, sessionId: {} qualifier is empty, not forward",
                        crankNotiImpl.getVehicleId(),
                        crankNotiImpl.getBizTransactionId());
            }
        }

        IgniteEventImpl ackIgniteEvent =
                generateAckEventData(AcknowledgementV1_1.Status.SUCCESS, value);

        LOGGER.debug(
                "Key={}, ackIgniteEvent={}, sinkTopic={}",
                key.toString(),
                ackIgniteEvent.toString(),
                sinkTopics[0]);

        ackIgniteEvent.setDevMsgTopicSuffix(riSvasMqttTopic);

        Record<IgniteKey<?>, IgniteEvent> kafkaRecord =
                new Record<>(key, ackIgniteEvent, System.currentTimeMillis());

        ctxt.forward(kafkaRecord);
    }

    private boolean isSendEventToPartnerOutbound(
            final IgniteKey key,
            final IgniteEventImpl value,
            StreamProcessingContext ctxt,
            final String serviceId) {

        boolean sendToCallCenter = true;

        AuthorizedPartnerDetail authorizedPartnerDetail =
                serviceUtil.getAuthorizedPartnerDetail(
                        value.getVehicleId(), value.getEventId().toUpperCase(), serviceId);

        LOGGER.debug(
                "authorizedPartnerDetail: {}, size of list: {}",
                authorizedPartnerDetail.getOutboundDetails(),
                authorizedPartnerDetail.getOutboundDetails().size());

        if (!authorizedPartnerDetail.getOutboundDetails().isEmpty()) {

            for (AuthorizedPartnerDetailItem item : authorizedPartnerDetail.getOutboundDetails()) {

                LOGGER.debug(
                        "set authorized partner qualifier to  event, partnerId:{}, qualifier:{}",
                        item.getPartnerId(),
                        item.getQualifier());

                (value).setDFFQualifier(item.getQualifier());

                Record<IgniteKey<?>, IgniteEvent> kafkaRecord =
                        new Record<>(key, value, System.currentTimeMillis());

                ctxt.forward(kafkaRecord);
            }

            (value).setDFFQualifier("");
            sendToCallCenter = false;
        }
        return sendToCallCenter;
    }

    private IgniteEventImpl generateAckEventData(
            AcknowledgementV1_1.Status status, IgniteEvent value) {
        IgniteEventImpl ackIgniteEvent = new IgniteEventImpl();
        AcknowledgementV1_1 ackData = new AcknowledgementV1_1();
        ackData.setStatus(status);
        ackIgniteEvent.setEventData(ackData);
        ackIgniteEvent.setEventId(EventID.ACKNOWLEDGEMENT.toString());
        ackIgniteEvent.setVersion(Version.V1_0);
        ackIgniteEvent.setTimestamp(System.currentTimeMillis());
        ackIgniteEvent.setCorrelationId(value.getMessageId());
        ackIgniteEvent.setMessageId(globalMessageIdGenerator.generateUniqueMsgId(value.getVehicleId()));
        ackIgniteEvent.setBizTransactionId(value.getBizTransactionId());
        ackIgniteEvent.setDeviceRoutable(true);
        ackIgniteEvent.setVehicleId(value.getVehicleId());
        return ackIgniteEvent;
    }
}
