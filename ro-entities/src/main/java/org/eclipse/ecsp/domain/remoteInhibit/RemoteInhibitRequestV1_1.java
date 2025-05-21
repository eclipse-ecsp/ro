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

package org.eclipse.ecsp.domain.remoteInhibit;

import org.eclipse.ecsp.annotations.EventMapping;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.constant.Constants;

/**
 * Remote Inhibit Request V1_1 representation.
 */
@EventMapping(id = Constants.EVENT_ID_REMOTE_INHIBIT_REQUEST, version = Version.V1_1)
public class RemoteInhibitRequestV1_1 extends AbstractRemoteInhibitEventData {

    private static final long serialVersionUID = -8181253986175452786L;
    private CrankInhibit crankInhibit;

    public CrankInhibit getCrankInhibit() {
        return crankInhibit;
    }

    public void setCrankInhibit(CrankInhibit crankInhibit) {
        this.crankInhibit = crankInhibit;
    }

    @Override
    public String toString() {
        return "RemoteInhibitRequestV1_1 [crankInhibit=" + crankInhibit + "]";
    }

    /**
     * Crank Inhibit enum values.
     */
    public static enum CrankInhibit {

        END_INHIBIT("END_INHIBIT"),
        INHIBIT("INHIBIT");

        private String value;

        private CrankInhibit(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

    }

}
