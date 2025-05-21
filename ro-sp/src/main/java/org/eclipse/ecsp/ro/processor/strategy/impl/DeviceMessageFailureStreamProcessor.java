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

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.DeviceMessageFailureEventDataV1_0;
import org.eclipse.ecsp.domain.EventID;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.remoteInhibit.AbstractRemoteInhibitEventData;
import org.eclipse.ecsp.domain.remoteInhibit.RemoteInhibitResponseV1_1;
import org.eclipse.ecsp.domain.ro.AbstractRoEventData;
import org.eclipse.ecsp.domain.ro.RemoteOperationResponseV1_1;
import org.eclipse.ecsp.domain.ro.Ro;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.nosqldao.Updates;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.ro.processor.RCPDHandler;
import org.eclipse.ecsp.ro.processor.RemoteInhibitDataHandler;
import org.eclipse.ecsp.ro.queue.QueueHandler;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Objects;
import java.util.Optional;

/**
 * Stream Processor for DeviceMessageFailure event.
 */
@Component(EventID.DEVICEMESSAGEFAILURE)
public class DeviceMessageFailureStreamProcessor extends AbstractStreamProcessor {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(DeviceMessageFailureStreamProcessor.class);

    @Value("${deviceMessage.failures.store:false}")
    protected boolean deviceMessageFailuresStore;

    @Autowired
    private RemoteInhibitDataHandler riEventHandler;

    @Autowired
    private RCPDHandler rcpdEventHandler;

    @Autowired
    @Qualifier("deviceMessageFailureQueueHandler")
    private QueueHandler queueHandler;

    @Override
    public void process(Record<IgniteKey<?>, IgniteEvent> kafkaRecordIn, StreamProcessingContext ctxt) {

        IgniteKey<?> key = kafkaRecordIn.key();
        IgniteEvent value = kafkaRecordIn.value();

        LOGGER.error("Device_Message_Failure event received: {}", value);

        IgniteEventImpl event = new IgniteEventImpl();

        DeviceMessageFailureEventDataV1_0 dmFailureEventData = (DeviceMessageFailureEventDataV1_0) value
                .getEventData();

        IgniteEventImpl failedEvent = (IgniteEventImpl) dmFailureEventData.getFailedIgniteEvent();

        if (deviceMessageFailuresStore) {
            updateDeviceFailureMessages(value, failedEvent);
        }

        if (Constants.ERROR_RESPONSE.containsKey(dmFailureEventData.getErrorCode())
                && (Objects.nonNull(failedEvent) && Objects.nonNull(failedEvent.getEventData()))) {

            String origin = null;
            String userId = null;
            String qualifier = null;

            if (failedEvent.getEventData() instanceof AbstractRoEventData) {

                // process RO failure event
                event = processAbstractEventData(key, value, failedEvent, ctxt, dmFailureEventData);
                qualifier = getAbstractEventQualifier(failedEvent);

            } else if (failedEvent.getEventData() instanceof AbstractRemoteInhibitEventData riEventData) {

                // Process RI failure event
                origin = riEventData.getOrigin();
                userId = riEventData.getUserId();

                event.setVehicleId(failedEvent.getVehicleId());
                event.setEventId(Constants.EVENT_ID_REMOTE_INHIBIT_RESPONSE);
                event.setUserContextInfo(failedEvent.getUserContextInfo());

                RemoteInhibitResponseV1_1 failureResponse = new RemoteInhibitResponseV1_1();
                failureResponse.setRoRequestId(failedEvent.getRequestId());
                failureResponse.setUserId(userId);
                failureResponse.setResponse(dmFailureRemoteInhibitResponse(dmFailureEventData));

                event.setEventData(failureResponse);
                event.setVersion(Version.V1_1);
                qualifier = riEventHandler.setQualifier(key, event, origin);

            } else if (StringUtils.equals(org.eclipse.ecsp.domain.ro.constant.Constants.RCPDREQUEST,
                    failedEvent.getEventId())) {

                // process rcpd failure event
                rcpdEventHandler.handleDeviceMessageFailure(key, value, ctxt);
            }

            createAndForwardResponseEvent(key, event, failedEvent, ctxt, dmFailureEventData, qualifier);

        }
    }

