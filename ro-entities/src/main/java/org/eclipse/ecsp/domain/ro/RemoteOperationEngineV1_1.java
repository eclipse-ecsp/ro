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
 * Event representation for RemoteOperationEngine event. Version 1_1.
 */
@EventMapping(id = Constants.REMOTEOPERATIONENGINE, version = Version.V1_1)
public class RemoteOperationEngineV1_1 extends AbstractRoEventData {

    private static final long serialVersionUID = -3152890392226416323L;
    private State state;
    private Integer duration;
    private String roRequestId;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getRoRequestId() {
        return roRequestId;
    }

    public void setRoRequestId(String roRequestId) {
        this.roRequestId = roRequestId;
    }

    @Override
    public String toString() {
        return "RemoteOperationEngineV1_1 ["
                + "state=" + state
                + ", duration=" + duration
                + ", roRequestId=" + roRequestId
                + "]";
    }

    /**
     * States.
     */
    public static enum State {
        STARTED(Constants.STARTED),
        STOPPED(Constants.STOPPED),
        SLOW_MOVING(Constants.SLOW_MOVING),
        IGNITION_DISABLED(Constants.IGNITION_DISABLED),
        IGNITION_ENABLED(Constants.IGNITION_ENABLED),
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
