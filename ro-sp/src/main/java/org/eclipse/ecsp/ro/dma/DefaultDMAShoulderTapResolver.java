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

package org.eclipse.ecsp.ro.dma;

import org.eclipse.ecsp.entities.IgniteEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Default DMA ShoulderTap resolver used by Ignite.
 */
@ConditionalOnProperty(name = "shoulderTapResolver.custom", havingValue = "default")
@Component
public class DefaultDMAShoulderTapResolver implements DMAShoulderTapResolver {

    @Value("${shoulderTap.default:false}")
    private Boolean shoulderTap;

    @Override
    public Boolean isShoulderTap(IgniteEvent event) {
        return shoulderTap;
    }
}
