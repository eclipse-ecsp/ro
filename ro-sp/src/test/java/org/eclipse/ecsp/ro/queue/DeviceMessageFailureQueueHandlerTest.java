package org.eclipse.ecsp.ro.queue;

import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.DeviceMessageFailureEventDataV1_0;
import org.eclipse.ecsp.entities.AbstractIgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.entities.dma.DeviceMessageErrorCode;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.ro.RoDAOMongoImpl;
import org.eclipse.ecsp.services.utils.ServiceUtil;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link DeviceMessageFailureQueueHandler} class.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DeviceMessageFailureQueueHandler.class)
@TestPropertySource(value = "/application-test.properties")
@TestMethodOrder(MethodOrderer.MethodName.class)
public class DeviceMessageFailureQueueHandlerTest {

    @Mock
    private IgniteLogger LOGGER;

    @MockBean
    private StreamProcessingContext ctxt;

    @MockBean
    private RedissonClient redissonClient;

    @MockBean
    private ServiceUtil serviceUtil;

    @Mock
    private RQueue<AbstractIgniteEvent> queue;

    @Mock
    private AbstractIgniteEvent headEvent;

    @MockBean
    RoDAOMongoImpl roDAOMongo;

    @Autowired
    private DeviceMessageFailureQueueHandler deviceMessageFailureQueueHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldProcessPickableDeviceMessageErrorCode_headEventMismatch() {
        DeviceMessageFailureEventDataV1_0 dmFailureEventData = new DeviceMessageFailureEventDataV1_0();
        dmFailureEventData.setErrorCode(DeviceMessageErrorCode.DEVICE_DELIVERY_CUTOFF_EXCEEDED);
        IgniteEventImpl failedEvent = new IgniteEventImpl();
        failedEvent.setRequestId("REQUEST_ID");
        failedEvent.setVehicleId("VEHICLE_ID");
        dmFailureEventData.setFailedIgniteEvent(failedEvent);
        IgniteEventImpl value = new IgniteEventImpl();
        value.setEventData(dmFailureEventData);

        when(roDAOMongo.getROEntityByFieldNameByRoReqIdExceptACV(any(), any())).thenReturn(Optional.empty());
        when(redissonClient.getQueue(anyString())).thenAnswer(invocation -> queue);
        when(queue.peek()).thenReturn(headEvent);
        when(queue.iterator()).thenReturn(null);
        when(headEvent.getRequestId()).thenReturn("REQUEST_ID_2");

        IgniteKey key = new IgniteStringKey("hello");
        Assertions.assertThrows(NullPointerException.class,
                () -> deviceMessageFailureQueueHandler.process(key, value, ctxt));

        Mockito.verify(roDAOMongo, times(1)).getROEntityByFieldNameByRoReqIdExceptACV(any(), any());
        Mockito.verify(redissonClient, times(1)).getQueue(anyString());
        Mockito.verify(queue, times(1)).peek();
        Mockito.verify(queue, times(1)).iterator();
        verify(queue, never()).poll();
    }


    @Test
    void shouldProcessPickableDeviceMessageErrorCode_headEventMatch() {
        DeviceMessageFailureEventDataV1_0 dmFailureEventData = new DeviceMessageFailureEventDataV1_0();
        dmFailureEventData.setErrorCode(DeviceMessageErrorCode.DEVICE_DELIVERY_CUTOFF_EXCEEDED);
        IgniteEventImpl failedEvent = new IgniteEventImpl();
        failedEvent.setRequestId("REQUEST_ID");
        failedEvent.setVehicleId("VEHICLE_ID");
        dmFailureEventData.setFailedIgniteEvent(failedEvent);
        IgniteEventImpl value = new IgniteEventImpl();
        value.setEventData(dmFailureEventData);

        when(roDAOMongo.getROEntityByFieldNameByRoReqIdExceptACV(any(), any())).thenReturn(Optional.empty());
        when(redissonClient.getQueue(anyString())).thenAnswer(invocation -> queue);
        when(queue.peek()).thenReturn(headEvent);
        when(queue.iterator()).thenReturn(null);
        when(headEvent.getRequestId()).thenReturn("REQUEST_ID");

        IgniteKey key = new IgniteStringKey("hello");
        Assertions.assertThrows(NullPointerException.class,
                () -> deviceMessageFailureQueueHandler.process(key, value, ctxt));

        Mockito.verify(roDAOMongo, times(1)).getROEntityByFieldNameByRoReqIdExceptACV(any(), any());
        Mockito.verify(redissonClient, times(1)).getQueue(anyString());
        Mockito.verify(queue, times(1)).peek();
        Mockito.verify(queue, times(1)).iterator();
        verify(queue, times(1)).poll();
    }

