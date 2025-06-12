package org.eclipse.ecsp.ro.utils;

import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.GenericCustomExtension;
import org.eclipse.ecsp.domain.ro.RemoteOperationResponseV1_1;
import org.eclipse.ecsp.domain.ro.RemoteOperationResponseV1_1.Response;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.ro.notification.NotificationResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Test for {@link NotificationUtil} class.
 */
public class NotificationUtilTest {

    @InjectMocks
    private NotificationUtil notificationUtil;

    @Mock
    private NotificationResolver notificationResolver;

    @Mock
    private StreamProcessingContext context;

    @Mock
    private IgniteKey igniteKey;

    @Mock
    private IgniteEvent igniteEvent;

    @Mock
    private RemoteOperationResponseV1_1 response;

    @Mock
    private GenericCustomExtension customExtension;

    /**
     * Initializes mocks and sets up the test environment before each test case.
     */
    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        notificationUtil = new NotificationUtil();

        // Inject private fields using reflection directly
        setField(notificationUtil, "notificationStatusMapping", Map.of("notif-1", "SUCCESS"));
        setField(notificationUtil, "notificationIdMapping", Map.of("RESPONSE_OK", "notif-1"));
        setField(notificationUtil, "whitelistedDffOrigins", new String[]{"THIRDPARTY2"});
        setField(notificationUtil, "sinkTopics", new String[]{"sink-topic"});
        setField(notificationUtil, "sourceTopics", new String[]{"source-topic"});
        setField(notificationUtil, "notificationResolver", notificationResolver);
    }

    // Inline reflection helper inside the test class
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    public void testSendRONotification_withValidInput_shouldForwardEvent() {
        when(igniteEvent.getVehicleId()).thenReturn("veh123");
        when(igniteEvent.getBizTransactionId()).thenReturn("biz123");

        when(response.getRoRequestId()).thenReturn("ro-req-1");
        when(response.getResponse()).thenReturn(Response.SUCCESS);

        when(response.getCustomExtension()).thenReturn(Optional.of(customExtension));
        when(customExtension.getCustomData()).thenReturn(Map.of("response", "SUCCESS"));

        notificationUtil.sendRONotification(
                igniteKey,
                igniteEvent,
                context,
                "THIRDPARTY2",
                "notif-1",
                response
        );

        verify(context).forwardDirectly(any(IgniteKey.class), any(IgniteEvent.class), eq("sink-topic"));
    }

    @Test
    public void testSendRONotification_withNonWhitelistedOrigin_shouldNotSend() {
        notificationUtil.sendRONotification(
                igniteKey,
                igniteEvent,
                context,
                "UNKNOWN_ORIGIN",
                "notif-1",
                response
        );

        verifyNoInteractions(context);
    }

    @Test
    public void testSendRONotification_withEmptyNotificationId_shouldSkip() {
        when(response.getRoRequestId()).thenReturn("ro-req-1");

        notificationUtil.sendRONotification(
                igniteKey,
                igniteEvent,
                context,
                "THIRDPARTY2",
                "",
                response
        );

        verifyNoInteractions(context);
    }
}