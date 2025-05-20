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

import org.apache.commons.lang3.StringUtils;
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
 * Default notification identifier.
 */
@Component(Constants.DEFAULT_IDENTIFIER)
public class DefaultNotificationIdentifier implements NotificationIdentifier {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(DefaultNotificationIdentifier.class);

    private static final String RESPONSE = "response";

    @Autowired
    protected NotificationResolver notificationResolver;

    /**
     * Get the response from the RemoteOperationResponseV1_1 or RemoteOperationNotificationV2_0 object.
     *
     * @param response the response
     * @return the response string
     */
    public String getRoCommandResponse(AbstractRoEventData response) {
        String roCommandResponse = StringUtils.EMPTY;
        Optional<Object> customExtension = response.getCustomExtension();
        if (response instanceof RemoteOperationResponseV1_1 responseObj) {
            if (customExtension.isPresent()) {
                LOGGER.debug("custom extension is present, will use custom extension response");
                GenericCustomExtension customData = (GenericCustomExtension) customExtension.get();
                roCommandResponse = (String) customData.getCustomData().get(RESPONSE);
            } else {
                roCommandResponse = responseObj.getResponse().toString();
            }
        }

        return roCommandResponse;
    }

    @Override
    public String getIdentifier(AbstractRoEventData response, String state, String eventId) {

        String roCommandResponse = getRoCommandResponse(response);

        String mappingIdentifier = eventId.toUpperCase() + Constants.UNDER_SCORE + state + Constants.UNDER_SCORE
                + roCommandResponse;
        LOGGER.debug("Default notification mapping identifier:{}", Utils.logForging(mappingIdentifier));
        return mappingIdentifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNotification(IgniteEvent value, String state, String eventId) {

        AbstractRoEventData response = (AbstractRoEventData) value.getEventData();
        String mappingIdentifier = this.getIdentifier(response, state, eventId);
        String notificationId = notificationResolver.get(value, eventId, mappingIdentifier);
        LOGGER.info("Default notification mapping identifier: {} -> notificationId: {}",
                Utils.logForging(mappingIdentifier), Utils.logForging(notificationId));

        return notificationId;
    }

}
