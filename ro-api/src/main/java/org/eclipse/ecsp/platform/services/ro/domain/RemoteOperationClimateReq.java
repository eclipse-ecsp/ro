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
import lombok.Getter;
import lombok.Setter;
import org.eclipse.ecsp.entities.EventData;

/**
 * Remote climate request.
 *
 * @author midnani
 */
@Setter
@Getter
@JsonInclude(Include.NON_NULL)
public class RemoteOperationClimateReq extends RemoteRequest implements EventData {


    private State state;

    private ACState acState;

    private Integer duration;

    private Integer temperature;

    private Integer fanSpeed;

    private TemperatureUnit temperatureUnit;

    @Override
    public String toString() {
        return "RemoteOperationClimateReq ["
                + "state=" + state
                + ", acState=" + acState
                + ", duration=" + duration
                + ", temperature=" + temperature
                + ", fanSpeed=" + fanSpeed
                + ", temperatureUnit=" + temperatureUnit
                + ", roRequestId=" + getRoRequestId()
                + ", customExtension=" + getCustomExtension()
                + ", mfaCode=" + getMfaCode() + "]";
    }

    /**
     * The enum State.
     * <br>
     * State and Description
     * <br>
     * ON 0 <br>
     * OFF 1 <br>
     * AUTO 2 <br>
     *
     * @author midnani
     */
    public enum State {

        /**
         * On state.
         */
        ON("ON"),
        /**
         * Off state.
         */
        OFF("OFF"),
        /**
         * Auto state.
         */
        AUTO("AUTO");

        private final String value;

        private State(String value) {
            this.value = value;
        }

        /**
         * Gets value.
         *
         * @return the value
         */
        public String getValue() {
            return this.value;
        }

    }

    /**
     * The enum Ac state.
     * <br>
     * Values:
     * <br>
     * ON 0 <br>
     * OFF 1 <br>
     *
     * @author midnani
     */
    public enum ACState {

        /**
         * On ac state.
         */
        ON("ON"),
        /**
         * Off ac state.
         */
        OFF("OFF");

        private final String value;

        private ACState(String value) {
            this.value = value;
        }

        /**
         * Gets value.
         *
         * @return the value
         */
        public String getValue() {
            return this.value;
        }

    }

    /**
     * The enum Temperature unit.
     * <br>
     * Values:
     * <br>
     * CELSIUS 0  <br>
     * FAHRENHEIT 1
     *
     * @author midnani
     */
    public enum TemperatureUnit {

        /**
         * CELSIUS temperature unit.
         */
        CELSIUS("CELSIUS"),
        /**
         * FAHRENHEIT temperature unit.
         */
        FAHRENHEIT("FAHRENHEIT");

        private final String value;

        private TemperatureUnit(String value) {
            this.value = value;
        }

        /**
         * Gets value.
         *
         * @return the value
         */
        public String getValue() {
            return this.value;
        }

    }

}
