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

import org.eclipse.ecsp.entities.AbstractEventData;

/**
 * ROGenericNotificationEventDataV1_1.
 */
public class ROGenericNotificationEventDataV1_1 extends AbstractEventData {

    private static final long serialVersionUID = 6088352349094190465L;
    private Status status;
    private String response;
    private String roRequestId;
    private String notificationId;

    public ROGenericNotificationEventDataV1_1() {
        super();
    }

    /**
     * Constructor with parameters.
     */
    public ROGenericNotificationEventDataV1_1(
            String roRequestId, String responseName, Status status, String notificationId) {
        super();
        this.setRoRequestId(roRequestId);
        this.setStatus(status);
        this.setResponse(responseName);
        this.setNotificationId(notificationId);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getRoRequestId() {
        return roRequestId;
    }

    public void setRoRequestId(String roRequestId) {
        this.roRequestId = roRequestId;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    /**
     * Status.
     */
    public enum Status {
        SUCCESS("success"),
        SUCCESS_CONTINUE("success_continue"),
        FAIL("fail");
        private final String value;

        Status(String value) {
            this.value = value;
        }
    }
}
