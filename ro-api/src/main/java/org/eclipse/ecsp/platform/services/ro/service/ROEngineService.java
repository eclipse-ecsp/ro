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

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.ro.RecurrenceType;
import org.eclipse.ecsp.domain.ro.RemoteOperationEngineV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationType;
import org.eclipse.ecsp.domain.ro.RoSchedule;
import org.eclipse.ecsp.domain.ro.Schedule;
import org.eclipse.ecsp.domain.ro.ScheduleDto;
import org.eclipse.ecsp.domain.ro.ScheduleStatus;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.exceptions.BadRequestException;
import org.eclipse.ecsp.exceptions.ForbiddenException;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.platform.services.ro.constant.EventIdConstants;
import org.eclipse.ecsp.platform.services.ro.constant.ResponseMsgConstants;
import org.eclipse.ecsp.platform.services.ro.dao.RoScheduleDAOMongoImpl;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationEngineReq;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationEngineScheduleReq;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationResponse;
import org.eclipse.ecsp.platform.services.ro.domain.RemoteOperationScheduleResponse;
import org.eclipse.ecsp.platform.services.ro.domain.ScheduleRequest;
import org.eclipse.ecsp.utils.ApiUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Service Class for RO Engine Operations.
 *
 * @author midnani
 */