    private void createAndForwardResponseEvent(
            IgniteKey<?> key,
            IgniteEventImpl event,
            IgniteEventImpl failedEvent,
            StreamProcessingContext ctxt,
            DeviceMessageFailureEventDataV1_0 dmFailureEventData,
            String qualifier) {

        LOGGER.debug("DFF Qualifier : {}", qualifier);

        if (Objects.nonNull(qualifier) && (StringUtils.isNotBlank(qualifier))) {
            event.setVehicleId(failedEvent.getVehicleId());
            event.setRequestId(failedEvent.getRequestId());
            event.setBizTransactionId(failedEvent.getBizTransactionId());
            event.setDFFQualifier(qualifier);
            event.setTimestamp(System.currentTimeMillis());

            LOGGER.debug("requestId:{}, deviceMessageFailure error code :{}",
                    failedEvent.getRequestId(),
                    dmFailureEventData.getErrorCode());

            LOGGER.debug("Forwarding Device Message Failure Event: {}", event);

            Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(key, event, System.currentTimeMillis());
            ctxt.forward(kafkaRecord);

        }
    }

    private String getAbstractEventQualifier(IgniteEventImpl failedEvent) {
        AbstractRoEventData roEventData = (AbstractRoEventData) failedEvent.getEventData();
        String origin = roEventData.getOrigin();

        if (StringUtils.isNotBlank(origin)) {

            StringBuilder builder = new StringBuilder(Constants.QUALIFIER);

            builder.append(Constants.UNDER_SCORE)
                    .append(Constants.REMOTE_OPERATION_RESPONSE_EVENT_ID.toUpperCase())
                    .append(Constants.UNDER_SCORE)
                    .append(origin.toUpperCase());

            return builder.toString();
        }
        return null;
    }

    private IgniteEventImpl processAbstractEventData(
            IgniteKey key,
            IgniteEvent value,
            IgniteEventImpl failedEvent,
            StreamProcessingContext ctxt,
            DeviceMessageFailureEventDataV1_0 dmFailureEventData) {

        IgniteEventImpl event = new IgniteEventImpl();
        event.setEventId(Constants.REMOTE_OPERATION_RESPONSE_EVENT_ID);
        event.setUserContextInfo(failedEvent.getUserContextInfo());
        RemoteOperationResponseV1_1 failureResponse = new RemoteOperationResponseV1_1();
        failureResponse.setRoRequestId(failedEvent.getRequestId());
        AbstractRoEventData roEventData = (AbstractRoEventData) failedEvent.getEventData();
        String userId = roEventData.getUserId();
        failureResponse.setUserId(userId);

        if (ObjectUtils.isNotEmpty(roEventData.getPartnerId())) {
            failureResponse.setPartnerId(roEventData.getPartnerId());
        }

        failureResponse.setResponse(Constants.ERROR_RESPONSE.get(dmFailureEventData.getErrorCode()));
        event.setEventData(failureResponse);
        event.setVersion(Version.V1_1);

        if (roQueueEnable) {
            queueHandler.process(key, value, ctxt);
        }

        return event;
    }

    private RemoteInhibitResponseV1_1.Response dmFailureRemoteInhibitResponse(
            DeviceMessageFailureEventDataV1_0 dmFailureEventData) {
        if (Objects.nonNull(dmFailureEventData) && Objects.nonNull(dmFailureEventData.getErrorCode())) {
            RemoteOperationResponseV1_1.Response errorResponse =
                    Constants.ERROR_RESPONSE.get(dmFailureEventData.getErrorCode());
            if (Objects.nonNull(errorResponse)) {
                return RemoteInhibitResponseV1_1.Response.valueOf(errorResponse.toString());
            }
        }
        return null;
    }

    private void updateDeviceFailureMessages(IgniteEvent value, IgniteEventImpl failedEvent) {
        if (Objects.nonNull(failedEvent) && Objects.nonNull(failedEvent.getEventData())
                && (failedEvent.getEventData() instanceof AbstractRoEventData)) {

            Updates updateServiceMessageFailures = new Updates();

            updateServiceMessageFailures.addListAppend(
                    org.eclipse.ecsp.domain.ro.constant.Constants.DEVICE_MESSAGE_FAILURES,
                    value);

            // For all the RO failed events for a roRequestId ,
            // update the events with failure details
            Optional<Ro> roOptional = roDAOMongoImpl.getROEntityByFieldNameByRoReqId(
                    failedEvent.getVehicleId(),
                    failedEvent.getRequestId());

            roOptional.ifPresent(ro -> roDAOMongoImpl.update(ro.getId(), updateServiceMessageFailures));
        }
    }

}
