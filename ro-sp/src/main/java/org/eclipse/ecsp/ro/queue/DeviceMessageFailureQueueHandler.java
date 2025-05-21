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

package org.eclipse.ecsp.ro.queue;

import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.DeviceMessageFailureEventDataV1_0;
import org.eclipse.ecsp.domain.ro.ROStatus;
import org.eclipse.ecsp.entities.AbstractIgniteEvent;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.ro.utils.CachedKeyUtil;
import org.eclipse.ecsp.ro.utils.Utils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.redisson.api.RQueue;
import org.springframework.stereotype.Component;

/**
 * Queue handler for deviceMessageFailure.
 */
@Component("deviceMessageFailureQueueHandler")
public class DeviceMessageFailureQueueHandler extends AbstractQueueHandler {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(DeviceMessageFailureQueueHandler.class);

    @Override
    public void process(IgniteKey key, IgniteEvent value, StreamProcessingContext ctxt) {
        LOGGER.info(
                "DeviceMessageFailureQueueHandler vin:{}, IgniteEvent:{}", value.getVehicleId(), value);
        DeviceMessageFailureEventDataV1_0 dmFailureEventData =
                (DeviceMessageFailureEventDataV1_0) value.getEventData();
        LOGGER.info("dmFailureEventData->getErrorCode:{}", dmFailureEventData.getErrorCode());
        LOGGER.debug(
                "dmFailureEventData->getFailedIgniteEvent failedEvent:{}",
                dmFailureEventData.getFailedIgniteEvent());

        // pick up next
        if (Constants.PICKABLE_DEVICE_MESSAGE_ERROR_CODE.contains(dmFailureEventData.getErrorCode())) {
            IgniteEventImpl failedEvent = (IgniteEventImpl) dmFailureEventData.getFailedIgniteEvent();
            updateEntityByROStatus(failedEvent, ROStatus.PROCESSED_FAILED);
            RQueue<AbstractIgniteEvent> queue =
                    redissonClient.getQueue(CachedKeyUtil.getROQueueKey(failedEvent));
            AbstractIgniteEvent headEvent = queue.peek();
            LOGGER.debug("headEvent in the queue:{}", Utils.logForging(headEvent));
            if (null != headEvent) {
                LOGGER.debug(
                        "requestId:{} of the headEvent in the queue,requestId:{} in the DMA failedEvent",
                        Utils.logForging(headEvent.getRequestId()),
                        Utils.logForging(failedEvent.getRequestId()));
                if (headEvent.getRequestId().equals(failedEvent.getRequestId())) {
                    AbstractIgniteEvent head = queue.poll();
                    LOGGER.info("poll the ro request from the queue:{}", Utils.logForging(head));
                }
            }
            checkTTLExpireANDForwad(queue, ctxt);
        }
    }
}
