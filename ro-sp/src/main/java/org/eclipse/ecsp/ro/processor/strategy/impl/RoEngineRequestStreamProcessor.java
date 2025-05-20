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
import org.eclipse.ecsp.cache.IgniteCache;
import org.eclipse.ecsp.domain.ro.AbstractRoEventData;
import org.eclipse.ecsp.domain.ro.RemoteOperationEngineV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationResponseV1_1;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.ro.utils.CachedKeyUtil;
import org.eclipse.ecsp.ro.utils.OutboundUtil;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * Concrete processor for remote engine request.
 *
 * @author Arnold
 */
@Component(Constants.REMOTEOPERATIONENGINE)
public class RoEngineRequestStreamProcessor extends RoRequestStreamProcessor {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(RoEngineRequestStreamProcessor.class);

    @Autowired
    private IgniteCache cache;

    @Autowired
    private OutboundUtil outboundUtil;

    @Value("#{${notification.responsemessage.notificationid.mapping}}")
    private Map<String, String> notificationIdMapping;

    /**
     * Process kafka record.
     *
     * @param kafkaRecordIn kafka record
     * @param ctxt          stream processing context
     */
    @Override
    public void process(
            Record<IgniteKey<?>, IgniteEvent> kafkaRecordIn, StreamProcessingContext ctxt) {
        IgniteKey<?> key = kafkaRecordIn.key();
        IgniteEvent value = kafkaRecordIn.value();
        AbstractRoEventData roEvent = (AbstractRoEventData) value.getEventData();
        RemoteOperationEngineV1_1 remoteOperationEngine = (RemoteOperationEngineV1_1) roEvent;

        // if engine start check is enabled and an engine STOPPED event is received,
        // check if engine start event was received for the vehicle.
        // If not, send a notification.
        if (engineStartCheckEnable
                && remoteOperationEngine
                .getState()
                .name()
                .equals(RemoteOperationEngineV1_1.State.STOPPED.name())) {
            String cacheKey = CachedKeyUtil.getEngineStatusKey(value);
            String cacheValue = cache.getString(cacheKey);

            LOGGER.debug("cacheValue:{}", cacheValue);

            if (null == cacheValue) {
                LOGGER.info("no remote engine start found for vin:{}", value.getVehicleId());

                // creating failed response
                RemoteOperationResponseV1_1 response =
                        outboundUtil.createRemoteOperationResponseV1_1(
                                remoteOperationEngine.getRoRequestId(),
                                remoteOperationEngine.getUserId(),
                                remoteOperationEngine.getPartnerId(),
                                RemoteOperationResponseV1_1.Response.FAIL);

                // sending notification that engine stop is received without engine start
                notificationUtil.sendRONotification(
                        key,
                        value,
                        ctxt,
                        roEvent.getOrigin(),
                        notificationIdMapping.get(org.eclipse.ecsp.ro.constants.Constants.ENGINE_NOT_START),
                        response);

                outboundUtil.sendROResponseOutbound(
                        key,
                        ctxt,
                        org.eclipse.ecsp.ro.constants.Constants.REMOTE_OPERATION_RESPONSE_EVENT_ID,
                        value.getVehicleId(),
                        value.getRequestId(),
                        value.getBizTransactionId(),
                        remoteOperationEngine.getOrigin(),
                        remoteOperationEngine.getUserId(),
                        response);
            } else {
                // if engine start event is found, process the engine stop event
                LOGGER.info("engine status check:{}", cacheValue);
                super.process(kafkaRecordIn, ctxt);
            }
        } else {
            LOGGER.info("skip engine status check for vin:{}", value.getVehicleId());
            super.process(kafkaRecordIn, ctxt);
        }
    }
}
