package trd.home.common.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.repository.ApplicationEventRepository;

class FrontendNotificationPublisherTest {

    private final ApplicationEventRepository eventRepository = mock(ApplicationEventRepository.class);
    private final FrontendNotificationPublisher publisher = new FrontendNotificationPublisher(
            eventRepository, JsonMapper.builder().build(), () -> Optional.of("alice"));

    @Test
    void serializesNotificationIntoApplicationEvent() {
        publisher.publish(FrontendNotificationType.WARNING, "Warning");

        ArgumentCaptor<ApplicationEvent> eventCaptor = ArgumentCaptor.forClass(ApplicationEvent.class);
        verify(eventRepository).save(eventCaptor.capture());
        ApplicationEvent event = eventCaptor.getValue();
        assertEquals(EventType.FRONTEND_NOTIFICATION, event.getType());
        assertEquals(EventStatus.TO_DO, event.getStatus());
        assertEquals("{\"username\":\"alice\",\"type\":\"WARNING\",\"message\":\"Warning\"}", event.getMessage());
    }
}
