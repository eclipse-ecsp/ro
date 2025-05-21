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

package org.eclipse.ecsp.platform.services.ro.dao;

import org.eclipse.ecsp.domain.ro.Ro;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import java.util.List;
import java.util.Optional;

/**
 * Dao for Mongo operations on {@link Ro} Object with VehicleId as key.
 *
 * @author Neerajkumar
 */
@Repository
public class RoDAOMongoImpl extends IgniteBaseDAOMongoImpl<String, Ro> {

    private static final String VEHICLE_ID = "roEvent.vehicleId";

    private static final String RO_REQUEST_ID = "roEvent.eventData.roRequestId";

    /**
     * Gets ri responses.
     *
     * @param vehicleId   the vehicle id
     * @param roRequestId the ro request id
     * @return the ri responses
     */
    public Optional<Ro> getRIResponses(String vehicleId, String roRequestId) {
        IgniteCriteria vehicleIdCriteria = new IgniteCriteria(VEHICLE_ID, Operator.EQ, vehicleId);
        IgniteCriteria roRequestCriteria = new IgniteCriteria(RO_REQUEST_ID, Operator.EQ, roRequestId);

        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(roRequestCriteria).and(vehicleIdCriteria);

        IgniteQuery igQuery = new IgniteQuery(criteriaGroup);

        List<Ro> entities = (List<Ro>) this.find(igQuery);
        Ro roRes = CollectionUtils.isEmpty(entities) ? null : entities.get(Constants.ZERO);

        return Optional.ofNullable(roRes);
    }
}