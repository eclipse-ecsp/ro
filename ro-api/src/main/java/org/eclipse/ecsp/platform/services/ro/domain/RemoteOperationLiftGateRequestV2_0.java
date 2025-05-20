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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.ecsp.entities.EventData;

/**
 * RemoteLiftGateRequest V2 cLass.
 *
 * @author Arnold
 */
@Setter
@Getter
@JsonInclude(Include.NON_NULL)
public class RemoteOperationLiftGateRequestV2_0 extends RemoteRequest implements EventData {

    @NotNull
    private State state;

    @Override
    public String toString() {
        return "RemoteOperationLiftGateRequestV2_0{"
                + "state=" + state
                + ", roRequestId=" + getRoRequestId()
                + ", customExtension=" + getCustomExtension()
                + ", mfaCode=" + getMfaCode()
                + '}';
    }

    /**
     * State Enum.
     * <br>
     * <br>
     * State and Description
     * <br>
     * OPENED 0 OPEN liftgate
     * <br>
     * CLOSED 1 CLOSE liftgate
     * <br>
     * LOCKED 2 LOCK liftgate
     * <br>
     * UNLOCKED 3 UNLOCK liftgate
     *
     * @author Arnold
     */
    public enum State {

        OPENED("OPENED"),
        CLOSED("CLOSED"),
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
