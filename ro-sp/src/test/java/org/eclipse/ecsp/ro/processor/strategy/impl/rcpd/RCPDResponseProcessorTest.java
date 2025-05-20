package org.eclipse.ecsp.ro.processor.strategy.impl.rcpd;

import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.analytics.stream.base.idgen.internal.GlobalMessageIdGenerator;
import org.eclipse.ecsp.domain.DeviceMessageFailureEventDataV1_0;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.ro.RoDAOMongoImpl;
import org.eclipse.ecsp.ro.processor.RCPDHandler;
import org.eclipse.ecsp.ro.utils.CacheUtil;
import org.eclipse.ecsp.ro.utils.NotificationUtil;
import org.eclipse.ecsp.ro.utils.OutboundUtil;
import org.eclipse.ecsp.services.utils.ServiceUtil;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link RCPDResponseProcessor} class.
 */
@SpringBootTest(classes = org.eclipse.ecsp.Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = {"classpath:/application-test.properties", "classpath:/application-base.properties"})
public class RCPDResponseProcessorTest extends CommonTestBase {

    @Autowired
    private RCPDResponseProcessor rCPDResponseProcessor;

    @MockBean
    StreamProcessingContext spc;

    @MockBean
    private RCPDHandler rcpdEventHandler;

    @MockBean
    private RedissonClient redissonClient;

    @MockBean
    private ServiceUtil serviceUtil;

    @MockBean
    RoDAOMongoImpl roDAOMongo;


    @MockBean
    private GlobalMessageIdGenerator idGenerator;

    @MockBean
    private CacheUtil cacheUtil;

    @MockBean
    private NotificationUtil notificationUtil;

    @MockBean
    private OutboundUtil outboundUtil;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcess_nullRCPDResponse() {
        DeviceMessageFailureEventDataV1_0 eventData = new DeviceMessageFailureEventDataV1_0();
        eventData.setFailedIgniteEvent(new IgniteEventImpl());
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setVehicleId("vehicleId");
        igniteEvent.setEventData(eventData);
        IgniteStringKey key = new IgniteStringKey();
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(key, igniteEvent, System.currentTimeMillis());
        when(rcpdEventHandler.processRCPDResponse(any(), any(), any())).thenReturn(null);
        // Act
        rCPDResponseProcessor.process(kafkaRecord, spc);
        // Assert
        verify(spc, never()).forward(any());
    }

    @Test
    public void testProcess_completeRCPDResponse_NoDffQualifier() {
        DeviceMessageFailureEventDataV1_0 eventData = new DeviceMessageFailureEventDataV1_0();
        eventData.setFailedIgniteEvent(new IgniteEventImpl());
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setVehicleId("vehicleId");
        igniteEvent.setEventData(eventData);
        IgniteStringKey key = new IgniteStringKey();
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(key, igniteEvent, System.currentTimeMillis());
        when(rcpdEventHandler.processRCPDResponse(any(), any(), any())).thenReturn(igniteEvent);
        // Act
        rCPDResponseProcessor.process(kafkaRecord, spc);
        // Assert
        verify(spc, never()).forward(any());
    }

    @Test
    public void testProcess_completeRCPDResponse_PresentDffQualifier() {
        DeviceMessageFailureEventDataV1_0 eventData = new DeviceMessageFailureEventDataV1_0();
        eventData.setFailedIgniteEvent(new IgniteEventImpl());
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setVehicleId("vehicleId");
        igniteEvent.setEventData(eventData);
        igniteEvent.setDFFQualifier("DFFQualifier");
        IgniteStringKey key = new IgniteStringKey();
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(key, igniteEvent, System.currentTimeMillis());
        when(rcpdEventHandler.processRCPDResponse(any(), any(), any())).thenReturn(igniteEvent);
        // Act
        rCPDResponseProcessor.process(kafkaRecord, spc);
        // Assert
        verify(spc, times(1)).forward(any());
    }

}
