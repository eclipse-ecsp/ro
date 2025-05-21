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
 * Remote Door Request Class.
 *
 * @author midnani
 */
@Setter
@Getter
abstract class RemoteOperationDoorReq extends RemoteRequest implements EventData {

    private State state;

    @Override
    public String toString() {
        return "RemoteOperationDoorReq [state=" + state + ", roRequestId=" + getRoRequestId() + ", customExtension="
                + getCustomExtension() + ", mfaCode=" + getMfaCode() + "]";
    }

    /**
     * State Enum.
     * <br/>
     * State and Description
     * <br/>
     * LOCKED - 0 - Lock All Doors / Lock Driver Door Only
     * <br/>
     * UNLOCKED - 1 - Unlock All Doors / Unlock Driver Door Only
     *
     * @author midnani
     */

    public enum State {

        LOCKED("LOCKED"),
        UNLOCKED("UNLOCKED");

        private final String value;

        private State(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

    }

}
