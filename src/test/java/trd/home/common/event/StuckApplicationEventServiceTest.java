package trd.home.common.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;

class StuckApplicationEventServiceTest {

    private final ApplicationEventQueue eventQueue = mock(ApplicationEventQueue.class);
    private final FrontendNotificationPublisher notificationPublisher = mock(FrontendNotificationPublisher.class);
    private final StuckApplicationEventService service = new StuckApplicationEventService(
            eventQueue, notificationPublisher, JsonMapper.builder().build(), Duration.ofHours(1));

    @Test
    void failsStuckEventsAndNotifiesTheirFrontendUser() {
        ApplicationEvent event = new ApplicationEvent(
                EventType.FRONTEND_NOTIFICATION, "{\"username\":\"alice\",\"type\":\"SUCCESS\",\"message\":\"Done\"}");
        event.markProcessing();
        givenStuckEvents(List.of(event));

        service.failStuckEvents();

        assertEquals(EventStatus.ERROR, event.getStatus());
        assertNotNull(event.getErrorMessage());
        verify(eventQueue).saveAll(List.of(event));
        verify(notificationPublisher)
                .publish(eq("alice"), eq(FrontendNotificationType.ERROR), contains("FRONTEND_NOTIFICATION"));
    }

    @Test
    void doesNothingWithoutStuckEvents() {
        service.failStuckEvents();

        verifyNoInteractions(notificationPublisher);
    }

    @Test
    void fallsBackToCreatorForMalformedFrontendNotification() {
        ApplicationEvent event = spy(new ApplicationEvent(EventType.FRONTEND_NOTIFICATION, "invalid-json"));
        when(event.getCreatedBy()).thenReturn("creator");
        event.markProcessing();
        givenStuckEvents(List.of(event));

        service.failStuckEvents();

        verify(notificationPublisher).publish(eq("creator"), eq(FrontendNotificationType.ERROR), any());
    }

    @Test
    void limitsLongTimeoutNotificationAndReportsRemainingCount() {
        givenStuckEvents(eventsFor("alice", 22));

        service.failStuckEvents();

        verify(notificationPublisher).publish(eq("alice"), eq(FrontendNotificationType.ERROR), contains("and 2 more"));
    }

    @Test
    void omitsRemainingCountForExactlyTwentyEvents() {
        givenStuckEvents(eventsFor("alice", 20));

        service.failStuckEvents();

        verify(notificationPublisher)
                .publish(
                        eq("alice"),
                        eq(FrontendNotificationType.ERROR),
                        org.mockito.ArgumentMatchers.argThat(message -> !message.contains("more")));
    }

    private void givenStuckEvents(List<ApplicationEvent> events) {
        when(eventQueue.findStuckBefore(any())).thenReturn(events);
    }

    private List<ApplicationEvent> eventsFor(String username, int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> {
                    ApplicationEvent event = spy(new ApplicationEvent(EventType.IMPORT_MEDIA));
                    when(event.getCreatedBy()).thenReturn(username);
                    event.markProcessing();
                    return event;
                })
                .toList();
    }
}
