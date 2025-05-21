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

import org.eclipse.ecsp.domain.ro.AbstractRoEventData;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.ro.notification.NotificationResolver;
import org.eclipse.ecsp.ro.utils.Utils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * Notification resolver for the ecu type and the arch type.
 */
@Component
public class NotificationArchAndECUTypeResolver {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(NotificationArchAndECUTypeResolver.class);

    @Autowired
    private Map<String, NotificationIdentifier> notificationIdentifierMap;

    @Value("#{${notification.identifier.archAndEcu}}")
    private Map<String, Map<String, String>> identifierMapping;

    @Autowired
    private NotificationResolver notificationResolver;

    @Autowired
    @Qualifier(Constants.DEFAULT_IDENTIFIER)
    private DefaultNotificationIdentifier defaultNotificationIdentifier;

    @Value("#{${notification.responsemessage.notificationid.mapping}}")
    private Map<String, String> notificationIdMapping;

    /**
     * Get the notification id for the IgniteEvent.
     *
     * @param value    the {@link IgniteEvent}
     * @param state    the state
     * @param eventId  the event id
     * @param archType the arch type
     * @param ecuType  the ecu type
     * @return the notification id
     */
    public String getNotification(
            IgniteEvent value, String state, String eventId, String archType, String ecuType) {
        LOGGER.debug(
                "start getting notification id for IgniteEvent: {} ,"
                        + " state: {}, eventId: {}, archType: {}, ecuType: {}",
                Utils.logForging(value),
                Utils.logForging(state),
                Utils.logForging(eventId),
                Utils.logForging(archType),
                Utils.logForging(ecuType));
        Map<String, String> ecuMapping = identifierMapping.get(archType);

        String identifier = null;
        if (null != ecuMapping && null != ecuMapping.get(ecuType)) {
            identifier = ecuMapping.get(ecuType);
        }
        NotificationIdentifier notificationIdentifierBean = notificationIdentifierMap.get(identifier);
        // if no identifier is there , fall back to default logic
        if (null == notificationIdentifierBean) {
            notificationIdentifierBean = defaultNotificationIdentifier;
        }
        String notificationId = notificationIdentifierBean.getNotification(value, state, eventId);

        if (null == notificationId) {
            AbstractRoEventData response = (AbstractRoEventData) value.getEventData();
            String roCommandResponse = defaultNotificationIdentifier.getRoCommandResponse(response);
            LOGGER.debug("notification id not found, so will try for :{} ", roCommandResponse);
            notificationId = notificationIdMapping.get(roCommandResponse);
        }
        return notificationId;
    }
}
