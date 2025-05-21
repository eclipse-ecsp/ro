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

package org.eclipse.ecsp.ro.processor;

import jakarta.validation.constraints.NotBlank;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.analytics.stream.base.idgen.internal.GlobalMessageIdGenerator;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.remoteInhibit.AbstractRemoteInhibitEventData;
import org.eclipse.ecsp.domain.remoteInhibit.CrankNotificationDataV1_0;
import org.eclipse.ecsp.domain.remoteInhibit.RemoteInhibitRequestV1_1;
import org.eclipse.ecsp.domain.remoteInhibit.RemoteInhibitResponseV1_1;
import org.eclipse.ecsp.domain.ro.Ro;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.entities.UserContext;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.nosqldao.Updates;
import org.eclipse.ecsp.ro.RoDAOMongoImpl;
import org.eclipse.ecsp.ro.constants.Constants;
import org.eclipse.ecsp.ro.dma.DMAShoulderTapResolver;
import org.eclipse.ecsp.ro.utils.CacheUtil;
import org.eclipse.ecsp.ro.utils.Utils;
import org.eclipse.ecsp.services.constants.VehicleProfileAttribute;
import org.eclipse.ecsp.services.utils.SettingsManagerClient;
import org.eclipse.ecsp.services.utils.VehicleProfileClient;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * RemoteInhibitDataHandler class.
 */
@Component
public class RemoteInhibitDataHandler {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(RemoteInhibitDataHandler.class);

    public static final String UNKNOWN = "UNKNOWN";

    public static final String STOLEN_REMOTE_INHIBIT_SUPPORT_OWNER =
            "StolenRemoteInhibitSupportOwner";

    public static final String FLEET_REMOTE_INHIBIT_OWNER = "FleetRemoteInhibitOwner";

    public static final String SVL = "SVL";

    public static final String FLEET = "FLEET";

    public static final String NOTIFICATION = "NOTIFICATION";

    public static final String UNDERSCORE = "_";

    public static final String ASSIGN_CALL_CENTER = "AssignCallCenter";

    public static final String CALL_CENTER_NAME = "callCenterName";

    public static final String STOLEN = "stolen";

    public static final String CALL_CENTER_INFO = "callCenterInfo";

    @Value("${service.name}")
    private String serviceName;

    @NotBlank
    @Value("${settings.object.json.path}")
    private String settingsJsonPath;

    @Value("#{'${callcenter.enabled.origin}'.split(',')}")
    private List<String> callCenterEnabledOrigin;

    @Autowired
    private VehicleProfileClient vehicleProfileClient;

    @Autowired
    private GlobalMessageIdGenerator globalMessageIdGenerator;

    @Autowired
    private RoDAOMongoImpl roDAOMongoImpl;

    @Autowired
    private SettingsManagerClient settingsManagerClient;

    @Autowired
    private CacheUtil cacheUtil;

    @Autowired
    private DMAShoulderTapResolver dmaShoulderTapResolver;


    /**
     * Process user event.
     *
     * @param key   Ignite Key
     * @param value Ignite event
     * @return serealized event
     */
    public IgniteEventImpl processUserEvent(IgniteKey key, IgniteEvent value) {
        IgniteEventImpl riRequestImpl = (IgniteEventImpl) value;

        if (value.getEventId().equals(Constants.EVENT_ID_REMOTE_INHIBIT_REQUEST)) {
            riRequestImpl.setMessageId(globalMessageIdGenerator.generateUniqueMsgId(serviceName));
            riRequestImpl.setVersion(Version.V1_1);
            riRequestImpl.setTimestamp(System.currentTimeMillis());
            riRequestImpl.setResponseExpected(true);
            riRequestImpl.setDeviceRoutable(true);
            riRequestImpl.setShoulderTapEnabled(dmaShoulderTapResolver.isShoulderTap(value));
            LOGGER.debug("Key={}, apiEvent={}", key, value);
            persistEntity(riRequestImpl);
        }
        return riRequestImpl;
    }

