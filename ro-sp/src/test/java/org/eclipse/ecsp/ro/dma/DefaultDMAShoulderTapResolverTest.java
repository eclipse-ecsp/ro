package org.eclipse.ecsp.ro.dma;

import org.eclipse.ecsp.entities.IgniteEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class DefaultDMAShoulderTapResolverTest {

    private DefaultDMAShoulderTapResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new DefaultDMAShoulderTapResolver();
    }

    private void injectShoulderTapValue(boolean value) throws Exception {
        Field field = DefaultDMAShoulderTapResolver.class.getDeclaredField("shoulderTap");
        field.setAccessible(true);
        field.set(resolver, value);
    }

    @Test
    void testIsShoulderTap_WhenTrue() throws Exception {
        injectShoulderTapValue(true);

        IgniteEvent mockEvent = mock(IgniteEvent.class);
        boolean result = resolver.isShoulderTap(mockEvent);

        assertTrue(result, "Expected shoulderTap to be true");
    }

    @Test
    void testIsShoulderTap_WhenFalse() throws Exception {
        injectShoulderTapValue(false);

        IgniteEvent mockEvent = mock(IgniteEvent.class);
        boolean result = resolver.isShoulderTap(mockEvent);

        assertFalse(result, "Expected shoulderTap to be false");
    }
}