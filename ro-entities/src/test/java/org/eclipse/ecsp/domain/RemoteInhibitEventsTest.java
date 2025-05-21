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

package org.eclipse.ecsp.domain;

import org.eclipse.ecsp.domain.remoteInhibit.CrankNotificationDataV1_0;
import org.eclipse.ecsp.domain.remoteInhibit.RemoteInhibitRequestV1_1;
import org.eclipse.ecsp.domain.remoteInhibit.RemoteInhibitRequestV1_1.CrankInhibit;
import org.eclipse.ecsp.domain.remoteInhibit.RemoteInhibitResponseV1_1;
import org.eclipse.ecsp.domain.remoteInhibit.RemoteInhibitResponseV1_1.Response;
import org.eclipse.ecsp.domain.ro.constant.TestConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class {@link RemoteInhibitEventsTest}.
 */
class RemoteInhibitEventsTest {

    @Test
    void testRemoteInhibitRequest() {

        // RemoteInhibitRequestV1_1
        RemoteInhibitRequestV1_1 remoteInhibitRequest = new RemoteInhibitRequestV1_1();
        remoteInhibitRequest.setCrankInhibit(CrankInhibit.INHIBIT);
        remoteInhibitRequest.setUserId("user123");

        String expectedValue = "RemoteInhibitRequestV1_1 [crankInhibit=INHIBIT]";

        Assertions.assertTrue(remoteInhibitRequest.toString()
                .replaceAll("\\s", "").contains(expectedValue
                        .replaceAll("\\s", "")));
        Assertions.assertEquals(CrankInhibit.INHIBIT, remoteInhibitRequest.getCrankInhibit());
        Assertions.assertEquals("user123", remoteInhibitRequest.getUserId());

    }

    @Test
    void testRemoteInhibitResponse() {

        // RemoteInhibitResponseV1_1
        RemoteInhibitResponseV1_1 remoteInhibitResponse = new RemoteInhibitResponseV1_1();
        remoteInhibitResponse.setResponse(Response.SUCCESS);
        remoteInhibitResponse.setRoRequestId("roId1234");

        String expectedValue = "RemoteInhibitResponseV1_1 [response=SUCCESS, roRequestId=roId1234]";

        Assertions.assertTrue(remoteInhibitResponse.toString()
                .replaceAll("\\s", "")
                .contains(expectedValue.replaceAll("\\s", "")));
        Assertions.assertEquals(Response.SUCCESS, remoteInhibitResponse.getResponse());
        Assertions.assertEquals("roId1234", remoteInhibitResponse.getRoRequestId());

    }

    @Test
    void testCrankNotificationData() {

        // CrankNotificationDataV1_0
        CrankNotificationDataV1_0 cnData = new CrankNotificationDataV1_0();
        cnData.setAltitude(TestConstants.DOUBLE_123_5);
        cnData.setBearing(TestConstants.DOUBLE_123);
        cnData.setCrankAttempted(true);
        cnData.setHorPosError(TestConstants.TWELVE);
        cnData.setLatitude(TestConstants.DOUBLE_92_4);
        cnData.setLongitude(TestConstants.DOUBLE_12_32);
        cnData.setOrigin("StoelnVehicleSupportOwner");
        cnData.setUserId("usr123");
        cnData.setRoRequestId("ro12345");

        Assertions.assertEquals("usr123", cnData.getUserId());
        Assertions.assertEquals("ro12345", cnData.getRoRequestId());

    }
}
