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

package org.eclipse.ecsp.platform.services.ro.domain;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.ecsp.entities.EventData;

/**
 * RemoteOperationEngineReq Class.
 *
 * @author midnani
 */
@Setter
@Getter
public class RemoteOperationEngineReq extends RemoteRequest implements EventData {

    private State state;
    private Integer duration;

    @Override
    public String toString() {
        return "RemoteOperationEngineReq [state=" + state + ", duration=" + duration + ", roRequestId="
                + getRoRequestId() + ", customExtension="
                + getCustomExtension() + ", mfaCode=" + getMfaCode() + "]";
    }

    /**
     * State Enum.
     *
     * <p>State and Description
     * <br>
     * STARTED 0 Execute a Remote Start
     * <br>
     * STOPPED 1 End a Remote Start
     * <br>
     * IGNITION_DISABLED 2 Disable Engine Ignition
     * <br>
     * IGNITION_ENABLED 3 Enable Engine Ignition
     * </p>
     *
     * @author midnani
     */
    public enum State {

        STARTED("STARTED"),
        STOPPED("STOPPED"),
        IGNITION_DISABLED("IGNITION_DISABLED"),
        IGNITION_ENABLED("IGNITION_ENABLED"),
        SLOW_MOVING("SLOW_MOVING");

        private final String value;

        private State(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

    }

}
