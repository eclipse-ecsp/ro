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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.services.constants.EventAttribute;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link ROEventFilter} filter for general events.
 */
public class GeneralEventFiler implements ROEventFilter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        Map<String, PropertyFilter> filters = new HashMap<>();

        filters.put(EventAttribute.EVENT_FILTER,
                SimpleBeanPropertyFilter.serializeAllExcept(
                        EventAttribute.ID,
                        EventAttribute.TIMEZONE,
                        EventAttribute.SCHEMA_VERSION,
                        EventAttribute.SOURCE_DEVICE_ID,
                        EventAttribute.VEHICLE_ID,
                        EventAttribute.MESSAGE_ID,
                        EventAttribute.CORRELATION_ID,
                        EventAttribute.BIZTRANSACTION_ID,
                        EventAttribute.BENCH_MODE,
                        EventAttribute.RESPONSE_EXPECTED,
                        EventAttribute.DEVICE_DELIVERY_CUTOFF,
                        EventAttribute.DFF_QUALIFIER,
                        EventAttribute.USER_CONTEXT,
                        EventAttribute.LAST_UPDATED_TIME,
                        EventAttribute.DUPLICATE_MESSAGE,
                        EventAttribute.ECU_TYPE,
                        EventAttribute.MQTT_TOPIC));

        filters.put(EventAttribute.RO_RESPONSE_FILTER,
                SimpleBeanPropertyFilter.serializeAllExcept(
                        EventAttribute.ORIGIN,
                        EventAttribute.USERID,
                        EventAttribute.PARTNER_ID,
                        Constants.RO_SCHEDULE_ID,
                        Constants.RO_SCHEDULE_KEY,
                        Constants.RO_FILTER_FIRSTSCHEDULETS,
                        Constants.RO_FILTER_DEPARTURETS));

        filters.put(Constants.RO_NOTIFICATION_LIST,
                SimpleBeanPropertyFilter.serializeAllExcept(
                        EventAttribute.ORIGIN,
                        EventAttribute.USERID,
                        EventAttribute.PARTNER_ID));

        filters.put(Constants.DEVICE_MESSAGE_FAILURES,
                SimpleBeanPropertyFilter.serializeAllExcept(
                        EventAttribute.ORIGIN,
                        EventAttribute.USERID,
                        EventAttribute.PARTNER_ID));

        SimpleFilterProvider filterProvider = new SimpleFilterProvider(filters);

        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.setFilterProvider(filterProvider);
    }

    @Override
    public String filter(Object object) throws JsonProcessingException {
        return MAPPER.writeValueAsString(object);
    }
}
