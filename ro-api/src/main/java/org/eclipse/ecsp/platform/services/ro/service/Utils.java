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

package org.eclipse.ecsp.platform.services.ro.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.EventData;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.platform.services.ro.constant.NumericConstants;
import org.eclipse.ecsp.utils.ApiUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Comparator;
import java.util.Map;

/**
 * Utility Class.
 */
@Component
public class Utils {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(RCPDService.class);


    @Autowired
    ApiUtils apiUtils;

    public static Comparator<IgniteEvent> getIgniteEventTimeStampComparator() {
        return ((e1, e2) -> (e2.getTimestamp() > e1.getTimestamp()) ? 1 : NumericConstants.MINUS_ONE);
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

    /**
     * Create ignite event.
     *
     * @param version   the version
     * @param eventId   the event id
     * @param vehicleId the vehicle id
     * @param eventData the event data
     * @param requestId the request id
     * @param sessionId the session id
     * @param userId    the user id
     * @return the ignite event
     */
    public IgniteEvent createIgniteEvent(Version version, String eventId, String vehicleId,
                                         EventData eventData, String requestId, String sessionId, String userId) {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventData(eventData);
        igniteEvent.setEventId(eventId);
        igniteEvent.setVersion(version);
        igniteEvent.setVehicleId(vehicleId);
        igniteEvent.setRequestId(requestId);
        igniteEvent.setBizTransactionId(sessionId);
        igniteEvent.setTimestamp(System.currentTimeMillis());

        igniteEvent.setUserContextInfo(apiUtils.getUserContext(userId));

        return igniteEvent;
    }

    /**
     * Convert map to json string .
     *
     * @param map the map
     * @return the string
     */
    public static String convertMapToJSonString(Map<String, Object> map) {

        ObjectMapper mapper = new ObjectMapper();
        String json = "";

        // convert map to JSON string
        try {
            json = mapper.writeValueAsString(map);
            LOGGER.debug("Map value as Json string : " + Utils.logForging(json));
        } catch (JsonProcessingException e) {
            LOGGER.debug("Error while while converting map to string");
        }
        return json;
    }
}
