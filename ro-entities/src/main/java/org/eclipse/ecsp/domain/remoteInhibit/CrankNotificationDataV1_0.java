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
 * Representation of Crank notification data.
 */
@EventMapping(id = Constants.EVENT_ID_CRANK_NOTIFICATION_DATA, version = Version.V1_0)
public class CrankNotificationDataV1_0 extends AbstractRemoteInhibitEventData {

    private static final long serialVersionUID = -2333308719065801506L;

    private boolean crankAttempted;
    private Double longitude;
    private Double latitude;
    private Double bearing;
    private Double altitude;
    private Integer horPosError;

    public boolean isCrankAttempted() {
        return crankAttempted;
    }

    public void setCrankAttempted(boolean crankAttempted) {
        this.crankAttempted = crankAttempted;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getBearing() {
        return bearing;
    }

    public void setBearing(Double bearing) {
        this.bearing = bearing;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Integer getHorPosError() {
        return horPosError;
    }

    public void setHorPosError(Integer horPosError) {
        this.horPosError = horPosError;
    }

    @Override
    public String toString() {
        return "CrankNotificationDataV1_0 [crankAttempted=" + crankAttempted
                + ", longitude=" + longitude
                + ", latitude=" + latitude
                + ", bearing=" + bearing
                + ", altitude=" + altitude
                + ", horPosError=" + horPosError + "]";
    }

}
