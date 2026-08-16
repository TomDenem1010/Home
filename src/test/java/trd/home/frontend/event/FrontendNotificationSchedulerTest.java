package trd.home.frontend.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import tools.jackson.databind.json.JsonMapper;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.dto.FrontendEvent;
import trd.home.common.event.FrontendNotificationType;
import trd.home.common.repository.ApplicationEventRepository;

class FrontendNotificationSchedulerTest {

    private final ApplicationEventRepository eventRepository = mock(ApplicationEventRepository.class);
    private final FrontendEventService frontendEventService = mock(FrontendEventService.class);
    private final FrontendNotificationScheduler scheduler = new FrontendNotificationScheduler(
            eventRepository, frontendEventService, JsonMapper.builder().build());

    @Test
    void readsAndDeliversOldestFrontendNotification() {
        ApplicationEvent event =
                new ApplicationEvent(EventType.FRONTEND_NOTIFICATION, "{\"type\":\"SUCCESS\",\"message\":\"Kész\"}");
        when(eventRepository.findFirstByTypeAndStatusOrderByCreatedAtAsc(
                        EventType.FRONTEND_NOTIFICATION, EventStatus.TO_DO))
                .thenReturn(Optional.of(event));

        scheduler.processNextEvent();

        ArgumentCaptor<FrontendEvent> frontendEventCaptor = ArgumentCaptor.forClass(FrontendEvent.class);
        InOrder order = inOrder(eventRepository, frontendEventService);
        order.verify(eventRepository).save(event);
        order.verify(frontendEventService).sendToAll(frontendEventCaptor.capture());
        order.verify(eventRepository).save(event);
        FrontendEvent frontendEvent = frontendEventCaptor.getValue();
        assertEquals(FrontendNotificationType.SUCCESS, frontendEvent.type());
        assertEquals("Kész", frontendEvent.message());
        assertEquals(EventStatus.DONE, event.getStatus());
        assertNotNull(event.getProcessedAt());
    }

    @Test
    void marksInvalidNotificationAsFailed() {
        ApplicationEvent event = new ApplicationEvent(EventType.FRONTEND_NOTIFICATION, "not-json");
        when(eventRepository.findFirstByTypeAndStatusOrderByCreatedAtAsc(
                        EventType.FRONTEND_NOTIFICATION, EventStatus.TO_DO))
                .thenReturn(Optional.of(event));

        scheduler.processNextEvent();

        assertEquals(EventStatus.ERROR, event.getStatus());
        assertNotNull(event.getErrorMessage());
    }
}
