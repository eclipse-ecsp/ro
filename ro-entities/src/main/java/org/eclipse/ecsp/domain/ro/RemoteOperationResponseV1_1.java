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
 * Version 1_1 event representation for RemoteOperationResponse event.
 */
@EventMapping(id = Constants.REMOTEOPERATIONRESPONSE, version = Version.V1_1)
public class RemoteOperationResponseV1_1 extends AbstractRoEventData {

    private static final long serialVersionUID = -4975489717907058939L;
    private Response response;
    private String roRequestId;

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public String getRoRequestId() {
        return roRequestId;
    }

    public void setRoRequestId(String roRequestId) {
        this.roRequestId = roRequestId;
    }

    @Override
    public String toString() {
        return "RemoteOperationResponseV1_1 [response=" + response + ", roRequestId=" + roRequestId + "]";
    }

    /**
     * Responses.
     */
    public static enum Response {
        SUCCESS(Constants.SUCCESS),
        SUCCESS_CONTINUE(Constants.SUCCESS_CONTINUE),
        FAIL(Constants.FAIL),
        FAIL_MESSAGE_DELIVERY_TIMED_OUT(Constants.FAIL_MESSAGE_DELIVERY_TIMED_OUT),
        FAIL_VEHICLE_NOT_CONNECTED(Constants.FAIL_VEHICLE_NOT_CONNECTED),
        FAIL_DELIVERY_RETRYING(Constants.FAIL_DELIVERY_RETRYING),
        RETRYING_DEVICE_DELIVERY_MESSAGE(Constants.RETRYING_DEVICE_DELIVERY_MESSAGE),
        SUCCESS_SCHEDULE_CREATED(Constants.SUCCESS_SCHEDULE_CREATED),
        SUCCESS_SCHEDULE_UPDATED(Constants.SUCCESS_SCHEDULE_UPDATED),
        SUCCESS_SCHEDULE_DELETED(Constants.SUCCESS_SCHEDULE_DELETED),
        FAIL_SCHEDULE_CREATED(Constants.FAIL_SCHEDULE_CREATED),
        FAIL_SCHEDULE_DELETED(Constants.FAIL_SCHEDULE_DELETED),
        FAIL_SCHEDULE_UPDATED(Constants.FAIL_SCHEDULE_UPDATED),
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
