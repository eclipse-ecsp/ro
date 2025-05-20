package org.eclipse.ecsp.ro.processor;

import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.DeviceMessageFailureEventDataV1_0;
import org.eclipse.ecsp.domain.remoteInhibit.CrankNotificationDataV1_0;
import org.eclipse.ecsp.domain.ro.RemoteOperationDoorsV1_1;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.entities.dma.DeviceMessageErrorCode;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.ro.RoDAOMongoImpl;
import org.eclipse.ecsp.ro.processor.strategy.impl.DeviceMessageFailureStreamProcessor;
import org.eclipse.ecsp.ro.queue.DeviceMessageFailureQueueHandler;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

/**
 * Test for {@link DeviceMessageFailureStreamProcessor} class.
 */
@SpringBootTest(classes = org.eclipse.ecsp.Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(value = {"classpath:/application-test.properties", "classpath:/application-base.properties"})
public class DeviceMessageFailureStreamProcessorTest extends CommonTestBase {

    @Autowired
    private DeviceMessageFailureStreamProcessor deviceMessageFailureStreamProcessor;

    @MockBean
    RCPDHandler rcpdEventHandler;

    @MockBean
    RemoteInhibitDataHandler riEventHandler;

    @MockBean
    StreamProcessingContext spc;

    @MockBean
    RoDAOMongoImpl roDAOMongo;

    @MockBean
    private DeviceMessageFailureQueueHandler deviceMessageFailureQueueHandler;

    @Test
    public void testProcess_noFailedEventErrorCode_RoAbstractEventData() {
        DeviceMessageFailureEventDataV1_0 eventData = new DeviceMessageFailureEventDataV1_0();
        eventData.setFailedIgniteEvent(new IgniteEventImpl());
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setVehicleId("vehicleId");
        igniteEvent.setEventData(eventData);
        IgniteStringKey key = new IgniteStringKey();
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(key, igniteEvent, System.currentTimeMillis());
        deviceMessageFailureStreamProcessor.process(kafkaRecord, spc);
        Mockito.verify(rcpdEventHandler, times(0)).handleDeviceMessageFailure(any(), any(), any());
        Mockito.verify(riEventHandler, times(0)).setQualifier(any(), any(), any());
        Mockito.verify(roDAOMongo, times(0)).getROEntityByFieldNameByRoReqIdExceptACV(any(), any());
    }

    @Test
    public void testProcess_failedEventErrorCode_RoAbstractEventData() {
        DeviceMessageFailureEventDataV1_0 eventData = new DeviceMessageFailureEventDataV1_0();
        IgniteEventImpl failedEvent = new IgniteEventImpl();
        failedEvent.setVehicleId("vehicleId");
        failedEvent.setEventData(new RemoteOperationDoorsV1_1());
        eventData.setFailedIgniteEvent(failedEvent);
        eventData.setErrorCode(DeviceMessageErrorCode.DEVICE_DELIVERY_CUTOFF_EXCEEDED);
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setVehicleId("vehicleId");
        igniteEvent.setEventData(eventData);
        IgniteStringKey key = new IgniteStringKey();
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(key, igniteEvent, System.currentTimeMillis());
        deviceMessageFailureStreamProcessor.process(kafkaRecord, spc);
        Mockito.verify(rcpdEventHandler, times(0)).handleDeviceMessageFailure(any(), any(), any());
        Mockito.verify(riEventHandler, times(0)).setQualifier(any(), any(), any());
        Mockito.verify(roDAOMongo, times(1)).getROEntityByFieldNameByRoReqId(any(), any());
    }

    @Test
    public void testProcess_failedEventErrorCode_deviceMessageFailuresStoreNo() {
        ReflectionTestUtils.setField(deviceMessageFailureStreamProcessor, "deviceMessageFailuresStore", false);
        DeviceMessageFailureEventDataV1_0 eventData = new DeviceMessageFailureEventDataV1_0();
        IgniteEventImpl failedEvent = new IgniteEventImpl();
        failedEvent.setVehicleId("vehicleId");
        failedEvent.setEventData(new RemoteOperationDoorsV1_1());
        eventData.setFailedIgniteEvent(failedEvent);
        eventData.setErrorCode(DeviceMessageErrorCode.DEVICE_DELIVERY_CUTOFF_EXCEEDED);
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setVehicleId("vehicleId");
        igniteEvent.setEventData(eventData);
        IgniteStringKey key = new IgniteStringKey();
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(key, igniteEvent, System.currentTimeMillis());
        deviceMessageFailureStreamProcessor.process(kafkaRecord, spc);
        Mockito.verify(rcpdEventHandler, times(0)).handleDeviceMessageFailure(any(), any(), any());
        Mockito.verify(riEventHandler, times(0)).setQualifier(any(), any(), any());
        Mockito.verify(deviceMessageFailureQueueHandler, times(1)).process(any(), any(), any());
    }

    @Test
    public void testProcess_failedEventErrorCode_misMatchErrorCode() {
        DeviceMessageFailureEventDataV1_0 eventData = new DeviceMessageFailureEventDataV1_0();
        IgniteEventImpl failedEvent = new IgniteEventImpl();
        failedEvent.setVehicleId("vehicleId");
        failedEvent.setEventData(new RemoteOperationDoorsV1_1());
        eventData.setFailedIgniteEvent(failedEvent);
        eventData.setErrorCode(DeviceMessageErrorCode.RETRYING_DEVICE_MESSAGE);
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setVehicleId("vehicleId");
        igniteEvent.setEventData(eventData);
        IgniteStringKey key = new IgniteStringKey();
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(key, igniteEvent, System.currentTimeMillis());
        deviceMessageFailureStreamProcessor.process(kafkaRecord, spc);
        Mockito.verify(rcpdEventHandler, times(0)).handleDeviceMessageFailure(any(), any(), any());
        Mockito.verify(riEventHandler, times(0)).setQualifier(any(), any(), any());
        Mockito.verify(deviceMessageFailureQueueHandler, times(0)).process(any(), any(), any());
    }

    @Test
    public void testProcess_noFailedEventErrorCode_AbstractRIEventData() {
        DeviceMessageFailureEventDataV1_0 eventData = new DeviceMessageFailureEventDataV1_0();
        eventData.setErrorCode(DeviceMessageErrorCode.RETRY_ATTEMPTS_EXCEEDED);

        CrankNotificationDataV1_0 failedEventData = new CrankNotificationDataV1_0();
        failedEventData.setOrigin("origin");
        failedEventData.setRoRequestId("roRequestId");
        failedEventData.setUserId("userId");
        failedEventData.setCrankAttempted(true);

        IgniteEventImpl failedIgniteEvent = new IgniteEventImpl();
        failedIgniteEvent.setVehicleId("vehicleId");
        failedIgniteEvent.setEventData(failedEventData);

        eventData.setFailedIgniteEvent(failedIgniteEvent);

        IgniteEventImpl value = new IgniteEventImpl();
        value.setEventData(eventData);

        IgniteStringKey key = new IgniteStringKey();
        Record<IgniteKey<?>, IgniteEvent> kafkaRecord = new Record<>(key, value, System.currentTimeMillis());
        deviceMessageFailureStreamProcessor.process(kafkaRecord, spc);
        Mockito.verify(rcpdEventHandler, times(0)).handleDeviceMessageFailure(any(), any(), any());
        Mockito.verify(riEventHandler, times(1)).setQualifier(any(), any(), any());
        Mockito.verify(roDAOMongo, times(0)).getROEntityByFieldNameByRoReqIdExceptACV(any(), any());
    }


}
