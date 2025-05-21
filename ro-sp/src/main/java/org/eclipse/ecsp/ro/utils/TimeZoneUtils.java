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

import net.iakovlev.timeshape.TimeZoneEngine;
import org.springframework.util.CollectionUtils;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for Timezone.
 */
public abstract class TimeZoneUtils {

    public static final String TIME_PATTERN = "yyyy/MM/dd HH:mm:ss";

    private static TimeZoneEngine engine = TimeZoneEngine.initialize();

    private TimeZoneUtils() {
        throw new UnsupportedOperationException("Cannot create an instance of TimeZoneUtils");
    }

    /**
     * Get zone id by latitude and longitude location.
     *
     * @param latitude  latitude
     * @param longitude longitude
     * @return zone id
     */
    public static ZoneId getZoneIdByLocation(double latitude, double longitude) {
        List<ZoneId> zoneIds = engine.queryAll(latitude, longitude);
        if (CollectionUtils.isEmpty(zoneIds)) {
            return null;
        }
        return zoneIds.get(0);
    }

    /**
     * Get UTC timestamp.
     *
     * @param zoneId  zone id
     * @param timeStr time in string
     * @param pattern date pattern
     * @return utc timestamp
     */
    public static long getUTCTimestamp(ZoneId zoneId, String timeStr, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime localDateTime = LocalDateTime.parse(timeStr, formatter);
        long second = ZonedDateTime.of(localDateTime, zoneId).toEpochSecond();
        return second;
    }

    public static long getCurrentUTCTimestamp() {
        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.systemDefault());
        return localDateTime.toEpochSecond(ZoneOffset.UTC);
    }
}
