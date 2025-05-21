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
 * Remote Inhibit Response V1_1 representation.
 */
@EventMapping(id = Constants.EVENT_ID_REMOTE_INHIBIT_RESPONSE, version = Version.V1_1)
public class RemoteInhibitResponseV1_1 extends AbstractRemoteInhibitEventData {

    private static final long serialVersionUID = -8181253986175452786L;
    private Response response;

    public RemoteInhibitResponseV1_1() {

    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "RemoteInhibitResponseV1_1 [response=" + response + ", roRequestId=" + getRoRequestId() + "]";
    }

    /**
     * Crank Inhibit Response enum values.
     */
    public static enum Response {
        SUCCESS(Constants.SUCCESS),
        FAIL(Constants.FAIL),
        FAIL_DELIVERY_RETRYING(Constants.FAIL_DELIVERY_RETRYING),
        FAIL_VEHICLE_NOT_CONNECTED(Constants.FAIL_VEHICLE_NOT_CONNECTED),
        CUSTOM_EXTENSION(Constants.CUSTOM_EXTENSION);

        private String value;

        Response(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

}
