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

package org.eclipse.ecsp.domain.ro;

import org.eclipse.ecsp.annotations.EventMapping;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.constant.Constants;

/**
 * Version 1_1 event representation for RemoteOperationHood event.
 */
@EventMapping(id = Constants.REMOTEOPERATIONHOOD, version = Version.V1_1)
public class RemoteOperationHoodV1_1 extends AbstractRoEventData {

    private static final long serialVersionUID = -1899659969161733233L;
    private State state;
    private String roRequestId;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getRoRequestId() {
        return roRequestId;
    }

    public void setRoRequestId(String roRequestId) {
        this.roRequestId = roRequestId;
    }

    @Override
    public String toString() {
        return "RemoteOperationHoodV1_1 [state=" + state + ", roRequestId=" + roRequestId + "]";
    }

    /**
     * States.
     */
    public static enum State {
        OPENED(Constants.OPENED),
        CLOSED(Constants.CLOSED),
        LOCKED(Constants.LOCKED),
        UNLOCKED(Constants.UNLOCKED),
        CUSTOM_EXTENSION(Constants.CUSTOM_EXTENSION);
        private String value;

        State(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

}
