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

import net.sf.ehcache.Element;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.ro.utils.CacheUtil;
import org.eclipse.ecsp.ro.utils.CachedKeyUtil;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * Notification cache service.
 */
@Component
public class NotificationCacheService {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(NotificationCacheService.class);

    @Autowired
    private CacheUtil cacheUtil;

    /**
     * Persist configuration map into cache.
     *
     * @param value              the {@link IgniteEvent}
     * @param stockingRuleConfig the stocking rule configuration
     */
    public void persistConfigMapIntoCache(IgniteEvent value, Map<String, String> stockingRuleConfig) {
        LOGGER.debug(
                "persisting to ehcache ,stockingRuleConfig: {}, vin: {}",
                stockingRuleConfig,
                value.getVehicleId());
        Element entityElementToCache =
                new Element(CachedKeyUtil.getRONotificationMappingKey(value), stockingRuleConfig);
        cacheUtil.getCache(Constants.RO_CACHE_NAME).put(entityElementToCache);
    }

    /**
     * Get notification mapping from cache.
     *
     * @param value the {@link IgniteEvent}
     */
    public Map<String, String> getNotificationMappingFromCache(IgniteEvent value) {
        Element roNotificationMappingCacheElement =
                cacheUtil
                        .getCache(Constants.RO_CACHE_NAME)
                        .get(CachedKeyUtil.getRONotificationMappingKey(value));
        Map<String, String> roNotificationMapping = null;
        if (null != roNotificationMappingCacheElement
                && !roNotificationMappingCacheElement.isExpired()) {
            roNotificationMapping =
                    (Map<String, String>) roNotificationMappingCacheElement.getObjectValue();
        }
        return roNotificationMapping;
    }
}
