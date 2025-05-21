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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.eclipse.ecsp.annotations.EventMapping;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.entities.AbstractEventData;
import org.eclipse.ecsp.services.constants.EventAttribute;

/**
 * RCPD Request V1_0 data.
 */
@JsonInclude(Include.NON_NULL)
@JsonFilter(EventAttribute.RO_RESPONSE_FILTER)
@EventMapping(id = Constants.RCPDREQUEST, version = Version.V1_0)
public class RCPDRequestV1_0 extends AbstractEventData {

    private static final long serialVersionUID = 8593412459736292876L;
    private String origin;
    private String userId;
    private String rcpdRequestId;
    @JsonIgnore
    private String scheduleRequestId;

    public String getScheduleRequestId() {
        return scheduleRequestId;
    }

    public void setScheduleRequestId(String scheduleRequestId) {
        this.scheduleRequestId = scheduleRequestId;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRcpdRequestId() {
        return rcpdRequestId;
    }

    public void setRcpdRequestId(String rcpdRequestId) {
        this.rcpdRequestId = rcpdRequestId;
    }

    @Override
    public String toString() {
        return "RCPDRequestV1_0 [origin=" + origin + ", rcpdRequestId=" + rcpdRequestId + "scheduleRequestId="
                + scheduleRequestId + "]";
    }

}
