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

package org.eclipse.ecsp.ro.notification;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.ro.utils.StockingRuleConfigUtil;
import org.eclipse.ecsp.ro.utils.Utils;
import org.eclipse.ecsp.services.utils.SettingsManagerClient;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Stocking Rule Notification resolver implementation of NotificationResolver.
 *
 * @see NotificationResolver
 */
@ConditionalOnProperty(prefix = "stockingRule", name = "enable", havingValue = "true")
@Component
public class StockingRuleNotificationResolver implements NotificationResolver {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(StockingRuleNotificationResolver.class);

    @Value("#{${notification.responsemessage.notificationid.mapping}}")
    private Map<String, String> localNotificationIdMapping;

    @Value("#{${notification.ext.mapping}}")
    private Map<String, Map<String, String>> notificationIdExtMap;

    @Autowired
    private SettingsManagerClient settingsManagerClient;

    @NotBlank
    @Value("${settings.object.json.path}")
    private String settingsJsonPath;

    @NotBlank
    @Value("${stockingRule.key.name}")
    private String StockingRuleKeyName;

    @Autowired
    private NotificationCacheService notificationCacheService;

    @Value("#{'${stockingRule.config.events}'.split(',')}")
    private List<String> roEvents;

    @Override
    public String get(IgniteEvent event, String eventId, String mappingIdentifier) {
        LOGGER.debug(event, "StockingRuleNotificationIDResolver get logical start");
        LOGGER.debug(
                "roEvents: {} ,eventId: {}", Utils.logForging(roEvents), Utils.logForging(eventId));

        String cachedSetMngMappingName = null;
        if (roEvents.contains(eventId)) {
            Map<String, String> cachedNotificationMapping =
                    notificationCacheService.getNotificationMappingFromCache(event);
            LOGGER.debug("stocking rules from cache {}", cachedNotificationMapping);
            if (null != cachedNotificationMapping
                    && !CollectionUtils.isEmpty(cachedNotificationMapping)) {
                cachedSetMngMappingName = cachedNotificationMapping.get(StockingRuleKeyName);
                LOGGER.debug(
                        "get cachedSetMngMappingName: {} from memory cache for vin: {}",
                        cachedSetMngMappingName,
                        event.getVehicleId());
            } else {
                Map<String, String> stockingRuleConfig = getNotificationMappingFromSettingsManager(event);
                LOGGER.debug("stocking rules from settingsManager {}", stockingRuleConfig);
                cachedSetMngMappingName =
                        Objects.nonNull(stockingRuleConfig)
                                ? stockingRuleConfig.get(StockingRuleKeyName)
                                : null;
                LOGGER.debug(
                        "get mappingName: {} from settingsManager for vin: {}",
                        cachedSetMngMappingName,
                        event.getVehicleId());
            }
        } else {
            LOGGER.debug("eventId: {} is using default notification config", Utils.logForging(eventId));
        }
        LOGGER.debug("cached SettingManager mapping name: {}", cachedSetMngMappingName);
        return (null != cachedSetMngMappingName)
                ? notificationIdExtMap.get(cachedSetMngMappingName).get(mappingIdentifier)
                : localNotificationIdMapping.get(mappingIdentifier);
    }

    private Map<String, String> getNotificationMappingFromSettingsManager(IgniteEvent value) {
        Map<String, String> stockingRuleConfig = invokeSettingsManagerApi(value);
        if (null != stockingRuleConfig && !CollectionUtils.isEmpty(stockingRuleConfig)) {
            notificationCacheService.persistConfigMapIntoCache(value, stockingRuleConfig);
        } else {
            LOGGER.debug(
                    "No configured notification record found in settingsManagerDB for the vehicle :{}",
                    value.getVehicleId());
        }
        return stockingRuleConfig;
    }

    private Map<String, String> invokeSettingsManagerApi(IgniteEvent value) {
        Map<String, Object> smConfigurationObject =
                settingsManagerClient.getSettingsManagerConfigurationObject(
                        Constants.UNKNOWN,
                        value.getVehicleId(),
                        Constants.UPDATE_RO_NOTIFICATION_MAPPING,
                        settingsJsonPath);
        LOGGER.debug(
                "Configuration Object received after settingsManagerClient call is :{}",
                smConfigurationObject);
        Map<String, String> stockingRuleConfig =
                StockingRuleConfigUtil.getStockingRuleConfig(smConfigurationObject);
        LOGGER.debug(
                "Configuration Messages received from configuration object is :{}", stockingRuleConfig);
        return stockingRuleConfig;
    }
}
