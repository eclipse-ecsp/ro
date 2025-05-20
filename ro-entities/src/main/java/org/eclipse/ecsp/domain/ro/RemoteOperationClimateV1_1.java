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
import static org.eclipse.ecsp.domain.ro.constant.Constants.THIRTY_ONE;

/**
 * Enum values for RemoteOperationClimate event.
 */
@EventMapping(id = Constants.REMOTEOPERATIONCLIMATE, version = Version.V1_1)
public class RemoteOperationClimateV1_1 extends AbstractRoEventData {

    private static final long serialVersionUID = -3975646048224960472L;
    private State state;
    private AcState acState;
    private TemperatureUnit temperatureUnit;
    private Integer duration;
    private Integer temperature;
    private Integer fanSpeed;
    private String roRequestId;

    /**
     * State values.
     */
    public enum State {
        ON(Constants.ON),
        OFF(Constants.OFF),
        AUTO(Constants.AUTO),
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

    /**
     * AcState values.
     */
    public enum AcState {

        ON("ON"),
        OFF("OFF"),
        CUSTOM_EXTENSION("CUSTOM_EXTENSION");

        private String value;

        AcState(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    /**
     * Temperature unit values.
     */
    public enum TemperatureUnit {

        CELSIUS("CELSIUS"),
        FAHRENHEIT("FAHRENHEIT"),
        CUSTOM_EXTENSION("CUSTOM_EXTENSION");

        private String value;

        TemperatureUnit(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public AcState getAcState() {
        return acState;
    }

    public void setAcState(AcState acState) {
        this.acState = acState;
    }

    public TemperatureUnit getTemperatureUnit() {
        return temperatureUnit;
    }

    public void setTemperatureUnit(TemperatureUnit temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getTemperature() {
        return temperature;
    }

    public void setTemperature(Integer temperature) {
        this.temperature = temperature;
    }

    public Integer getFanSpeed() {
        return fanSpeed;
    }

    public void setFanSpeed(Integer fanSpeed) {
        this.fanSpeed = fanSpeed;
    }

    public String getRoRequestId() {
        return roRequestId;
    }

    public void setRoRequestId(String roRequestId) {
        this.roRequestId = roRequestId;
    }

    @Override
    public String toString() {
        return "RemoteOperationClimateV1_1 ["
                + "state=" + state
                + ", acState=" + acState
                + ", temperatureUnit=" + temperatureUnit
                + ", duration=" + duration
                + ", temperature=" + temperature
                + ", fanSpeed=" + fanSpeed
                + ", roRequestId=" + roRequestId
                + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RemoteOperationClimateV1_1 that = (RemoteOperationClimateV1_1) obj;
        return state == that.state
                && acState == that.acState
                && temperatureUnit == that.temperatureUnit
                && duration.equals(that.duration)
                && temperature.equals(that.temperature)
                && fanSpeed.equals(that.fanSpeed)
                && roRequestId.equals(that.roRequestId);
    }

    @Override
    public int hashCode() {
        int result = state.hashCode();
        result = THIRTY_ONE * result + acState.hashCode();
        result = THIRTY_ONE * result + temperatureUnit.hashCode();
        result = THIRTY_ONE * result + duration.hashCode();
        result = THIRTY_ONE * result + temperature.hashCode();
        result = THIRTY_ONE * result + fanSpeed.hashCode();
        result = THIRTY_ONE * result + roRequestId.hashCode();
        return result;
    }

}
