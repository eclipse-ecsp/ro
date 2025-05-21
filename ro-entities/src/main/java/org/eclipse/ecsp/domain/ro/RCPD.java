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
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import dev.morphia.annotations.Property;
import dev.morphia.utils.IndexType;
import org.bson.types.ObjectId;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.entities.AuditableIgniteEntity;
import org.eclipse.ecsp.entities.IgniteEntity;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.services.constants.EventAttribute;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * RCPD entity.
 */
@Entity(Constants.RCPD)
@JsonInclude(Include.NON_NULL)
@JsonFilter(EventAttribute.EVENT_FILTER)
@Indexes({
    @Index(fields = @Field(value = "rcpdEvent.requestId", type = IndexType.ASC), 
            options = @IndexOptions(disableValidation = true)),
    @Index(fields = { @Field(value = "rcpdEvent.timestamp", type = IndexType.ASC),
        @Field(value = "_id", type = IndexType.ASC) }, 
        options = @IndexOptions(name = "timestamp.id.index", disableValidation = true)) })
public class RCPD implements IgniteEntity, AuditableIgniteEntity {

    private Version schemaVersion;

    @Property(concreteClass = IgniteEventImpl.class)
    private IgniteEvent rcpdEvent;

    private LocalDateTime lastUpdatedTime;

    @Id
    private ObjectId id;

    /**
     * RCPD response list.
     */
    private List<IgniteEvent> rcpdResponseList = new ArrayList<IgniteEvent>();

    public List<IgniteEvent> getRcpdResponseList() {
        return rcpdResponseList;
    }

    public void setRcpdResponseList(List<IgniteEvent> rcpdResponseList) {
        this.rcpdResponseList = rcpdResponseList;
    }

    public IgniteEvent getRcpdEvent() {
        return rcpdEvent;
    }

    public void setRcpdEvent(IgniteEvent rcpdEvent) {
        this.rcpdEvent = rcpdEvent;
    }

    @Override
    public Version getSchemaVersion() {
        return this.schemaVersion;
    }

    @Override
    public void setSchemaVersion(Version schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    @Override
    public LocalDateTime getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    @Override
    public void setLastUpdatedTime(LocalDateTime lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;

    }

    @Override
    public String toString() {
        return "RCPD [schemaVersion=" + schemaVersion + ", rcpdEvent=" + rcpdEvent + ", lastUpdatedTime="
                + lastUpdatedTime + ", id=" + id + ", roResponseList=" + rcpdResponseList + "]";
    }

}
