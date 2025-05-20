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

import org.eclipse.ecsp.domain.ro.constant.Constants;

/**
 * Remote Operation Types.
 */
public enum RemoteOperationType {

    REMOTE_OPERATION_CLIMATE(Constants.REMOTEOPERATIONCLIMATE),
    REMOTE_OPERATION_ENGINE(Constants.REMOTEOPERATIONENGINE),
    REMOTE_OPERATION_LIGHTS(Constants.REMOTEOPERATIONLIGHTS),
    REMOTE_OPERATION_HORN(Constants.REMOTEOPERATIONHORN),
    REMOTE_OPERATION_ALARM(Constants.REMOTEOPERATIONALARM),
    REMOTE_OPERATION_DOORS(Constants.REMOTEOPERATIONDOORS),
    REMOTE_OPERATION_DRIVERDOOR(Constants.REMOTEOPERATIONDRIVERDOOR),
    REMOTE_OPERATION_HOOD(Constants.REMOTEOPERATIONHOOD),
    REMOTE_OPERATION_LIFTGATE(Constants.REMOTEOPERATIONLIFTGATE),
    REMOTE_OPERATION_TRUNK(Constants.REMOTEOPERATIONTRUNK),
    REMOTE_OPERATION_WINDOWS(Constants.REMOTEOPERATIONWINDOWS),
    REMOTE_OPERATION_DRIVERWINDOW(Constants.REMOTEOPERATIONDRIVERWINDOW);

    private String value;

    RemoteOperationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
