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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.GenericCustomExtension;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.RemoteOperationResponseV1_1;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.ro.domains.ROGenericNotificationEventDataV1_1;
import org.eclipse.ecsp.ro.domains.ROGenericNotificationEventV1_1;
import org.eclipse.ecsp.ro.notification.NotificationResolver;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility class for notifications.
 */
@Component
public class NotificationUtil {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(NotificationUtil.class);

    private static final String RESPONSE = "response";

    @Value("${whitelisted.dff.origins.list:THIRDPARTY2}")
    private String[] whitelistedDffOrigins;

    @Value("${source.topic.name}")
    private String[] sourceTopics;

    @Value("${sink.topic.name}")
    private String[] sinkTopics;

    @Value("#{${notification.status.mapping}}")
    private Map<String, String> notificationStatusMapping;

    @Value("#{${notification.responsemessage.notificationid.mapping}}")
    private Map<String, String> notificationIdMapping;

    @Autowired
    private NotificationResolver notificationResolver;

    boolean isWhitelistedDffOrigins(String origin) {
        return ArrayUtils.contains(whitelistedDffOrigins, origin);
    }

    /**
     * Send RO notification.
     *
     * @param key            Ignite Key
     * @param value          Ignite event
     * @param ctxt           stream processing context
     * @param origin         origin source
     * @param notificationId notification id
     * @param response       response
     */
    public void sendRONotification(
            IgniteKey key,
            IgniteEvent value,
            StreamProcessingContext ctxt,
            String origin,
            String notificationId,
            RemoteOperationResponseV1_1 response) {
        if (!isWhitelistedDffOrigins(origin)) {
            LOGGER.info("{} User::Notification is not required", Utils.logForging(origin));
            return;
        }
        if (StringUtils.isEmpty(notificationId)) {
            LOGGER.info(
                    "Notificationid not found, so skipping notification for ro request id :{}",
                    response.getRoRequestId());
            return;
        }

        IgniteEventImpl notificationEvent = createNotificationEvent(value, response, notificationId);
        LOGGER.debug("prepare sending notificationEvent: {}", Utils.logForging(notificationEvent));
        if (sinkTopics.length > 0) {
            ctxt.forwardDirectly(key, notificationEvent, sinkTopics[0]);
        } else {
            LOGGER.error("sink topic is not configured for notification");
        }
    }

    private IgniteEventImpl createNotificationEvent(
            IgniteEvent value, RemoteOperationResponseV1_1 response, String notificationId) {
        IgniteEventImpl notificationEvent = new IgniteEventImpl();
        notificationEvent.setEventId(Constants.GENERICNOTIFICATIONEVENT);
        notificationEvent.setVersion(Version.V1_0);
        notificationEvent.setTimestamp(System.currentTimeMillis());
        notificationEvent.setVehicleId(value.getVehicleId());
        notificationEvent.setBizTransactionId(value.getBizTransactionId());
        ROGenericNotificationEventDataV1_1 notificationData =
                generateNotificationData(response, notificationId);
        if (notificationData != null) {
            notificationEvent.setRequestId(notificationData.getRoRequestId());
            notificationEvent.setEventData(notificationData);
        } else {
            notificationEvent.setEventData(
                    new ROGenericNotificationEventV1_1(
                            notificationId,
                            Optional.ofNullable(response)
                                    .map(RemoteOperationResponseV1_1::getResponse)
                                    .orElse(null),
                            Objects.requireNonNull(response).getRoRequestId(),
                            response.getCustomExtension().orElse(null),
                            response.getUserId()));
        }
        return notificationEvent;
    }

    private ROGenericNotificationEventDataV1_1 generateNotificationData(
            RemoteOperationResponseV1_1 response, String notificationId) {
        LOGGER.info(
                "Generating ROGenericNotificationEventV1_1 for ro response: {}",
                Utils.logForging(response));
        String responseName = null;
        ROGenericNotificationEventDataV1_1 notificationData = null;
        ROGenericNotificationEventDataV1_1.Status status = null;

        if (null != response) {
            Optional<Object> customExtension = response.getCustomExtension();
            if (customExtension.isPresent()) {
                GenericCustomExtension customdata = (GenericCustomExtension) customExtension.get();
                responseName = (String) customdata.getCustomData().get(RESPONSE);
                LOGGER.info("For Notification Id :{} response name :{}", notificationId, responseName);
            } else {
                responseName = response.getResponse().toString();
            }
            if (null != notificationStatusMapping.get(notificationId)) {
                status =
                        ROGenericNotificationEventDataV1_1.Status.valueOf(
                                notificationStatusMapping.get(notificationId).toUpperCase());
            }
            notificationData =
                    new ROGenericNotificationEventDataV1_1(
                            response.getRoRequestId(), responseName, status, notificationId);
        }

        return notificationData;
    }
}
