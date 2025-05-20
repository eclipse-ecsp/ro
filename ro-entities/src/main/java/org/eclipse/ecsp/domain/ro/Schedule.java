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

import dev.morphia.annotations.Entity;

/**
 * Schedule entity class.
 */
@Entity
public class Schedule {
    /**
     * Initial delay time in milliseconds for the first schedule.
     */
    private long firstScheduleTs;

    /**
     * Recurrence type to be used to calculate the delay for recurring schedule.
     */
    private RecurrenceType recurrenceType = null;

    private String name;

    public RecurrenceType getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(RecurrenceType recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getFirstScheduleTs() {
        return firstScheduleTs;
    }

    public void setFirstScheduleTs(long firstScheduleTs) {
        this.firstScheduleTs = firstScheduleTs;
    }

    @Override
    public String toString() {
        return "ScheduleV1_1 ["
                + "initialTimeStamp=" + firstScheduleTs
                + ", recurrenceType=" + recurrenceType
                + ", name=" + name
                + "]";
    }
}
