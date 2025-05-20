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
 * {@link RemoteInhibitRequest} class.
 * <br>
 * <br>
 * State and Description
 * <br>
 * CrankInhibit - “END_INHIBIT”| "INHIBIT"
 * <br>
 * EngineSlow - "EXIT_SLOW"| "SLOW"
 *
 * @author pkumar16
 */
@Setter
@Getter
@JsonInclude(Include.NON_NULL)
public class RemoteInhibitRequest extends RemoteRequest implements EventData {

    private CrankInhibit crankInhibit;

    @Override
    public String toString() {
        return "RemoteInhibitRequest [crankInhibit=" + crankInhibit + "]";
    }

    /**
     * The enum Crank inhibit {@link CrankInhibit}.
     */
    public enum CrankInhibit {

        END_INHIBIT("END_INHIBIT"),
        INHIBIT("INHIBIT");

        private final String value;

        private CrankInhibit(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

    }

}
