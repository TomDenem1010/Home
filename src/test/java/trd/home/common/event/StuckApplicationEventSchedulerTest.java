package trd.home.common.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
}
