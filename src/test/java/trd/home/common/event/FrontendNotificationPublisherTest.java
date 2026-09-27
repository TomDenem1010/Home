package trd.home.common.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import trd.home.common.constant.EventType;

class FrontendNotificationPublisherTest {
    @Test
    void preservesSerializationFailureWithoutEnqueueingNotification() {
        var mapper = mock(tools.jackson.databind.ObjectMapper.class);
        var cause = mock(tools.jackson.core.JacksonException.class);
        org.mockito.Mockito.when(mapper.writeValueAsString(org.mockito.ArgumentMatchers.any()))
                .thenThrow(cause);
        var failingPublisher = new FrontendNotificationPublisher(eventQueue, mapper, Optional::empty);
        var exception = org.junit.jupiter.api.Assertions.assertThrows(
                trd.home.common.exception.UnableToSerializeNotificationException.class,
                () -> failingPublisher.publish(FrontendNotificationType.ERROR, "error"));
        org.junit.jupiter.api.Assertions.assertSame(cause, exception.getCause());
        org.mockito.Mockito.verifyNoInteractions(eventQueue);
    }

    private final ApplicationEventQueue eventQueue = mock(ApplicationEventQueue.class);
    private final FrontendNotificationPublisher publisher =
            new FrontendNotificationPublisher(eventQueue, JsonMapper.builder().build(), () -> Optional.of("alice"));

    @Test
    void serializesNotificationIntoApplicationEvent() {
        publisher.publish(FrontendNotificationType.WARNING, "Warning");

        verify(eventQueue)
                .enqueue(
                        EventType.FRONTEND_NOTIFICATION,
                        "{\"username\":\"alice\",\"type\":\"WARNING\",\"message\":\"Warning\"}");
    }
}
