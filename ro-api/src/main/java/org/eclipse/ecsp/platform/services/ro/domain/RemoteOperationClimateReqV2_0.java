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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.ecsp.entities.EventData;

/**
 * Remote Climate request version 2.
 *
 * @author Arnold
 */
@Setter
@Getter
@JsonInclude(Include.NON_NULL)
public class RemoteOperationClimateReqV2_0 extends RemoteRequest implements EventData {

    @NotNull
    private State state;

    private Integer temperature;

    private TemperatureUnit temperatureUnit;

    @Min(0)
    @Max(100)
    private Integer fanSpeed;

    private Double targetTemperature;

    private Integer timeoutForAfterTemperature;

    private Integer timeOutPreTrip;

    @Override
    public String toString() {
        return "RemoteClimateRequest_v2{"
                + "state=" + state
                + ", interiorTemperature=" + temperature
                + ", temperatureUOM=" + temperatureUnit
                + ", fanSpeed=" + fanSpeed
                + ", targetTemp=" + targetTemperature
                + ", timeoutAfterTemp=" + timeoutForAfterTemperature
                + ", preTripTimeout=" + timeOutPreTrip
                + ", roRequestId=" + getRoRequestId()
                + ", customExtension=" + getCustomExtension()
                + ", mfaCode=" + getMfaCode()
                + '}';
    }

    /**
     * State and Description.
     * <br>
     * ON 0 On
     * <br>
     * OFF 1 Off
     * <br>
     * COMFORT_AUTO_ON_MODE_DISABLED 2 COMFORT_AUTO_ON_MODE_DISABLED
     * <br>
     * COMFORT_AUTO_ON_MODE_START_ALL 3 COMFORT_AUTO_ON_MODE_START_ALL
     *
     * @author Arnold
     */
    public enum State {

        ON("ON"),
        OFF("OFF"),
        COMFORT_AUTO_ON_MODE_DISABLED("COMFORT_AUTO_ON_MODE_DISABLED"),
        COMFORT_AUTO_ON_MODE_START_ALL("COMFORT_AUTO_ON_MODE_START_ALL");

        private final String value;

        private State(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

    }

    /**
     * Temperature Eom enum.
     * <br>
     * <br>
     * Temperature UOM
     * <br>
     * CELSIUS 0 CELSIUS
     * <br>
     * FAHRENHEIT 1 FAHRENHEIT
     *
     * @author Arnold
     */
    public enum TemperatureUnit {

        Celsius("Celsius"),
        Fahrenheit("Fahrenheit");

        private final String value;

        private TemperatureUnit(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

    }
}
