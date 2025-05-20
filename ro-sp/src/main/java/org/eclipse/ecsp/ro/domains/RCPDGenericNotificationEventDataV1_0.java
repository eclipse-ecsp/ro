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

package org.eclipse.ecsp.ro.domains;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.eclipse.ecsp.annotations.EventMapping;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.AbstractEventData;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.services.constants.EventAttribute;

/**
 * RCPDGenericNotificationEventData V1_0 class.
 */
@JsonInclude(Include.NON_NULL)
@JsonFilter(EventAttribute.RESPONSE_LIST)
@EventMapping(id = Constants.GENERICNOTIFICATIONEVENT, version = Version.V1_0)
public class RCPDGenericNotificationEventDataV1_0 extends AbstractEventData {

    private static final long serialVersionUID = -7196034358237297440L;
    private String response;
    private String rcpdRequestId;
    private String notificationId;

    /**
     * Default constructor.
     */
    public RCPDGenericNotificationEventDataV1_0() {
        super();
    }

    /**
     * Constructor with parameters.
     */
    public RCPDGenericNotificationEventDataV1_0(
            String rcpdRequestId, String responseName, String notificationId) {
        super();
        this.setRcpdRequestId(rcpdRequestId);
        this.setResponse(responseName);
        this.setNotificationId(notificationId);
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getRcpdRequestId() {
        return rcpdRequestId;
    }

    public void setRcpdRequestId(String roRequestId) {
        this.rcpdRequestId = roRequestId;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    @Override
    public String toString() {
        return "RCPDGenericNotificationEventDataV1_0 "
                + "[notificationId="
                + notificationId
                + "response"
                + response
                + "rcpdRequestId:"
                + rcpdRequestId
                + "]";
    }
}
