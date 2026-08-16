package trd.home.frontend.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
    void marksNotificationDoneAfterSendingIt() {
        ApplicationEvent event = frontendEvent("alice", "SUCCESS", "Done");
        pendingEvent(event);
        when(frontendEventService.hasConnection("alice")).thenReturn(true);

        scheduler.processNextEvent();

        ArgumentCaptor<FrontendEvent> frontendEventCaptor = ArgumentCaptor.forClass(FrontendEvent.class);
        InOrder order = inOrder(eventRepository, frontendEventService);
        order.verify(eventRepository).save(event);
        order.verify(frontendEventService).sendToUser(eq("alice"), frontendEventCaptor.capture());
        order.verify(eventRepository).save(event);
        FrontendEvent frontendEvent = frontendEventCaptor.getValue();
        assertEquals("alice", frontendEvent.username());
        assertEquals(FrontendNotificationType.SUCCESS, frontendEvent.type());
        assertEquals("Done", frontendEvent.message());
        assertEquals(EventStatus.DONE, event.getStatus());
        assertNotNull(event.getProcessedAt());
    }

    @Test
    void keepsNotificationPendingWithoutActiveRecipient() {
        ApplicationEvent event = frontendEvent("alice", "SUCCESS", "Done");
        pendingEvent(event);

        scheduler.processNextEvent();

        assertEquals(EventStatus.TO_DO, event.getStatus());
        verify(eventRepository, never()).save(event);
        verify(frontendEventService, never()).sendToUser(any(), any());
    }

    @Test
    void marksInvalidNotificationAsFailed() {
        ApplicationEvent event = new ApplicationEvent(EventType.FRONTEND_NOTIFICATION, "not-json");
        pendingEvent(event);

        scheduler.processNextEvent();

        assertEquals(EventStatus.ERROR, event.getStatus());
        assertNotNull(event.getErrorMessage());
    }

    private ApplicationEvent frontendEvent(String username, String type, String message) {
        return new ApplicationEvent(
                EventType.FRONTEND_NOTIFICATION,
                "{\"username\":\"" + username + "\",\"type\":\"" + type + "\",\"message\":\"" + message + "\"}");
    }

    private void pendingEvent(ApplicationEvent event) {
        when(eventRepository.findTop100ByTypeAndStatusOrderByCreatedAtAsc(
                        EventType.FRONTEND_NOTIFICATION, EventStatus.TO_DO))
                .thenReturn(List.of(event));
    }
}
