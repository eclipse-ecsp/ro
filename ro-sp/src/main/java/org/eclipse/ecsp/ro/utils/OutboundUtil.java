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
 *
 *
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

package org.eclipse.ecsp.ro.utils;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.ro.RemoteOperationResponseV1_1;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.entities.UserContext;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Arrays;

/**
 * Utility class for outbound data.
 */
@Component
public class OutboundUtil {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(OutboundUtil.class);

    /**
     * Send outbounded RO response.
     *
     * @param key              Ignite Key
     * @param ctxt             stream processing context
     * @param eventId          event id
     * @param vehicleId        vehicle id
     * @param requestId        request id
     * @param bizTransactionId biz transaction id
     * @param origin           origin source
     * @param userId           user id
     * @param responseV1_1     response
     */
    public void sendROResponseOutbound(
            IgniteKey key,
            StreamProcessingContext ctxt,
            String eventId,
            String vehicleId,
            String requestId,
            String bizTransactionId,
            String origin,
            String userId,
            RemoteOperationResponseV1_1 responseV1_1) {
        IgniteEventImpl event = new IgniteEventImpl();
        event.setEventId(eventId);
        event.setVehicleId(vehicleId);
        event.setRequestId(requestId);
        event.setBizTransactionId(bizTransactionId);
        StringBuilder qualifier = new StringBuilder();
        qualifier
                .append(Constants.QUALIFIER)
                .append(Constants.UNDER_SCORE)
                .append(event.getEventId().toUpperCase())
                .append(Constants.UNDER_SCORE)
                .append(origin.toUpperCase());
        event.setDFFQualifier(qualifier.toString());
        event.setTimestamp(System.currentTimeMillis());
        UserContext userContext = new UserContext();
        userContext.setUserId(userId);
        event.setUserContextInfo(Arrays.asList(userContext));
        event.setEventData(responseV1_1);
        LOGGER.debug(event, "Forwarding dff outbound Event");
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord =
                new Record<>(key, event, System.currentTimeMillis());

        ctxt.forward(kafkaRecord);
    }

    /**
     * Create RemoteOperationResponseV1_1 response from the given data.
     *
     * @param requestId request id
     * @param userId    user id
     * @param partnerId partner id
     * @param response  response of type {@link RemoteOperationResponseV1_1.Response}
     * @return response object
     * @see RemoteOperationResponseV1_1
     */
    public RemoteOperationResponseV1_1 createRemoteOperationResponseV1_1(
            String requestId,
            String userId,
            String partnerId,
            RemoteOperationResponseV1_1.Response response) {
        RemoteOperationResponseV1_1 remoteOperationResponseV11 = new RemoteOperationResponseV1_1();
        remoteOperationResponseV11.setRoRequestId(requestId);
        remoteOperationResponseV11.setUserId(userId);
        // set partnerId
        if (ObjectUtils.isNotEmpty(partnerId)) {
            remoteOperationResponseV11.setPartnerId(partnerId);
        }
        remoteOperationResponseV11.setResponse(response);
        return remoteOperationResponseV11;
    }
}
