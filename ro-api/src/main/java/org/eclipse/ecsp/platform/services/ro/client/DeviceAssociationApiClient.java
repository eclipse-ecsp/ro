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
 *
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

package org.eclipse.ecsp.platform.services.ro.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.exceptions.BadRequestException;
import org.eclipse.ecsp.platform.services.ro.constant.ResponseMsgConstants;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Objects;

/**
 * Client to check user device association with vehicle.
 * Uses association api to get associated users for given device.
 * If user is not associated with device, then throws UserNotAssociatedException.
 */
@Component
public class DeviceAssociationApiClient {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(DeviceAssociationApiClient.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${association.api.base.url}")
    private String userAssociatedBaseUrl;

    @Value("${associated.user.api.path}")
    private String associatedUserApiPath;

    /**
     * Method to fetch associated users for given deviceId.
     */
    public String getAssociatedUsers(String deviceId) throws BadRequestException {
        String url = userAssociatedBaseUrl + associatedUserApiPath + "?deviceid=" + deviceId;
        LOGGER.info("Getting AssociatedUsers with url :{} ", url);
        String userId = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add(ResponseMsgConstants.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET,
                    new HttpEntity<>(headers), String.class);
            LOGGER.info("Association API Response : {}", response);
            if (Objects.nonNull(response) && response.getStatusCode().equals(HttpStatus.OK)) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode responseNode = mapper.readValue(response.getBody(), ObjectNode.class);
                LOGGER.debug("Associated Users API response body : {} for deviceId: {}", responseNode, deviceId);
                JsonNode nameNode = responseNode.get(Constants.RO_REQUEST_USERID);
                userId = nameNode.asText();
                LOGGER.info("Fetched UserId : {}", userId);
            } else {
                LOGGER.error("Fetching device association details failed with code : {} and reason: {}",
                        response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            LOGGER.error("Error occurred while fetching device association details using API call: {}",
                    userAssociatedBaseUrl + associatedUserApiPath + "?deviceid="
                            + deviceId + "with message" + e.getMessage());
            throw new BadRequestException("Error occurred while fetching device association details using API call: {}"
                    + userAssociatedBaseUrl + associatedUserApiPath + "?deviceid="
                    + deviceId + "with message" + e.getMessage());
        }
        if (Objects.isNull(userId)) {
            LOGGER.error("UserId not found for deviceId : " + deviceId);
            throw new BadRequestException("UserId not found for deviceId : " + deviceId);
        }
        return userId;
    }
}
