package org.eclipse.ecsp.ro.processor.strategy.impl.rcpd;

import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.ro.processor.RCPDHandler;
import org.eclipse.ecsp.services.utils.ServiceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RCPDRequestProcessorTest - This class contains unit tests for the RCPDRequestProcessor.
 */
@ExtendWith(MockitoExtension.class)
public class RCPDRequestProcessorTest {

    // Mock dependencies using @Mock
    @Mock
    private RCPDHandler rcpdEventHandler;

    @Mock
    private ServiceUtil serviceUtil;

    @InjectMocks
    private RCPDRequestProcessor rcpdRequestProcessor;

    // Define a value for the rcpdMqttTopic, which would normally be injected by Spring
    private static final String MOCK_MQTT_TOPIC = "mock/rcpd/topic";

    /**
     * Set up method executed before each test.
     * Used to inject the mocked @Value property using ReflectionTestUtils.
     */
    @BeforeEach
    void setUp() {
        // Use ReflectionTestUtils to set the private field 'rcpdMqttTopic'
        // This simulates how @Value would inject the property in a real Spring context.
        ReflectionTestUtils.setField(rcpdRequestProcessor, "rcpdMqttTopic", MOCK_MQTT_TOPIC);
    }

    /**
     * Test case for the process method.
     * Verifies that:
     * 1. rcpdEventHandler.processRCPDRequest is called with correct arguments.
     * 2. The returned IgniteEventImpl has the correct devMsgTopicSuffix set.
     * 3. ctxt.forward is called with the correctly constructed Kafka Record.
     */
    @Test
    void testProcess() {
        IgniteKey mockKey = mock(IgniteKey.class);
        IgniteEvent mockValue = mock(IgniteEvent.class);
        Record<IgniteKey<?>, IgniteEvent> kafkaRecordIn = new Record<>(mockKey, mockValue, System.currentTimeMillis());

        StreamProcessingContext mockCtxt = mock(StreamProcessingContext.class);

        IgniteEventImpl mockRcpdReqImpl = mock(IgniteEventImpl.class);

        when(rcpdEventHandler.processRCPDRequest(
                eq(mockKey),
                eq(mockValue),
                eq(serviceUtil)
        )).thenReturn(mockRcpdReqImpl);

        // Act
        rcpdRequestProcessor.process(kafkaRecordIn, mockCtxt);

        // Assert
        verify(rcpdEventHandler).processRCPDRequest(mockKey, mockValue, serviceUtil);

        verify(mockRcpdReqImpl).setDevMsgTopicSuffix(MOCK_MQTT_TOPIC);

        ArgumentCaptor<Record<IgniteKey<?>, IgniteEvent>> recordCaptor = ArgumentCaptor.forClass(Record.class);
        verify(mockCtxt).forward(recordCaptor.capture());

        Record<IgniteKey<?>, IgniteEvent> forwardedRecord = recordCaptor.getValue();

        // Assertions on the forwarded record
        // Verify the key of the forwarded record is the same as the input key
        assertEquals(mockKey, forwardedRecord.key());
        // Verify the value of the forwarded record is the mockRcpdReqImpl (after topic suffix is set)
        assertEquals(mockRcpdReqImpl, forwardedRecord.value());
    }
}