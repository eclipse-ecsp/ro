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

/**
 * Location Response.
 */
public class LocationResponse {

    private Long timeStamp;
    private Number longitude;
    private Number latitude;
    private Number altitude;
    private Boolean isLocationApprox;
    private Integer bearing;

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Number getLongitude() {
        return longitude;
    }

    public void setLongitude(Number longitude) {
        this.longitude = longitude;
    }

    public Number getLatitude() {
        return latitude;
    }

    public void setLatitude(Number latitude) {
        this.latitude = latitude;
    }

    public Number getAltitude() {
        return altitude;
    }

    public void setAltitude(Number altitude) {
        this.altitude = altitude;
    }

    public Boolean getIsLocationApprox() {
        return isLocationApprox;
    }

    public void setIsLocationApprox(Boolean isLocationApprox) {
        this.isLocationApprox = isLocationApprox;
    }

    public Integer getBearing() {
        return bearing;
    }

    public void setBearing(Integer bearing) {
        this.bearing = bearing;
    }

    @Override
    public String toString() {
        return "LocationResponse ["
                + "timeStamp="
                + timeStamp
                + ", longitude="
                + longitude
                + ", latitude="
                + latitude
                + ", altitude="
                + altitude
                + ", isLocationApprox="
                + isLocationApprox
                + ", bearing="
                + bearing
                + "]";
    }
}
