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

package org.eclipse.ecsp.ro.processor.strategy.impl;

import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.ro.processor.strategy.SubStreamProcessor;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

/**
 * Factory for substream processors.
 */
@Component
public class SubStreamProcessorFactory {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(SubStreamProcessorFactory.class);

    @Autowired
    private Map<String, SubStreamProcessor> subStreamProcessorMap;

    private List<String> roEventsMapping = Constants.getRoEvents();

    /**
     * Getter for substream processor for the event id.
     *
     * @param eventID event id
     * @return sub stream processor
     */
    public SubStreamProcessor get(String eventID) {
        LOGGER.debug("Fetching configured sub-stream processor for eventID: {}", eventID);
        if (roEventsMapping.contains(eventID)) {
            return subStreamProcessorMap.get(Constants.REMOTE_OPERATION_REQUEST_EVENT_ID);
        }
        return subStreamProcessorMap.get(eventID);
    }
}
