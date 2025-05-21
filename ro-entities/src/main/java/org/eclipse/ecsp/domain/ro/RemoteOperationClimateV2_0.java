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
 * Event representation for RemoteOperationClimate event.
 */
@EventMapping(id = Constants.REMOTEOPERATIONCLIMATE, version = Version.V2_0)
public class RemoteOperationClimateV2_0 extends AbstractRoEventData {

    private static final long serialVersionUID = -3975646048224960472L;
    private State state;
    private Integer fanSpeed;
    private Integer temperature;
    private TemperatureUnit temperatureUnit;
    private Double targetTemperature;
    private Integer timeoutForAfterTemperature;
    private Integer timeOutPreTrip;
    private String roRequestId;

    /**
     * State values.
     */
    public enum State {

        ON(Constants.ON),
        OFF(Constants.OFF),
        COMFORT_AUTO_ON_MODE_DISABLED(Constants.COMFORT_AUTO_ON_MODE_DISABLED),
        COMFORT_AUTO_ON_MODE_START_ALL(Constants.COMFORT_AUTO_ON_MODE_START_ALL),
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
     * Temperature UOM values.
     */
    public enum TemperatureUnit {

        Celsius("Celsius"),
        Fahrenheit("Fahrenheit"),
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

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Integer getFanSpeed() {
        return fanSpeed;
    }

    public void setFanSpeed(Integer fanSpeed) {
        this.fanSpeed = fanSpeed;
    }

    public Integer getTemperature() {
        return temperature;
    }

    public void setTemperature(Integer temperature) {
        this.temperature = temperature;
    }

    public TemperatureUnit getTemperatureUnit() {
        return temperatureUnit;
    }

    public void setTemperatureUnit(TemperatureUnit temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }

    public Double getTargetTemperature() {
        return targetTemperature;
    }

    public void setTargetTemperature(Double targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    public Integer getTimeoutForAfterTemperature() {
        return timeoutForAfterTemperature;
    }

    public void setTimeoutForAfterTemperature(Integer timeoutForAfterTemperature) {
        this.timeoutForAfterTemperature = timeoutForAfterTemperature;
    }

    public Integer getTimeOutPreTrip() {
        return timeOutPreTrip;
    }

    public void setTimeOutPreTrip(Integer timeOutPreTrip) {
        this.timeOutPreTrip = timeOutPreTrip;
    }

    public String getRoRequestId() {
        return roRequestId;
    }

    public void setRoRequestId(String roRequestId) {
        this.roRequestId = roRequestId;
    }

    @Override
    public String toString() {
        return "RemoteOperationClimateV2_0"
                + "{"
                + " state=" + state
                + ", temperature=" + temperature
                + ", temperatureUnit=" + temperatureUnit
                + ", fanSpeed=" + fanSpeed
                + ", targetTemperature=" + targetTemperature
                + ", timeoutForAfterTemperature=" + timeoutForAfterTemperature
                + ", timeOutPreTrip=" + timeOutPreTrip
                + ", roRequestId='" + roRequestId + '\''
                + '}';
    }

    // override equals method
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RemoteOperationClimateV2_0 that = (RemoteOperationClimateV2_0) obj;
        return state == that.state
                && temperature.equals(that.temperature)
                && temperatureUnit == that.temperatureUnit
                && fanSpeed.equals(that.fanSpeed)
                && targetTemperature.equals(that.targetTemperature)
                && timeoutForAfterTemperature.equals(that.timeoutForAfterTemperature)
                && timeOutPreTrip.equals(that.timeOutPreTrip)
                && roRequestId.equals(that.roRequestId);
    }

    // override hashCode method
    @Override
    public int hashCode() {
        int result = state.hashCode();
        result = THIRTY_ONE * result + temperature.hashCode();
        result = THIRTY_ONE * result + temperatureUnit.hashCode();
        result = THIRTY_ONE * result + fanSpeed.hashCode();
        result = THIRTY_ONE * result + targetTemperature.hashCode();
        result = THIRTY_ONE * result + timeoutForAfterTemperature.hashCode();
        result = THIRTY_ONE * result + timeOutPreTrip.hashCode();
        result = THIRTY_ONE * result + roRequestId.hashCode();
        return result;
    }
}