    /**
     * Process device event.
     *
     * @param key   Ignite Key
     * @param value Ignite event
     * @return serialized event
     */
    public IgniteEventImpl processDeviceEvent(IgniteKey key, IgniteEvent value) {
        IgniteEventImpl remoteInhibitResponse = (IgniteEventImpl) value;

        if (value.getEventId().equals(Constants.EVENT_ID_REMOTE_INHIBIT_RESPONSE)) {

            LOGGER.debug("Processing RemoteInhibitResponse event, event {}", value);

            RemoteInhibitResponseV1_1 responseData =
                    (RemoteInhibitResponseV1_1) remoteInhibitResponse.getEventData();

            String originFromRequest = null;
            String userIdFromRequest = null;

            String cacheKey =
                    responseData.getRoRequestId()
                            + Constants.UNDER_SCORE
                            + remoteInhibitResponse.getVehicleId();
            Element riRequestCacheElement = cacheUtil.getCache(Constants.RO_CACHE_NAME).get(cacheKey);
            Map<String, String> riRequestEntityMap = null;
            if (null != riRequestCacheElement && !riRequestCacheElement.isExpired()) {
                riRequestEntityMap = (Map<String, String>) riRequestCacheElement.getObjectValue();
            }

            // First check the cache for the Request data
            if (null != riRequestEntityMap && !riRequestEntityMap.isEmpty()) {

                originFromRequest =
                        riRequestEntityMap.get(org.eclipse.ecsp.domain.ro.constant.Constants.RO_REQUEST_ORIGIN);
                userIdFromRequest =
                        riRequestEntityMap.get(org.eclipse.ecsp.domain.ro.constant.Constants.RO_REQUEST_USERID);

            } else {
                // Then check the MongoDB for the Request data
                Optional<Ro> dbEntity =
                        roDAOMongoImpl.getRIEntityByFieldName(
                                responseData.getRoRequestId(), remoteInhibitResponse.getVehicleId());
                LOGGER.debug("dbEntity::" + Utils.logForging(dbEntity));

                if (dbEntity.isPresent()) {
                    originFromRequest =
                            ((RemoteInhibitRequestV1_1) dbEntity.get().getRoEvent().getEventData()).getOrigin();
                    userIdFromRequest =
                            ((RemoteInhibitRequestV1_1) dbEntity.get().getRoEvent().getEventData()).getUserId();
                }
            }

            responseData.setOrigin(originFromRequest);
            responseData.setUserId(userIdFromRequest);

            setQualifier(key, remoteInhibitResponse, originFromRequest);

            Updates updateRoResponse = new Updates();
            updateRoResponse.addListAppend(
                    org.eclipse.ecsp.domain.ro.constant.Constants.RO_RESPONSE_LIST, value);

            // Updating RI Response list to RO Entity
            roDAOMongoImpl.update(
                    roDAOMongoImpl.prepareIgniteQueryForRoRequest(
                            responseData.getRoRequestId(), remoteInhibitResponse.getVehicleId()),
                    updateRoResponse);

            UserContext userContext = new UserContext();
            userContext.setUserId(userIdFromRequest);
            remoteInhibitResponse.setUserContextInfo(Arrays.asList(userContext));
        }
        LOGGER.debug("remoteInhibitResponse: {} ", Utils.logForging(remoteInhibitResponse));
        return remoteInhibitResponse;
    }

    /**
     * Process notification event.
     *
     * @param key   Ignite Key
     * @param value Ignite event
     * @return serealized event
     */
    public IgniteEventImpl processNotificationEvent(IgniteKey key, IgniteEvent value) {
        IgniteEventImpl notificationEventImpl = (IgniteEventImpl) value;
        CrankNotificationDataV1_0 eventData =
                (CrankNotificationDataV1_0) notificationEventImpl.getEventData();
        LOGGER.debug("processNotificationEvent data {} ", eventData);
        // crank notification Dff set
        setQualifier(key, notificationEventImpl, NOTIFICATION);
        // crank notification userId set
        String userId = eventData.getUserId();
        if (null == userId || userId.isEmpty()) {
            userId = getDataFromVehicleProfile(value.getVehicleId(), VehicleProfileAttribute.USERID);
        }
        eventData.setUserId(userId);
        LOGGER.debug("processing Crank Notification data {} ", notificationEventImpl);

        Updates updateRoResponse = new Updates();
        updateRoResponse.addListAppend(
                org.eclipse.ecsp.domain.ro.constant.Constants.RO_RESPONSE_LIST, value);

        // Updating RI Response list to RO Entity
        roDAOMongoImpl.update(
                roDAOMongoImpl.prepareIgniteQueryForRIRequestWithSessionId(
                        notificationEventImpl.getBizTransactionId(), notificationEventImpl.getVehicleId()),
                updateRoResponse);

        UserContext userContext = new UserContext();
        userContext.setUserId(userId);
        notificationEventImpl.setUserContextInfo(Arrays.asList(userContext));
        LOGGER.debug("notificationEventImpl: {} ", notificationEventImpl);
        return notificationEventImpl;
    }

    private String getDataFromVehicleProfile(String vehicleId, VehicleProfileAttribute vpa) {
        try {
            Optional<String> value = vehicleProfileClient.getVehicleProfileAttribute(vehicleId, vpa);
            return value.orElse(UNKNOWN);
        } catch (Exception e) {
            LOGGER.error("Unable to get data from Vehicle Profile");
        }
        return UNKNOWN;
    }

