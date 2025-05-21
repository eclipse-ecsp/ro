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

package org.eclipse.ecsp.platform.services.ro.configure;

import org.eclipse.ecsp.platform.services.ro.service.DeviceMessageFailuresEventFiler;
import org.eclipse.ecsp.platform.services.ro.service.GeneralEventFiler;
import org.eclipse.ecsp.platform.services.ro.service.ROEventFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class {@link ROStatusHistoryFilterConfigure} to configure {@link ROEventFilter}.
 *
 * @author arnold
 */
@Configuration
public class ROStatusHistoryFilterConfigure {

    @Bean("dmMessageFailuresFilter")
    @ConditionalOnProperty(value = "filter.deviceMessage.failures")
    public ROEventFilter dmMessageFailuresFilter() {
        return new DeviceMessageFailuresEventFiler();
    }

    @Bean("generalEventFilter")
    @ConditionalOnProperty(value = "filter.general.event")
    public ROEventFilter generalEventFilter() {
        return new GeneralEventFiler();
    }

}
