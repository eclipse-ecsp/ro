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

import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.ro.ROStatus;
import org.eclipse.ecsp.domain.ro.Ro;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.entities.AbstractIgniteEvent;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.nosqldao.Updates;
import org.eclipse.ecsp.ro.RoDAOMongoImpl;
import org.eclipse.ecsp.services.utils.ServiceUtil;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.time.Instant;
import java.util.Iterator;
import java.util.Optional;

/**
 * Abstract implementation for Queue Handler interface.
 */
public abstract class AbstractQueueHandler implements QueueHandler {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(AbstractQueueHandler.class);

    @Autowired
    protected RoDAOMongoImpl roDAOMongoImpl;

    @Autowired
    protected RedissonClient redissonClient;

    @Autowired
    protected ServiceUtil serviceUtil;

    @Value("${ro.foreach.ttl:180000}")
    protected long roForeachTTL;

    @Value("${dma.service.max.retry}")
    protected long retryCount;

    @Value("${dma.service.retry.interval.millis}")
    protected long retryInterval;

    @Value("${dma.service.ttl.buffer}")
    protected long ttlBuffer;

    protected void updateEntityByROStatus(IgniteEvent event, ROStatus status) {

        Updates updateRoStatus = new Updates();
        updateRoStatus.addFieldSet(Constants.RO_STATUS, status);
        // find RO
        Optional<Ro> roOptional =
                roDAOMongoImpl.getROEntityByFieldNameByRoReqIdExceptACV(
                        event.getVehicleId(), event.getRequestId());
        if (roOptional.isPresent()) {
            roDAOMongoImpl.update(roOptional.get().getId(), updateRoStatus);
        }
    }

    protected void updateEntityByROStatusWithVinAndRoRequestID(
            String roRequestID, String vin, ROStatus status) {

        Updates updateRoStatus = new Updates();
        updateRoStatus.addFieldSet(Constants.RO_STATUS, status);
        // find RO
        Optional<Ro> roOptional =
                roDAOMongoImpl.getROEntityByFieldNameByRoReqIdExceptACV(vin, roRequestID);
        if (roOptional.isPresent()) {
            roDAOMongoImpl.update(roOptional.get().getId(), updateRoStatus);
        }
    }

    protected void sendToDevice(AbstractIgniteEvent igniteEvent, StreamProcessingContext ctxt) {
        LOGGER.debug("sending ro to telematics requestID:{}", igniteEvent.getRequestId());
        igniteEvent.setResponseExpected(true);
        igniteEvent.setDeviceRoutable(true);
        igniteEvent.setShoulderTapEnabled(true);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord =
                new Record<>(
                        new IgniteStringKey(igniteEvent.getVehicleId()),
                        igniteEvent,
                        System.currentTimeMillis());

        ctxt.forward(kafkaRecord);
    }

    protected void checkTTLExpireANDForwad(
            RQueue<AbstractIgniteEvent> queue, StreamProcessingContext ctxt) {
        Iterator<AbstractIgniteEvent> iterator = queue.iterator();
        while (iterator.hasNext()) {
            AbstractIgniteEvent pendingEvent = iterator.next();
            LOGGER.debug("pendingEvent in the queue:{}", pendingEvent);
            Instant instant = Instant.now();
            long currentTimeStamp = instant.toEpochMilli();
            if (pendingEvent.getTimestamp() + roForeachTTL > currentTimeStamp) {
                LOGGER.debug("not expired ttl");
                sendToDevice(pendingEvent, ctxt);
                break;
            } else {
                LOGGER.info(
                        " TTL expired, vin:{}, IgniteEvent:{}", pendingEvent.getVehicleId(), pendingEvent);
                updateEntityByROStatus(pendingEvent, ROStatus.TTL_EXPIRED);
                queue.poll();
            }
        }
    }
}