    /**
     * Save entity to database.
     *
     * @param value processed event
     */
    public void persistEntity(IgniteEventImpl value) {
        Ro remoteInhibitEntity = new Ro();
        remoteInhibitEntity.setSchemaVersion(value.getSchemaVersion());
        remoteInhibitEntity.setRoEvent(value);

        roDAOMongoImpl.save(remoteInhibitEntity);
        LOGGER.debug("Entity inserted into MongoDB ,entity :{}", remoteInhibitEntity);

        Element entityElementToCache = buildElementForCache(value);
        cacheUtil.getCache(Constants.RO_CACHE_NAME).put(entityElementToCache);
        LOGGER.debug("Relevant RI Entity fields are cached :{}", entityElementToCache.getObjectValue());
    }

    /**
     * Build {@link Element} for caching.
     *
     * @param value Ignite event
     * @return {@link Element} object
     */
    public Element buildElementForCache(IgniteEvent value) {

        Map<String, String> roRequestElementsMap = new HashMap<String, String>();
        AbstractRemoteInhibitEventData abstractRoEventData =
                (AbstractRemoteInhibitEventData) value.getEventData();
        roRequestElementsMap.put(
                org.eclipse.ecsp.domain.ro.constant.Constants.RO_REQUEST_ORIGIN,
                abstractRoEventData.getOrigin());
        roRequestElementsMap.put(
                org.eclipse.ecsp.domain.ro.constant.Constants.RO_REQUEST_USERID,
                abstractRoEventData.getUserId());

        String cachekey = value.getRequestId() + Constants.UNDER_SCORE + value.getVehicleId();
        return new Element(cachekey, roRequestElementsMap);
    }

    /**
     * Set Qualifier.
     *
     * @param key    Ignite Key
     * @param value  Ignite Event
     * @param origin origin source
     * @return qualifier
     */
    public String setQualifier(IgniteKey key, IgniteEventImpl value, String origin) {
        String callcenterName = null;
        String qualifier = null;
        try {
            if (callCenterEnabledOrigin.contains(origin.toUpperCase())) {
                callcenterName =
                        getCallCenterName(value.getVehicleId()) != null
                                ? getCallCenterName(value.getVehicleId())
                                : "";
                if (StringUtils.isNotBlank(callcenterName)) {
                    LOGGER.debug("callCenterName received : ", callcenterName);
                    qualifier =
                            Constants.QUALIFIER
                                    + Constants.UNDER_SCORE
                                    + value.getEventId().toUpperCase()
                                    + Constants.UNDER_SCORE
                                    + callcenterName.toUpperCase();
                    value.setDFFQualifier(qualifier);
                    LOGGER.debug("dffQualifier :: {}", value.getDFFQualifier());
                } else {
                    LOGGER.debug("callCenterName Not received from settingsMgrClient");
                }
            } else {
                qualifier =
                        Constants.QUALIFIER
                                + Constants.UNDER_SCORE
                                + value.getEventId().toUpperCase()
                                + Constants.UNDER_SCORE
                                + origin.toUpperCase();
                value.setDFFQualifier(qualifier);
                LOGGER.debug("dffQualifier :: {}", Utils.logForging(value.getDFFQualifier()));
            }
        } catch (Exception e) {
            LOGGER.error("Exception while setting qualifier :{}", e.getMessage());
        }
        LOGGER.debug("setQualifier - qualifier :: {}", Utils.logForging(value.getDFFQualifier()));
        return qualifier;
    }

    private String getCallCenterName(String vehicleId) {
        Map<String, Object> smConfigurationObject = getCallCenterDetails(vehicleId);
        return getCallCenterNameFromData(smConfigurationObject);
    }

    private Map<String, Object> getCallCenterDetails(String vehicleId) {
        return settingsManagerClient.getSettingsManagerConfigurationObject(
                UNKNOWN, vehicleId, ASSIGN_CALL_CENTER, settingsJsonPath);
    }

    private String getCallCenterNameFromData(Map<String, Object> configurationObject) {
        try {
            Map<String, Object> messages =
                    (Map<String, Object>) configurationObject.get(CALL_CENTER_INFO);
            LOGGER.debug(
                    "getCallCenterNameFromData Messages received from configuration object is :{}", messages);

            Map<String, String> phoneDetails = ((List<Map<String, String>>) messages.get(STOLEN)).get(0);
            if (null != phoneDetails.get(CALL_CENTER_NAME)
                    && !phoneDetails.get(CALL_CENTER_NAME).isEmpty()) {
                return phoneDetails.get(CALL_CENTER_NAME);
            }
        } catch (Exception e) {
            LOGGER.debug("Invalid data format :{}", e.getMessage());
        }
        return null;
    }
}
