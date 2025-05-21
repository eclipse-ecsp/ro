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

/**
 * RemoteOperationWindowReq class.
 *
 * @author midnani
 */
@Setter
@Getter
abstract class RemoteOperationWindowReq extends RemoteRequest {

    private State state;
    private Integer duration;
    private Integer percent;

    @Override
    public String toString() {
        return "RemoteOperationWindowReq ["
                + "state=" + state
                + ", duration=" + duration
                + ", percent=" + percent
                + ", roRequestId=" + getRoRequestId()
                + ", customExtention=" + getCustomExtension() + "]";
    }

    /**
     * State enum.
     * <br>
     * <br>
     * State and Description
     * <br>
     * OPENED 0 Open Window
     * <br>
     * CLOSED 1 Close Window
     * <br>
     * PARTIAL_OPENED 2 Partially Open Window
     *
     * @author midnani
     */
    public enum State {

        OPENED("OPENED"),
        CLOSED("CLOSED"),
        PARTIAL_OPENED("PARTIAL_OPENED");

        private final String value;

        private State(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

    }

}
