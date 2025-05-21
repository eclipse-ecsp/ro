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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.GenericCustomExtension;
import org.eclipse.ecsp.domain.ro.ROStatus;
import org.eclipse.ecsp.domain.ro.RemoteOperationResponseV1_1;
import org.eclipse.ecsp.entities.AbstractIgniteEvent;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.ro.utils.CachedKeyUtil;
import org.eclipse.ecsp.ro.utils.Utils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.redisson.api.RQueue;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Handler class for Response Queue.
 */
@Component("responseQueueHandler")
public class ResponseQueueHandler extends AbstractQueueHandler {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(ResponseQueueHandler.class);

    protected static final String RESPONSE = "response";

    @Override
    public void process(IgniteKey key, IgniteEvent value, StreamProcessingContext ctxt) {
        LOGGER.info("RO ResponseQueueHandler vin:{}, IgniteEvent:{}", value.getVehicleId(), value);

        RemoteOperationResponseV1_1 roResponse = (RemoteOperationResponseV1_1) value.getEventData();
        RemoteOperationResponseV1_1.Response responseEnum = roResponse.getResponse();
        Optional<Object> customExtension = roResponse.getCustomExtension();
        if (responseEnum.equals(RemoteOperationResponseV1_1.Response.SUCCESS)) {
            LOGGER.debug("RemoteOperationResponseV1_1.Response.SUCCESS");
            updateEntityByROStatusWithVinAndRoRequestID(
                    roResponse.getRoRequestId(), value.getVehicleId(), ROStatus.PROCESSED_SUCCESS);
            pickUpNextRoCMD(value, ctxt);
        } else if (responseEnum.equals(RemoteOperationResponseV1_1.Response.FAIL)) {
            LOGGER.debug("RemoteOperationResponseV1_1.Response.FAIL");
            updateEntityByROStatusWithVinAndRoRequestID(
                    roResponse.getRoRequestId(), value.getVehicleId(), ROStatus.PROCESSED_FAILED);
            pickUpNextRoCMD(value, ctxt);
        } else if (customExtension.isPresent()) {
            // if custom extension is not SUCCESS_CONTINUE -> pick up next RO request
            String roCommandResponse = StringUtils.EMPTY;
            LOGGER.debug("custom extension is present, will use custom extension response");
            GenericCustomExtension customdata = (GenericCustomExtension) customExtension.get();
            roCommandResponse = (String) customdata.getCustomData().get(RESPONSE);
            if (!roCommandResponse.equals(RemoteOperationResponseV1_1.Response.SUCCESS_CONTINUE.name())) {
                updateEntityByROStatusWithVinAndRoRequestID(
                        roResponse.getRoRequestId(), value.getVehicleId(), ROStatus.PROCESSED_FAILED);
                pickUpNextRoCMD(value, ctxt);
            }
        }
    }

    private void pickUpNextRoCMD(IgniteEvent value, StreamProcessingContext ctxt) {
        RemoteOperationResponseV1_1 roResponse = (RemoteOperationResponseV1_1) value.getEventData();
        RQueue<AbstractIgniteEvent> queue =
                redissonClient.getQueue((CachedKeyUtil.getROQueueKey(value)));

        AbstractIgniteEvent igniteEvent = queue.peek();
        if (null != igniteEvent) {
            LOGGER.debug(
                    "requestId in the queue:{},requestId in the response:{}",
                    Utils.logForging(igniteEvent.getRequestId()),
                    Utils.logForging(roResponse.getRoRequestId()));
            if (igniteEvent.getRequestId().equals(roResponse.getRoRequestId())) {
                // move the head ignite event from the queue
                AbstractIgniteEvent head = queue.poll();
                LOGGER.info("poll the ro request from the queue:{}", Utils.logForging(head));
            }
        }

        checkTTLExpireANDForwad(queue, ctxt);
    }
}
