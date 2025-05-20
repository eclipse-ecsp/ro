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
import org.eclipse.ecsp.domain.ro.ROStatus;
import org.eclipse.ecsp.entities.AbstractIgniteEvent;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.ro.utils.CachedKeyUtil;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.redisson.api.RQueue;
import org.springframework.stereotype.Component;
import java.util.Iterator;

/**
 * Request Queue Handler.
 */
@Component
public class RequestQueueHandler extends AbstractQueueHandler {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(RequestQueueHandler.class);

    @Override
    public void process(IgniteKey key, IgniteEvent value, StreamProcessingContext ctxt) {

        LOGGER.info("RO RequestQueueHandler vin:{}, IgniteEvent:{}", value.getVehicleId(), value);

        RQueue<AbstractIgniteEvent> queue = redissonClient.getQueue(CachedKeyUtil.getROQueueKey(value));
        queue.offer((AbstractIgniteEvent) value);

        LOGGER.info(
                "append ro request to the queue, vin:{}, IgniteEvent:{}", value.getVehicleId(), value);
        // if the first ro request in the queue is not expired , send it to device
        // if the first ro request in the queue is expired,
        // keep picking the next ro request until a non-expired event and forward it
        boolean isExpired = false;

        LOGGER.debug("queue.size:{}", queue.size());
        Iterator<AbstractIgniteEvent> iterator = queue.iterator();

        while (iterator.hasNext()) {

            AbstractIgniteEvent igniteEvent = iterator.next();

            LOGGER.debug("current ignite event: {}", igniteEvent);

            LOGGER.debug(
                    "roForeachTTL: {},"
                            + " System.currentTimeMillis: {},"
                            + " igniteEvent.getTimestamp: {},"
                            + "currentTimeMillis-igniteEvent.getTimestamp: {}",
                    roForeachTTL,
                    System.currentTimeMillis(),
                    igniteEvent.getTimestamp(),
                    System.currentTimeMillis() - igniteEvent.getTimestamp());

            if (System.currentTimeMillis() - igniteEvent.getTimestamp() > roForeachTTL) {
                // info TTL expired
                LOGGER.info(
                        " TTL expired, vin:{}, IgniteEvent:{}", igniteEvent.getVehicleId(), igniteEvent);
                updateEntityByROStatus(igniteEvent, ROStatus.TTL_EXPIRED);
                iterator.remove();
                isExpired = true;
            } else {
                LOGGER.debug(
                        "before send ro to device, vehicle: {}, "
                                + "requestId: {}"
                                + ", isExpired: {}"
                                + ", queue.size: {}",
                        igniteEvent.getVehicleId(),
                        igniteEvent.getRequestId(),
                        isExpired,
                        queue.size());

                // either an or more than one event in the queue expired
                // or there is only one event in the queue which is not expired.
                // in both the cases, send the not-expired event to device
                if (isExpired || queue.size() == 1) {
                    sendToDevice(igniteEvent, ctxt);
                }
                break;
            }
        }
    }
}
