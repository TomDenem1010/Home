package trd.home.common.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import trd.home.common.constant.EventType;

class FrontendNotificationPublisherTest {

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
