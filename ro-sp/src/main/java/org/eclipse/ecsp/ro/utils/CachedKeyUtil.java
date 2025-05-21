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

import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.ro.constants.Constants;

/**
 * Utility class for Cached Key.
 *
 * @author arnold
 */
public class CachedKeyUtil {

    private CachedKeyUtil() {
        throw new UnsupportedOperationException("Cannot create an object");
    }

    public static String getEngineStatusKey(IgniteEvent value) {

        return Constants.RO_ENGINE_STATUS_PREFIX + value.getVehicleId();
    }

    public static String getROQueueKey(IgniteEvent value) {
        return Constants.RO_QUEUE_PREFIX + value.getVehicleId();
    }

    public static String getRONotificationMappingKey(IgniteEvent value) {
        return Constants.NOTIFICATION_MAPPING + Constants.UNDER_SCORE + value.getVehicleId();
    }
}
