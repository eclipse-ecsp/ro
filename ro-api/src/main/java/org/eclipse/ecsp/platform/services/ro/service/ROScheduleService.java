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
import org.eclipse.ecsp.domain.ro.RemoteOperationScheduleV1;
import org.eclipse.ecsp.domain.ro.RemoteOperationType;
import org.eclipse.ecsp.domain.ro.RoSchedule;
import org.eclipse.ecsp.domain.ro.ScheduleDto;
import org.eclipse.ecsp.domain.ro.ScheduleStatus;
import org.eclipse.ecsp.exceptions.BadRequestException;
import org.eclipse.ecsp.exceptions.EmptyResponseException;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.platform.services.ro.constant.EventIdConstants;
import org.eclipse.ecsp.platform.services.ro.constant.ResponseMsgConstants;
import org.eclipse.ecsp.platform.services.ro.dao.RoScheduleDAOMongoImpl;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Service Class for RO Schedule Operations.
 */
@Service
public class ROScheduleService {

    @Autowired
    private RoScheduleDAOMongoImpl roScheduleDAOMongoImpl;

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private Utils utils;

    /**
     * Gets ro schedules.
     *
     * @param vehicleId the vehicle id
     * @param roType    the ro type
     * @return the ro schedules
     * @throws EmptyResponseException the empty response exception
     */
    public List<ScheduleDto> getROSchedules(String vehicleId, RemoteOperationType roType)
            throws EmptyResponseException {
        RoSchedule roSchedules = roScheduleDAOMongoImpl.findById(vehicleId);
        List<ScheduleDto> responseSchedule = null;
        if (roSchedules != null) {
            Map<String, List<ScheduleDto>> schedulesMap = roSchedules.getSchedules();
            if (schedulesMap != null && schedulesMap.get(roType.getValue()) != null) {
                responseSchedule = schedulesMap.get(roType.getValue()).stream().filter(t ->
                        ScheduleStatus.ACTIVE.equals(t.getStatus())).toList();
            }
        }
        if (CollectionUtils.isEmpty(responseSchedule)) {
            throw new EmptyResponseException(ResponseMsgConstants.SCHEDULE_NOT_FOUND,
                    ResponseMsgConstants.NO_SCHEDULE_DATA_MESSAGE);
        }
        return responseSchedule;
    }

    /**
     * Delete ro schedules remote operation response.
     *
     * @param vehicleId    the vehicle id
     * @param roType       the ro type
     * @param userId       the user id
     * @param sessionId    the session id
     * @param requestId    the request id
     * @param schedulerKey the scheduler key
     * @return the remote operation response
     * @throws ExecutionException  the execution exception
     * @throws BadRequestException the bad request exception
     */
    public RemoteOperationResponse deleteROSchedules(String vehicleId, RemoteOperationType roType, String userId,
                                                     String sessionId, String requestId,
                                                     String schedulerKey)
        throws ExecutionException, BadRequestException {

        boolean isUpdated = false;
        RoSchedule roSchedules = roScheduleDAOMongoImpl.findById(vehicleId);

        if (roSchedules != null && !CollectionUtils.isEmpty(roSchedules.getSchedules())
                && !CollectionUtils.isEmpty(roSchedules.getSchedules().get(roType.getValue()))) {

            for (ScheduleDto schedule : roSchedules.getSchedules().get(roType.getValue())) {

                if (schedulerKey.equalsIgnoreCase(schedule.getSchedulerKey())
                        && !ScheduleStatus.INACTIVE.equals(schedule.getStatus())) {
                    schedule.setStatus(ScheduleStatus.INACTIVE);
                    isUpdated = true;
                    break;
                }
            }
        }

        if (!isUpdated) {
            throw new BadRequestException(ResponseMsgConstants.INVALID_SCHEDULE_KEY,
                    ResponseMsgConstants.INVALID_SCHEDULE_KEY_MESSAGE);
        }

        isUpdated = roScheduleDAOMongoImpl.update(roSchedules);

        if (isUpdated) {
            RemoteOperationScheduleV1 roEngineData = new RemoteOperationScheduleV1();
            roEngineData.setRoRequestId(schedulerKey);
            roEngineData.setUserId(userId);
            roEngineData.setSchedulerKey(schedulerKey);

            kafkaService
                    .sendIgniteEvent(
                            utils.createIgniteEvent(Version.V1_1, EventIdConstants.EVENT_ID_DELETE_SCHEDULE
                                    .getValue(), vehicleId, roEngineData, requestId, sessionId, userId));

            return new RemoteOperationResponse(ResponseMsgConstants.RO_SCHEDULE_DELETE_SUCCESS);
        } else {
            return new RemoteOperationResponse(ResponseMsgConstants.RO_SCHEDULE_DELETE_FAIL);
        }

    }
}
