package org.eclipse.ecsp.ro.utils;

import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.ro.constants.Constants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CachedKeyUtilTest {

    /*@Test
    void testPrivateConstructor_shouldThrowException() throws Exception {
        var constructor = CachedKeyUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(UnsupportedOperationException.class, constructor::newInstance);
    }*/

    @Test
    void getEngineStatusKey_shouldReturnCorrectKey() {
        IgniteEvent event = mock(IgniteEvent.class);
        when(event.getVehicleId()).thenReturn("VH001");

        String expected = Constants.RO_ENGINE_STATUS_PREFIX + "VH001";
        String result = CachedKeyUtil.getEngineStatusKey(event);

        assertEquals(expected, result);
    }

    @Test
    void getROQueueKey_shouldReturnCorrectKey() {
        IgniteEvent event = mock(IgniteEvent.class);
        when(event.getVehicleId()).thenReturn("VH002");

        String expected = Constants.RO_QUEUE_PREFIX + "VH002";
        String result = CachedKeyUtil.getROQueueKey(event);

        assertEquals(expected, result);
    }

    @Test
    void getRONotificationMappingKey_shouldReturnCorrectKey() {
        IgniteEvent event = mock(IgniteEvent.class);
        when(event.getVehicleId()).thenReturn("VH003");

        String expected = Constants.NOTIFICATION_MAPPING + Constants.UNDER_SCORE + "VH003";
        String result = CachedKeyUtil.getRONotificationMappingKey(event);

        assertEquals(expected, result);
    }
}