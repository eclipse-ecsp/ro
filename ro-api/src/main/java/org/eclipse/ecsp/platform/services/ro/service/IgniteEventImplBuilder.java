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

package org.eclipse.ecsp.platform.services.ro.service;

import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.EventData;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.entities.UserContext;
import java.util.List;
import java.util.Objects;

/**
 * Builder class for {@link IgniteEventImplBuilder}.
 */
public class IgniteEventImplBuilder {

    protected String eventId;
    protected Version version;
    protected long timestamp;
    protected EventData eventData;
    protected String requestId;
    protected String vehicleId;
    private String bizTransactionId;
    private List<UserContext> userContextInfo;
    private String ecuType;

    public IgniteEventImplBuilder withEventId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    public IgniteEventImplBuilder withVersion(Version version) {
        this.version = version;
        return this;
    }

    public IgniteEventImplBuilder withTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public IgniteEventImplBuilder withEventData(EventData eventData) {
        this.eventData = eventData;
        return this;
    }

    public IgniteEventImplBuilder withRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public IgniteEventImplBuilder withVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
        return this;
    }

    public IgniteEventImplBuilder withBizTransactionId(String bizTransactionId) {
        this.bizTransactionId = bizTransactionId;
        return this;
    }

    public IgniteEventImplBuilder withUserContextInfo(List<UserContext> userContextInfo) {
        this.userContextInfo = userContextInfo;
        return this;
    }

    public IgniteEventImplBuilder withEcuType(String ecuType) {
        this.ecuType = ecuType;
        return this;
    }

    /**
     * Build ignite event.
     *
     * @return the ignite event
     */
    public IgniteEventImpl build() {

        if (this.eventId == null) {
            throw new IllegalArgumentException("Null eventId received."
                    + " Ignite Event cannot be created without an eventId");
        }

        IgniteEventImpl igniteEventImpl = new IgniteEventImpl();
        igniteEventImpl.setEventId(this.eventId);
        igniteEventImpl.setEventData(this.eventData);
        igniteEventImpl.setVersion(Objects.requireNonNullElse(this.version, Version.V1_0));
        igniteEventImpl.setRequestId(this.requestId);
        igniteEventImpl.setBizTransactionId(this.bizTransactionId);
        igniteEventImpl.setUserContextInfo(this.userContextInfo);
        igniteEventImpl.setEcuType(this.ecuType);
        igniteEventImpl.setVehicleId(this.vehicleId);
        if (this.timestamp <= 0) {
            igniteEventImpl.setTimestamp(System.currentTimeMillis());
        } else {
            igniteEventImpl.setTimestamp(this.timestamp);
        }
        return igniteEventImpl;
    }

}
