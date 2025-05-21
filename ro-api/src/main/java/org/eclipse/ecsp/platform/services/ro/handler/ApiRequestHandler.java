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

package org.eclipse.ecsp.platform.services.ro.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.ecsp.domain.ro.constant.Constants;
import org.eclipse.ecsp.platform.services.ro.client.DeviceAssociationApiClient;
import org.eclipse.ecsp.platform.services.ro.exceptions.UserNotAssociatedException;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import java.util.Map;
import java.util.Objects;

/**
 * Class to intercept API requests to validate requested user for device association
 * for all RO operations.
 */
@Component
public class ApiRequestHandler implements HandlerInterceptor {

    @Autowired
    private DeviceAssociationApiClient deviceAssociationApiClient;

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ApiRequestHandler.class);

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler)
            throws Exception {

        LOGGER.debug("Handling Requested URI : {}", request.getRequestURI());

        final Map<String, String> pathVariables = (Map<String, String>) request
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        String deviceId = pathVariables.get(Constants.RO_VEHICLE_ID);
        String userId = pathVariables.get(Constants.RO_REQUEST_USERID);

        LOGGER.debug("Requested Path Variable deviceId :{} , userId : {}", deviceId, userId);

        if (Objects.isNull(deviceId)) {
            deviceId = request.getHeader(Constants.RO_VEHICLE_ID);
            LOGGER.debug("Requested Headers deviceId :{}", deviceId);
        }

        if (Objects.isNull(userId)) {
            userId = request.getHeader(Constants.RO_REQUEST_USERID);
            LOGGER.debug("Requested Headers userId : {}", userId);
        }

        // verifying association of user with device
        String responseUserId = deviceAssociationApiClient.getAssociatedUsers(deviceId);

        if (!userId.equalsIgnoreCase(responseUserId)) {
            LOGGER.error("User not associated with deviceId : " + deviceId);
            throw new UserNotAssociatedException("User not associated with deviceId : " + deviceId);
        }
        return true;
    }
}