    @Test
    void shouldProcessPickableDeviceMessageErrorCode_nullQueue() {
        DeviceMessageFailureEventDataV1_0 dmFailureEventData = new DeviceMessageFailureEventDataV1_0();
        dmFailureEventData.setErrorCode(DeviceMessageErrorCode.DEVICE_DELIVERY_CUTOFF_EXCEEDED);
        IgniteEventImpl failedEvent = new IgniteEventImpl();
        failedEvent.setRequestId("REQUEST_ID");
        failedEvent.setVehicleId("VEHICLE_ID");
        dmFailureEventData.setFailedIgniteEvent(failedEvent);
        IgniteEventImpl value = new IgniteEventImpl();
        value.setEventData(dmFailureEventData);

        when(roDAOMongo.getROEntityByFieldNameByRoReqIdExceptACV(any(), any())).thenReturn(Optional.empty());
        when(redissonClient.getQueue(anyString())).thenReturn(null);
        when(queue.peek()).thenReturn(headEvent);
        when(queue.iterator()).thenReturn(null);
        when(headEvent.getRequestId()).thenReturn("REQUEST_ID_2");

        IgniteKey key = new IgniteStringKey("hello");
        Assertions.assertThrows(NullPointerException.class,
                () -> deviceMessageFailureQueueHandler.process(key, value, ctxt));

        Mockito.verify(roDAOMongo, times(1)).getROEntityByFieldNameByRoReqIdExceptACV(any(), any());
        Mockito.verify(redissonClient, times(1)).getQueue(anyString());
        Mockito.verify(queue, times(0)).peek();
        Mockito.verify(queue, times(0)).iterator();
        verify(queue, never()).poll();
    }

    @Test
    void shouldProcessPickableDeviceMessageErrorCode_nullHeadEvent() {
        DeviceMessageFailureEventDataV1_0 dmFailureEventData = new DeviceMessageFailureEventDataV1_0();
        dmFailureEventData.setErrorCode(DeviceMessageErrorCode.DEVICE_DELIVERY_CUTOFF_EXCEEDED);
        IgniteEventImpl failedEvent = new IgniteEventImpl();
        failedEvent.setRequestId("REQUEST_ID");
        failedEvent.setVehicleId("VEHICLE_ID");
        dmFailureEventData.setFailedIgniteEvent(failedEvent);
        IgniteEventImpl value = new IgniteEventImpl();
        value.setEventData(dmFailureEventData);

        when(roDAOMongo.getROEntityByFieldNameByRoReqIdExceptACV(any(), any())).thenReturn(Optional.empty());
        when(redissonClient.getQueue(anyString())).thenAnswer(invocation -> queue);
        when(queue.peek()).thenReturn(null);
        when(queue.iterator()).thenReturn(null);
        when(headEvent.getRequestId()).thenReturn("REQUEST_ID_2");

        IgniteKey key = new IgniteStringKey("hello");
        Assertions.assertThrows(NullPointerException.class,
                () -> deviceMessageFailureQueueHandler.process(key, value, ctxt));

        Mockito.verify(roDAOMongo, times(1)).getROEntityByFieldNameByRoReqIdExceptACV(any(), any());
        Mockito.verify(redissonClient, times(1)).getQueue(anyString());
        Mockito.verify(queue, times(1)).peek();
        Mockito.verify(queue, times(1)).iterator();
        verify(queue, never()).poll();
    }

    @Test
    void shouldProcessPickableDeviceMessageErrorCode_errorCodeNotPresent() {
        DeviceMessageFailureEventDataV1_0 dmFailureEventData = new DeviceMessageFailureEventDataV1_0();
        dmFailureEventData.setErrorCode(DeviceMessageErrorCode.RETRYING_DEVICE_MESSAGE);
        IgniteEventImpl failedEvent = new IgniteEventImpl();
        failedEvent.setRequestId("REQUEST_ID");
        failedEvent.setVehicleId("VEHICLE_ID");
        dmFailureEventData.setFailedIgniteEvent(failedEvent);
        IgniteEventImpl value = new IgniteEventImpl();
        value.setEventData(dmFailureEventData);

        when(roDAOMongo.getROEntityByFieldNameByRoReqIdExceptACV(any(), any())).thenReturn(Optional.empty());
        when(redissonClient.getQueue(anyString())).thenAnswer(invocation -> queue);
        when(queue.peek()).thenReturn(headEvent);
        when(queue.iterator()).thenReturn(null);
        when(headEvent.getRequestId()).thenReturn("REQUEST_ID_2");

        IgniteKey key = new IgniteStringKey("hello");
        Assertions.assertDoesNotThrow(() -> deviceMessageFailureQueueHandler.process(key, value, ctxt));

        Mockito.verify(roDAOMongo, times(0)).getROEntityByFieldNameByRoReqIdExceptACV(any(), any());
        Mockito.verify(redissonClient, times(0)).getQueue(anyString());
        Mockito.verify(queue, times(0)).peek();
        Mockito.verify(queue, times(0)).iterator();
        verify(queue, never()).poll();
    }

}
