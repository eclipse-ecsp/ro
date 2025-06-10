package org.eclipse.ecsp.ro.queue;

import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.entities.AbstractIgniteEvent;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.ro.RoDAOMongoImpl;
import org.eclipse.ecsp.services.utils.ServiceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;

import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;


import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;



class RequestQueueHandlerTest {

    @InjectMocks
    private RequestQueueHandler requestQueueHandler;

    @Mock
    private RoDAOMongoImpl roDAOMongoImpl;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RQueue<AbstractIgniteEvent> queue;

    @Mock
    private StreamProcessingContext context;

    @Mock
    private IgniteKey igniteKey;

    private IgniteEvent igniteEvent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        igniteEvent = mock(AbstractIgniteEvent.class);
        when(igniteEvent.getVehicleId()).thenReturn("VIN1234");
        when(igniteEvent.getRequestId()).thenReturn("REQ-1");
        queue = mock(RQueue.class);
        when(redissonClient.getQueue(anyString())).thenReturn((RQueue)queue);
    }

    @Test
    void testProcess_withNonExpiredEvent_shouldSendToDevice() {
        AbstractIgniteEvent event = mock(AbstractIgniteEvent.class);
        long currentTime = Instant.now().toEpochMilli();
        when(event.getTimestamp()).thenReturn(currentTime);
        when(event.getVehicleId()).thenReturn("VIN1234");
        when(event.getRequestId()).thenReturn("REQ-1");

        when(redissonClient.getQueue(anyString())).thenReturn((RQueue)queue);
        when(queue.iterator()).thenReturn(Collections.singletonList(event).iterator());
        when(queue.size()).thenReturn(1);

        requestQueueHandler.roForeachTTL = 180000L;

        requestQueueHandler.process(igniteKey, igniteEvent, context);

        // Should send to device, TTL not expired
        verify(queue).offer((AbstractIgniteEvent) igniteEvent);
    }

    @Test
    void testProcess_withExpiredEvent_shouldUpdateStatusAndRemove() {
        AbstractIgniteEvent expiredEvent = mock(AbstractIgniteEvent.class);
        long oldTimestamp = Instant.now().minusMillis(999999).toEpochMilli();

        when(expiredEvent.getTimestamp()).thenReturn(oldTimestamp);
        when(expiredEvent.getVehicleId()).thenReturn("VIN1234");
        when(expiredEvent.getRequestId()).thenReturn("REQ-2");

        Iterator<AbstractIgniteEvent> iterator = mock(Iterator.class);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(expiredEvent);

        when(redissonClient.getQueue(anyString())).thenReturn((RQueue)queue);
        when(queue.iterator()).thenReturn(iterator);
        when(queue.size()).thenReturn(1);

        requestQueueHandler.roForeachTTL = 180000L;

        requestQueueHandler.process(igniteKey, igniteEvent, context);

        // Should call updateEntityByROStatus
        verify(roDAOMongoImpl, atLeastOnce()).getROEntityByFieldNameByRoReqIdExceptACV(eq("VIN1234"), eq("REQ-2"));
    }
}