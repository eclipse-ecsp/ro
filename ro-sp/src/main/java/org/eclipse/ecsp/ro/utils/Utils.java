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

import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.EventData;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.entities.UserContext;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import java.util.List;

/**
 * Utility Class.
 */
public abstract class Utils {

    private Utils() {
        throw new UnsupportedOperationException("Cannot instantiate an object for Utils");
    }

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(Utils.class);

    /**
     * Create ignite event .
     *
     * @param vehicleId     the vehicle id
     * @param eventDataData the event data data
     * @param eventId       the event id
     * @param userContexts  the user contexts
     * @return the ignite event
     */
    public static IgniteEventImpl createIgniteEvent(
            String vehicleId, EventData eventDataData, String eventId, List<UserContext> userContexts) {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventData(eventDataData);
        igniteEvent.setEventId(eventId);
        igniteEvent.setVersion(Version.V1_0);
        igniteEvent.setTimestamp(System.currentTimeMillis());
        igniteEvent.setVehicleId(vehicleId);
        if (userContexts != null) {
            igniteEvent.setUserContextInfo(userContexts);
        } else {
            LOGGER.debug("UserContexts not avaialble");
        }
        return igniteEvent;
    }

    /**
     * User Input Encoded Prior Logging.
     */
    public static String logForging(Object input) {
        if (input != null) {
            return input.toString().replace('\t', '_').replace('\n', '_').replace('\r', '_');
        } else {
            return null;
        }
    }
}
