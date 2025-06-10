package org.eclipse.ecsp.ro.queue;

import org.apache.kafka.streams.processor.api.Record;
import org.bson.types.ObjectId;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.ro.ROStatus;
import org.eclipse.ecsp.domain.ro.Ro;
import org.eclipse.ecsp.entities.AbstractIgniteEvent;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.nosqldao.Updates;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private static final int DEFAULT_RO_FOREACH_TTL = 180000;

    private static final int TTL_SUBTRACTION_MILLIS = 200000;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        queueHandler = new TestQueueHandler();
        queueHandler.roDAOMongoImpl = roDAOMongoImpl;
        queueHandler.redissonClient = redissonClient;
        queueHandler.serviceUtil = serviceUtil;
        queueHandler.roForeachTTL = DEFAULT_RO_FOREACH_TTL;
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
    void testCheckTTLExpireANDForward_expiresEventIfOld() {
        AbstractIgniteEvent event = mock(AbstractIgniteEvent.class);
        long oldTimestamp = Instant.now().minusMillis(TTL_SUBTRACTION_MILLIS).toEpochMilli();
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