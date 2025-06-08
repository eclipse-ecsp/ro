package org.eclipse.ecsp.ro.queue;

import org.apache.kafka.streams.processor.api.Record;
import org.bson.types.ObjectId;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.ro.ROStatus;
import org.eclipse.ecsp.domain.ro.Ro;
import org.eclipse.ecsp.entities.AbstractIgniteEvent;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.nosqldao.Updates;
import org.eclipse.ecsp.ro.RoDAOMongoImpl;
import org.eclipse.ecsp.services.utils.ServiceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;

import java.time.Instant;
import java.util.*;

import static org.mockito.Mockito.*;

class AbstractQueueHandlerTest {

    static class TestQueueHandler extends AbstractQueueHandler {
        @Override
        public void process(IgniteKey key, IgniteEvent event, StreamProcessingContext context) {
            // No-op for test
        }
    }

    @InjectMocks
    private TestQueueHandler queueHandler;

    @Mock
    private RoDAOMongoImpl roDAOMongoImpl;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private ServiceUtil serviceUtil;

    @Mock
    private StreamProcessingContext ctxt;

    @Mock
    private RQueue<AbstractIgniteEvent> queue;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        queueHandler = new TestQueueHandler();
        queueHandler.roDAOMongoImpl = roDAOMongoImpl;
        queueHandler.redissonClient = redissonClient;
        queueHandler.serviceUtil = serviceUtil;
        queueHandler.roForeachTTL = 180000;
    }

    @Test
    void testUpdateEntityByROStatus_updatesWhenRoPresent() {
        IgniteEvent event = mock(IgniteEvent.class);
        when(event.getVehicleId()).thenReturn("VIN123");
        when(event.getRequestId()).thenReturn("REQ123");

        Ro ro = new Ro();
        ObjectId id = new ObjectId();
        ro.setId(id);

        when(roDAOMongoImpl.getROEntityByFieldNameByRoReqIdExceptACV("VIN123", "REQ123"))
                .thenReturn(Optional.of(ro));

        queueHandler.updateEntityByROStatus(event, ROStatus.TTL_EXPIRED);

        verify(roDAOMongoImpl).update(eq(id), any(Updates.class));
    }

    @Test
    void testUpdateEntityByROStatusWithVinAndRoRequestID_updatesWhenRoPresent() {
        Ro ro = new Ro();
        ObjectId id = new ObjectId();
        ro.setId(id);

        when(roDAOMongoImpl.getROEntityByFieldNameByRoReqIdExceptACV("VIN999", "REQ999"))
                .thenReturn(Optional.of(ro));

        queueHandler.updateEntityByROStatusWithVinAndRoRequestID("REQ999", "VIN999", ROStatus.TTL_EXPIRED);

        verify(roDAOMongoImpl).update(eq(id), any(Updates.class));
    }

    @Test
    void testSendToDevice_forwardsToKafkaStream() {
        AbstractIgniteEvent event = mock(AbstractIgniteEvent.class);
        when(event.getRequestId()).thenReturn("REQ001");
        when(event.getVehicleId()).thenReturn("VIN001");

        queueHandler.sendToDevice(event, ctxt);

        verify(event).setResponseExpected(true);
        verify(event).setDeviceRoutable(true);
        verify(event).setShoulderTapEnabled(true);
        verify(ctxt).forward(any(Record.class));
    }

    @Test
    void testCheckTTLExpireANDForwad_sendsEventIfNotExpired() {
        AbstractIgniteEvent event = mock(AbstractIgniteEvent.class);
        when(event.getTimestamp()).thenReturn(Instant.now().toEpochMilli());
        when(event.getVehicleId()).thenReturn("VIN001");

        when(queue.iterator()).thenReturn(List.of(event).iterator());

        queueHandler.checkTTLExpireANDForwad(queue, ctxt);

        verify(ctxt).forward(any(Record.class));
        verify(queue, never()).poll();
    }

    @Test
    void testCheckTTLExpireANDForwad_expiresEventIfOld() {
        AbstractIgniteEvent event = mock(AbstractIgniteEvent.class);
        long oldTimestamp = Instant.now().minusMillis(200000).toEpochMilli();
        when(event.getTimestamp()).thenReturn(oldTimestamp);
        when(event.getVehicleId()).thenReturn("VIN123");
        when(event.getRequestId()).thenReturn("REQ123");

        Ro ro = new Ro();
        ObjectId id = new ObjectId();
        ro.setId(id);

        when(roDAOMongoImpl.getROEntityByFieldNameByRoReqIdExceptACV("VIN123", "REQ123"))
                .thenReturn(Optional.of(ro));

        when(queue.iterator()).thenReturn(List.of(event).iterator());

        queueHandler.checkTTLExpireANDForwad(queue, ctxt);

        verify(roDAOMongoImpl).update(eq(id), any(Updates.class));
        verify(queue).poll();
    }
}