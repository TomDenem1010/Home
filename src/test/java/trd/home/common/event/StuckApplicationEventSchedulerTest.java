package trd.home.common.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.repository.ApplicationEventRepository;

class StuckApplicationEventSchedulerTest {

    private final ApplicationEventRepository eventRepository = mock(ApplicationEventRepository.class);
    private final FrontendNotificationPublisher notificationPublisher = mock(FrontendNotificationPublisher.class);
    private final StuckApplicationEventScheduler scheduler = new StuckApplicationEventScheduler(
            eventRepository, notificationPublisher, JsonMapper.builder().build(), Duration.ofHours(1));

    @Test
    void failsStuckEventsAndNotifiesTheirFrontendUser() {
        ApplicationEvent event = new ApplicationEvent(
                EventType.FRONTEND_NOTIFICATION, "{\"username\":\"alice\",\"type\":\"SUCCESS\",\"message\":\"Done\"}");
        event.markProcessing();
        when(eventRepository.findAllByStatusAndLastModifiedAtBeforeOrderByLastModifiedAtAsc(
                        eq(EventStatus.PROCESSING), any()))
                .thenReturn(List.of(event));

        scheduler.failStuckEvents();

        assertEquals(EventStatus.ERROR, event.getStatus());
        assertNotNull(event.getErrorMessage());
        verify(eventRepository).saveAll(List.of(event));
        verify(notificationPublisher)
                .publish(
                        eq("alice"),
                        eq(FrontendNotificationType.ERROR),
                        org.mockito.ArgumentMatchers.contains("FRONTEND_NOTIFICATION"));
    }

    @Test
    void doesNothingWithoutStuckEvents() {
        scheduler.failStuckEvents();

        verifyNoInteractions(notificationPublisher);
    }

    @Test
    void fallsBackToCreatorForMalformedFrontendNotification() {
        ApplicationEvent event = new ApplicationEvent(EventType.FRONTEND_NOTIFICATION, "invalid-json");
        event = org.mockito.Mockito.spy(event);
        when(event.getCreatedBy()).thenReturn("creator");
        event.markProcessing();
        when(eventRepository.findAllByStatusAndLastModifiedAtBeforeOrderByLastModifiedAtAsc(
                        eq(EventStatus.PROCESSING), any()))
                .thenReturn(List.of(event));

        scheduler.failStuckEvents();

        verify(notificationPublisher).publish(eq("creator"), eq(FrontendNotificationType.ERROR), any());
    }

    @Test
    void limitsLongTimeoutNotificationAndReportsRemainingCount() {
        List<ApplicationEvent> events = java.util.stream.IntStream.range(0, 22)
                .mapToObj(index -> {
                    ApplicationEvent event = org.mockito.Mockito.spy(new ApplicationEvent(EventType.IMPORT_MEDIA));
                    when(event.getCreatedBy()).thenReturn("alice");
                    event.markProcessing();
                    return event;
                })
                .toList();
        when(eventRepository.findAllByStatusAndLastModifiedAtBeforeOrderByLastModifiedAtAsc(
                        eq(EventStatus.PROCESSING), any()))
                .thenReturn(events);

        scheduler.failStuckEvents();

        verify(notificationPublisher)
                .publish(
                        eq("alice"),
                        eq(FrontendNotificationType.ERROR),
                        org.mockito.ArgumentMatchers.contains("and 2 more"));
    }

    @Test
    void doesNotReportRemainingCountForExactlyTwentyEvents() {
        List<ApplicationEvent> events = java.util.stream.IntStream.range(0, 20)
                .mapToObj(index -> {
                    ApplicationEvent event = org.mockito.Mockito.spy(new ApplicationEvent(EventType.IMPORT_MEDIA));
                    when(event.getCreatedBy()).thenReturn("alice");
                    event.markProcessing();
                    return event;
                })
                .toList();
        when(eventRepository.findAllByStatusAndLastModifiedAtBeforeOrderByLastModifiedAtAsc(
                        eq(EventStatus.PROCESSING), any()))
                .thenReturn(events);

        scheduler.failStuckEvents();

        verify(notificationPublisher)
                .publish(
                        eq("alice"),
                        eq(FrontendNotificationType.ERROR),
                        org.mockito.ArgumentMatchers.argThat(message -> !message.contains("more")));
    }
}
