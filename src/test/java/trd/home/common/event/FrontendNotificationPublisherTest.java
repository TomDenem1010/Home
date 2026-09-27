package trd.home.common.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import trd.home.common.constant.EventType;
import trd.home.common.exception.UnableToSerializeNotificationException;

class FrontendNotificationPublisherTest {
    @Test
    void preservesSerializationFailureWithoutEnqueueingNotification() {
        var mapper = mock(ObjectMapper.class);
        var cause = mock(JacksonException.class);
        Mockito.when(mapper.writeValueAsString(ArgumentMatchers.any())).thenThrow(cause);
        var failingPublisher = new FrontendNotificationPublisher(eventQueue, mapper, Optional::empty);
        var exception = Assertions.assertThrows(
                UnableToSerializeNotificationException.class,
                () -> failingPublisher.publish(FrontendNotificationType.ERROR, "error"));
        Assertions.assertSame(cause, exception.getCause());
        Mockito.verifyNoInteractions(eventQueue);
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
