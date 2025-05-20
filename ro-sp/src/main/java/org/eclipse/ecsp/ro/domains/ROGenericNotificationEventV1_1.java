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

import org.eclipse.ecsp.annotations.EventMapping;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.RemoteOperationResponseV1_1;
import org.eclipse.ecsp.ro.constants.Constants;

/**
 * ROGenericNotificationEventV1_1.
 *
 * @author Neerajkumar
 */
@EventMapping(id = Constants.GENERICNOTIFICATIONEVENT, version = Version.V1_1)
public class ROGenericNotificationEventV1_1 extends RemoteOperationResponseV1_1 {

    private static final long serialVersionUID = -8184057090011057857L;

    private String notificationId;

    public ROGenericNotificationEventV1_1() {
        super();
    }

    /**
     * Constructor with parameters.
     */
    public ROGenericNotificationEventV1_1(
            String notificationId,
            Response response,
            String roRequestId,
            Object customExtension,
            String userId) {
        super();
        this.notificationId = notificationId;
        this.setCustomExtension(customExtension);
        this.setResponse(response);
        this.setRoRequestId(roRequestId);
        this.setUserId(userId);
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }
}
