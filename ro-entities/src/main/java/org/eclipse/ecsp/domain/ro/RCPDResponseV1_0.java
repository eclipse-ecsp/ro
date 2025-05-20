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

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.eclipse.ecsp.annotations.EventMapping;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.entities.AbstractEventData;
import org.eclipse.ecsp.services.constants.EventAttribute;

/**
 * RCPD Response V1_0 data.
 */
@JsonInclude(Include.NON_NULL)
@JsonFilter(EventAttribute.RO_RESPONSE_FILTER)
@EventMapping(id = Constants.RCPDRESPONSE, version = Version.V1_0)
public class RCPDResponseV1_0 extends AbstractEventData {

    private static final long serialVersionUID = 5849278409016496232L;

    private Response response;

    private String rcpdRequestId;

    public String getRcpdRequestId() {
        return rcpdRequestId;
    }

    public void setRcpdRequestId(String rcpdRequestId) {
        this.rcpdRequestId = rcpdRequestId;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "RemoteOperationResponseV1_1 [response=" + response + ", rcpdRequestId=" + rcpdRequestId + "]";
    }

    /**
     * Response values.
     */
    public enum Response {

        CUSTOM_EXTENSION(Constants.CUSTOM_EXTENSION),
        MOVING_VEHICLE(Constants.MOVING_VEHICLE),
        FAIL_VEHICLE_NOT_CONNECTED(Constants.FAIL_VEHICLE_NOT_CONNECTED),
        FAIL_DELIVERY_RETRYING(Constants.FAIL_DELIVERY_RETRYING),
        FAIL_MESSAGE_DELIVERY_TIMED_OUT(Constants.FAIL_MESSAGE_DELIVERY_TIMED_OUT),
        SUCCESS(Constants.SUCCESS),
        RESPONSE_NOT_AVAILABLE(Constants.RCPD_RESPONSE_NOT_AVAILABLE),
        TIME_OUT(Constants.TIME_OUT),
        USUAL_FAILURE(Constants.USUAL_FAILURE);

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