@Service
public class ROEngineService {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ROEngineService.class);

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private Utils utils;

    @Autowired
    private ApiUtils apiUtils;

    @Autowired
    private RoScheduleDAOMongoImpl roScheduleDAOMongoImpl;

    @Value("${ro.schedule.limit:5}")
    private long scheduleLimit;

    /**
     * Create remote engine request.
     *
     * @param userId                   the user id
     * @param vehicleId                the vehicle id
     * @param remoteOperationEngineReq the remote engine request
     * @param sessionId                the session id
     * @param origin                   the origin
     * @param ecuType                  the ecu type
     * @param partnerId                the partner id
     * @param vehicleArchType          the vehicle arch type
     * @return the remote operation response
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    public RemoteOperationResponse createRemoteEngineRequest(
            String userId, String vehicleId,
            RemoteOperationEngineReq remoteOperationEngineReq,
            String sessionId, String origin,
            String ecuType, String partnerId,
            String vehicleArchType)
            throws
            InterruptedException,
            ExecutionException {

        LOGGER.info("Publishing remote engine command to Kafka for vehicleId:{}, userid: {}, ",
                Utils.logForging(vehicleId),
                Utils.logForging(userId));

        RemoteOperationEngineV1_1 roEngineData = new RemoteOperationEngineV1_1();
        roEngineData.setState(RemoteOperationEngineV1_1.State.valueOf(remoteOperationEngineReq.getState().name()));
        roEngineData.setDuration(remoteOperationEngineReq.getDuration());
        roEngineData.setRoRequestId(remoteOperationEngineReq.getRoRequestId());
        roEngineData.setOrigin(origin);
        roEngineData.setUserId(userId);
        roEngineData.setVehicleArchType(vehicleArchType);

        if (ObjectUtils.isNotEmpty(partnerId)) {
            roEngineData.setPartnerId(partnerId);
        }

        IgniteEvent igniteEventImpl = new IgniteEventImplBuilder()
                .withEventId(EventIdConstants.EVENT_ID_ENGINE.getValue())
                .withVersion(Version.V1_1)
                .withVehicleId(vehicleId)
                .withEventData(roEngineData)
                .withRequestId(remoteOperationEngineReq.getRoRequestId())
                .withBizTransactionId(sessionId)
                .withUserContextInfo(apiUtils.getUserContext(userId))
                .withTimestamp(System.currentTimeMillis())
                .withEcuType(ecuType)
                .build();

        LOGGER.debug("Publishing remote engine command to Kafka for IgniteEvent: {}",
                Utils.logForging(igniteEventImpl));

        kafkaService.sendIgniteEvent(igniteEventImpl);

        LOGGER.debug("Published remote engine command to Kafka for event ID: {} for vehicle: {}",
                Utils.logForging(igniteEventImpl.getEventId()),
                Utils.logForging(igniteEventImpl.getVehicleId()));

        return new RemoteOperationResponse(
                ResponseMsgConstants.RO_COMMAND_SUCCESS,
                remoteOperationEngineReq.getRoRequestId());
    }

    /**
     * Create remote engine schedule request .
     *
     * @param userId              the user id
     * @param vehicleId           the vehicle id
     * @param remoteEngineRequest the remote engine request
     * @param sessionId           the session id
     * @param origin              the origin
     * @param ecuType             the ecu type
     * @param vehicleArchType     the vehicle arch type
     * @return the remote operation schedule response
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     * @throws ForbiddenException   the forbidden exception
     * @throws BadRequestException  the bad request exception
     */
    public RemoteOperationScheduleResponse createRemoteEngineScheduleRequest(
            String userId, String vehicleId,
            RemoteOperationEngineScheduleReq remoteEngineRequest,
            String sessionId, String origin,
            String ecuType, String vehicleArchType)
            throws InterruptedException, ExecutionException, ForbiddenException, BadRequestException {

        LOGGER.info("Publishing remote engine schedule creation request to Kafka for "
                        + "vehicleId:{}, userid :{}", Utils.logForging(vehicleId),
                Utils.logForging(userId));

        RemoteOperationEngineV1_1 roEngineData = new RemoteOperationEngineV1_1();
        roEngineData.setState(RemoteOperationEngineV1_1.State.valueOf(remoteEngineRequest.getState().name()));
        roEngineData.setDuration(remoteEngineRequest.getDuration());
        roEngineData.setRoRequestId(remoteEngineRequest.getRoRequestId());
        roEngineData.setOrigin(origin);
        roEngineData.setUserId(userId);
        roEngineData.setVehicleArchType(vehicleArchType);
        String schedulerKey = null;
        ScheduleRequest scheduleRequest = remoteEngineRequest.getSchedule();
        if (scheduleRequest != null) {
            schedulerKey = remoteEngineRequest.getRoRequestId();
            saveSchedule(scheduleRequest, vehicleId, schedulerKey);
            Schedule schedule = new Schedule();
            schedule.setName(scheduleRequest.getName());
            if (scheduleRequest.getScheduleTs() < System.currentTimeMillis()) {
                throw new BadRequestException(ResponseMsgConstants.INVALID_SCHEDULE_TIME,
                        ResponseMsgConstants.INVALID_SCHEDULE_TIME_MESSAGE);
            }
            schedule.setFirstScheduleTs(scheduleRequest.getScheduleTs() - System.currentTimeMillis());
            schedule.setRecurrenceType((scheduleRequest.getRecurrenceType() == null) ? null
                    : (RecurrenceType.valueOf(scheduleRequest.getRecurrenceType().getValue())));
            roEngineData.setSchedule(schedule);
        }
        String roRequestId = remoteEngineRequest.getRoRequestId();
        IgniteEvent igniteEventImpl = new IgniteEventImplBuilder()
                .withEventId(EventIdConstants.EVENT_ID_ENGINE.getValue())
                .withVersion(Version.V1_1)
                .withVehicleId(vehicleId)
                .withEventData(roEngineData)
                .withRequestId(roRequestId)
                .withBizTransactionId(sessionId)
                .withUserContextInfo(apiUtils.getUserContext(userId))
                .withTimestamp(System.currentTimeMillis())
                .withEcuType(ecuType)
                .build();
        LOGGER.debug("Publish remote engine command to Kafka for IgniteEvent: {}", Utils.logForging(igniteEventImpl));
        kafkaService.sendIgniteEvent(igniteEventImpl);

        return new RemoteOperationScheduleResponse(ResponseMsgConstants.RO_COMMAND_SUCCESS, roRequestId, schedulerKey);
    }

    private void saveSchedule(ScheduleRequest scheduleRequest, String vehicleId,
                              String requestId) throws ForbiddenException {
        ScheduleDto scheduleDto = new ScheduleDto();
        scheduleDto.setName(scheduleRequest.getName());
        scheduleDto.setRecurrenceType(scheduleRequest.getRecurrenceType());
        scheduleDto.setSchedulerKey(requestId);
        scheduleDto.setScheduleTs(scheduleRequest.getScheduleTs());
        scheduleDto.setStatus(ScheduleStatus.ACTIVE);
        scheduleDto.setCreatedOn(System.currentTimeMillis());
        scheduleDto.setUpdatedOn(System.currentTimeMillis());
        RoSchedule dbRoSchedule = roScheduleDAOMongoImpl.findById(vehicleId);

        List<ScheduleDto> scheduleList = null;
        Map<String, List<ScheduleDto>> roMap = null;
        if (dbRoSchedule != null) {
            roMap = dbRoSchedule.getSchedules();

            if (roMap != null) {
                scheduleList = dbRoSchedule.getSchedules().get(RemoteOperationType.REMOTE_OPERATION_ENGINE.getValue());
                if (scheduleList != null) {
                    if (scheduleList.stream().filter(t -> ScheduleStatus.ACTIVE.equals(t.getStatus()))
                            .count() >= scheduleLimit) {
                        throw new ForbiddenException("Schedules limit reached.");
                    }
                    scheduleList.add(scheduleDto);
                } else {
                    scheduleList = new ArrayList<>();
                    scheduleList.add(scheduleDto);
                }
            } else {
                scheduleList = new ArrayList<>();
                roMap = new HashMap<>();
            }
            roMap.put(RemoteOperationType.REMOTE_OPERATION_ENGINE.getValue(), scheduleList);
        } else {

            dbRoSchedule = new RoSchedule();
            dbRoSchedule.setVehicleId(vehicleId);
            dbRoSchedule.setSchemaVersion(Version.V2_0);

            scheduleList = new ArrayList<>();
            scheduleList.add(scheduleDto);
            roMap = new HashMap<>();
            roMap.put(RemoteOperationType.REMOTE_OPERATION_ENGINE.getValue(), scheduleList);

        }

        dbRoSchedule.setSchedules(roMap);
        roScheduleDAOMongoImpl.update(dbRoSchedule);

    }
}
