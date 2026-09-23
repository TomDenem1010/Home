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
import trd.home.common.event.ApplicationEventQueue;
import trd.home.common.event.FrontendNotificationType;

class FrontendNotificationSchedulerTest {

    private final ApplicationEventQueue eventQueue = mock(ApplicationEventQueue.class);
    private final FrontendEventService frontendEventService = mock(FrontendEventService.class);
    private final FrontendNotificationScheduler scheduler = new FrontendNotificationScheduler(
            eventQueue, frontendEventService, JsonMapper.builder().build());

    @Test
    void marksNotificationDoneAfterSendingIt() {
        ApplicationEvent event = frontendEvent("alice", "SUCCESS", "Done");
        pendingEvent(event);
        when(frontendEventService.hasConnection("alice")).thenReturn(true);
        when(frontendEventService.sendToUser(eq("alice"), any(), any())).thenReturn(true);

        scheduler.processNextEvent();

        ArgumentCaptor<FrontendEvent> frontendEventCaptor = ArgumentCaptor.forClass(FrontendEvent.class);
        InOrder order = inOrder(eventQueue, frontendEventService);
        order.verify(frontendEventService).sendToUser(eq("alice"), any(), frontendEventCaptor.capture());
        FrontendEvent frontendEvent = frontendEventCaptor.getValue();
        assertEquals("alice", frontendEvent.username());
        assertEquals(FrontendNotificationType.SUCCESS, frontendEvent.type());
        assertEquals("Done", frontendEvent.message());
        assertEquals(EventStatus.TO_DO, event.getStatus());
        verify(eventQueue, never()).save(event);
    }

    @Test
    void keepsNotificationPendingWhenRecipientDisconnectsBeforeSending() {
        ApplicationEvent event = frontendEvent("alice", "SUCCESS", "Done");
        pendingEvent(event);
        when(frontendEventService.hasConnection("alice")).thenReturn(true);
        when(frontendEventService.sendToUser(eq("alice"), any(), any())).thenReturn(false);

        scheduler.processNextEvent();

        assertEquals(EventStatus.TO_DO, event.getStatus());
        verify(eventQueue, never()).save(event);
    }

    @Test
    void keepsNotificationPendingWithoutActiveRecipient() {
        ApplicationEvent event = frontendEvent("alice", "SUCCESS", "Done");
        pendingEvent(event);

        scheduler.processNextEvent();

        assertEquals(EventStatus.TO_DO, event.getStatus());
        verify(eventQueue, never()).save(event);
        verify(frontendEventService, never()).sendToUser(any(), any(), any());
    }

    @Test
    void marksInvalidNotificationAsFailed() {
        ApplicationEvent event = new ApplicationEvent(EventType.FRONTEND_NOTIFICATION, "not-json");
        pendingEvent(event);

        scheduler.processNextEvent();

        assertEquals(EventStatus.ERROR, event.getStatus());
        assertNotNull(event.getErrorMessage());
    }

    @Test
    void continuesAfterFailedDeliveryAndStopsAfterSuccessfulDelivery() {
        ApplicationEvent disconnected = frontendEvent("alice", "SUCCESS", "First");
        ApplicationEvent delivered = frontendEvent("bob", "SUCCESS", "Second");
        when(eventQueue.findPending(EventType.FRONTEND_NOTIFICATION, 100)).thenReturn(List.of(disconnected, delivered));
        when(frontendEventService.hasConnection(any())).thenReturn(true);
        when(frontendEventService.sendToUser(eq("alice"), any(), any())).thenReturn(false);
        when(frontendEventService.sendToUser(eq("bob"), any(), any())).thenReturn(true);

        scheduler.processNextEvent();

        assertEquals(EventStatus.TO_DO, disconnected.getStatus());
        assertEquals(EventStatus.TO_DO, delivered.getStatus());
        verify(eventQueue, never()).save(delivered);
        verify(frontendEventService).sendToUser(eq("alice"), any(), any());
        verify(frontendEventService).sendToUser(eq("bob"), any(), any());
    }

    private ApplicationEvent frontendEvent(String username, String type, String message) {
        return new ApplicationEvent(
                EventType.FRONTEND_NOTIFICATION,
                "{\"username\":\"" + username + "\",\"type\":\"" + type + "\",\"message\":\"" + message + "\"}");
    }

    private void pendingEvent(ApplicationEvent event) {
        when(eventQueue.findPending(EventType.FRONTEND_NOTIFICATION, 100)).thenReturn(List.of(event));
    }
}
