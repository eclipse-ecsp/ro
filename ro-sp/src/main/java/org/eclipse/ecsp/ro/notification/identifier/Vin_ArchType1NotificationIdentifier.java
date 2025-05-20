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

package org.eclipse.ecsp.ro.notification.identifier;

import org.eclipse.ecsp.domain.GenericCustomExtension;
import org.eclipse.ecsp.domain.ro.AbstractRoEventData;
import org.eclipse.ecsp.domain.ro.RemoteOperationResponseV1_1;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.ro.notification.NotificationResolver;
import org.eclipse.ecsp.ro.utils.Utils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Vin_ArchType1 notification identifier, use v2c RemoteOperationResponse.failureReasonCode to do
 * enhancement.
 */
@Component(Constants.VEHICLE_ARCHTYPE1_IDENTIFIER)
public class Vin_ArchType1NotificationIdentifier implements NotificationIdentifier {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(Vin_ArchType1NotificationIdentifier.class);

    private static final String FAILURE_REASON_CODE = "failureReasonCode";

    @Autowired
    protected NotificationResolver notificationResolver;

    @Autowired
    private DefaultNotificationIdentifier defaultNotificationIdentifier;

    @Override
    public String getIdentifier(AbstractRoEventData response, String state, String eventId) {
        String enhanceIdentifier =
                defaultNotificationIdentifier.getIdentifier(response, state, eventId);
        Optional<Object> customExtension = response.getCustomExtension();
        if (response instanceof RemoteOperationResponseV1_1 && customExtension.isPresent()) {
            GenericCustomExtension customdata = (GenericCustomExtension) customExtension.get();
            Integer failureReasonCode = (Integer) customdata.getCustomData().get(FAILURE_REASON_CODE);
            if (null != failureReasonCode && failureReasonCode > 0) {
                enhanceIdentifier = enhanceIdentifier + Constants.UNDER_SCORE + failureReasonCode;
            }
        }
        return enhanceIdentifier;
    }

    @Override
    public String getNotification(IgniteEvent value, String state, String eventId) {
        AbstractRoEventData response = (AbstractRoEventData) value.getEventData();
        String enhanceIdentifier = this.getIdentifier(response, state, eventId);
        String notificationId = notificationResolver.get(value, eventId, enhanceIdentifier);
        LOGGER.info(
                "Vin_ArchType1 vin: {} is using identifier: {} -> notificationID: {}",
                Utils.logForging(value.getVehicleId()),
                Utils.logForging(enhanceIdentifier),
                Utils.logForging(notificationId));
        // in case no match notificationId with failureReasonCode, fallback to default identifier
        return (null != notificationId)
                ? notificationId
                : defaultNotificationIdentifier.getNotification(value, state, eventId);
    }
}
