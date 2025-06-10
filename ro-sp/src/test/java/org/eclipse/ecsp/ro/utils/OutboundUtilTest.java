package org.eclipse.ecsp.ro.utils;

import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.ro.RemoteOperationResponseV1_1;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.key.IgniteKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Test for {@link OutboundUtil} class.
 */

class OutboundUtilTest {

    private OutboundUtil outboundUtil;
    private StreamProcessingContext mockContext;
    private IgniteKey<?> mockKey;

    @BeforeEach
    void setUp() {
        outboundUtil = new OutboundUtil();
        mockContext = mock(StreamProcessingContext.class);
        mockKey = mock(IgniteKey.class);
    }

    @Test
    void testSendROResponseOutbound_shouldForwardEvent() {
        // Given
        String eventId = "RO_EVENT";
        String vehicleId = "VH001";
        String requestId = "REQ123";
        String bizTransactionId = "BTID456";
        String origin = "THIRDPARTY";
        String userId = "user-abc";
        RemoteOperationResponseV1_1 response = new RemoteOperationResponseV1_1();
        response.setRoRequestId(requestId);
        response.setUserId(userId);

        // When
        outboundUtil.sendROResponseOutbound(
                mockKey,
                mockContext,
                eventId,
                vehicleId,
                requestId,
                bizTransactionId,
                origin,
                userId,
                response
        );

        // Then
        ArgumentCaptor.forClass(IgniteEvent.class);
        verify(mockContext).forward(any());
    }

    @Test
    void testCreateRemoteOperationResponseV1_1_shouldPopulateAllFields() {
        // Given
        String requestId = "REQ123";
        String userId = "user-xyz";
        String partnerId = "partner-321";
        RemoteOperationResponseV1_1.Response responseEnum = RemoteOperationResponseV1_1.Response.SUCCESS;

        // When
        RemoteOperationResponseV1_1 result = outboundUtil.createRemoteOperationResponseV1_1(
                requestId, userId, partnerId, responseEnum
        );

        // Then
        assertNotNull(result);
        assertEquals(requestId, result.getRoRequestId());
        assertEquals(userId, result.getUserId());
        assertEquals(partnerId, result.getPartnerId());
        assertEquals(responseEnum, result.getResponse());
    }

    @Test
    void testCreateRemoteOperationResponseV1_1_shouldSkipEmptyPartnerId() {
        // Given
        String requestId = "REQ789";
        String userId = "user-000";
        String partnerId = null;
        RemoteOperationResponseV1_1.Response responseEnum = RemoteOperationResponseV1_1.Response.FAIL;

        // When
        RemoteOperationResponseV1_1 result = outboundUtil.createRemoteOperationResponseV1_1(
                requestId, userId, partnerId, responseEnum
        );

        // Then
        assertEquals(requestId, result.getRoRequestId());
        assertEquals(userId, result.getUserId());
        assertNull(result.getPartnerId());
        assertEquals(responseEnum, result.getResponse());
    }
}