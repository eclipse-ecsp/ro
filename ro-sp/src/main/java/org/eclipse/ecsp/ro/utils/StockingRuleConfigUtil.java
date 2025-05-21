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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.eclipse.ecsp.analytics.stream.base.utils.JsonUtils;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for Stocking rule configuration.
 */
public class StockingRuleConfigUtil {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(StockingRuleConfigUtil.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    private StockingRuleConfigUtil() {
        throw new UnsupportedOperationException("Cannot create an instance");
    }

    /**
     * Getter for stocking rule configurations.
     *
     * @param configurationObject configuration object
     * @return stocking rule configuration from the configuration object
     */
    public static Map<String, String> getStockingRuleConfig(Map<String, Object> configurationObject) {
        try {
            String messages =
                    JsonUtils.getObjectValueAsString(
                            configurationObject.get(Constants.STOCKING_RULE_CONFIGURATIONOBJECT));
            LOGGER.debug("Messages received from configuration object is :{}", messages);
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, String.class);
            return objectMapper.readValue(messages, mapType);
        } catch (Exception e) {
            LOGGER.error(
                    "Error while parsing configurationObject JSON to notificationMapping :{}",
                    e.getMessage());
        }
        return new HashMap<>(0);
    }
}
