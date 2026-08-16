package trd.home.common.event;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.dto.FrontendEvent;
import trd.home.common.repository.ApplicationEventRepository;

@Component
public class StuckApplicationEventScheduler {

    private static final int MAX_EVENTS_IN_NOTIFICATION = 20;

    private final ApplicationEventRepository eventRepository;
    private final FrontendNotificationPublisher notificationPublisher;
    private final ObjectMapper objectMapper;
    private final Duration processingTimeout;

    public StuckApplicationEventScheduler(
            ApplicationEventRepository eventRepository,
            FrontendNotificationPublisher notificationPublisher,
            ObjectMapper objectMapper,
            @Value("${event.processing-timeout:1h}") Duration processingTimeout) {
        this.eventRepository = eventRepository;
        this.notificationPublisher = notificationPublisher;
        this.objectMapper = objectMapper;
        this.processingTimeout = processingTimeout;
    }

    @Scheduled(fixedDelayString = "${event.stuck.scheduler.delay:1m}")
    public void failStuckEvents() {
        List<ApplicationEvent> stuckEvents =
                eventRepository.findAllByStatusAndLastModifiedAtBeforeOrderByLastModifiedAtAsc(
                        EventStatus.PROCESSING, Instant.now().minus(processingTimeout));
        if (stuckEvents.isEmpty()) {
            return;
        }

        stuckEvents.forEach(event -> event.markFailed(
                new IllegalStateException("Event remained in PROCESSING state longer than " + processingTimeout)));
        eventRepository.saveAll(stuckEvents);

        stuckEvents.stream()
                .collect(Collectors.groupingBy(this::recipientUsername))
                .forEach((username, events) -> notificationPublisher.publish(
                        username, FrontendNotificationType.ERROR, stuckEventMessage(events)));
    }

    private String recipientUsername(ApplicationEvent event) {
        if (event.getType() == EventType.FRONTEND_NOTIFICATION) {
            try {
                return objectMapper
                        .readValue(event.getMessage(), FrontendEvent.class)
                        .username();
            } catch (RuntimeException ignored) {
                // Fall back to the audit owner for malformed frontend events.
            }
        }
        return Objects.requireNonNullElse(event.getCreatedBy(), "system");
    }

    private String stuckEventMessage(List<ApplicationEvent> events) {
        String eventList = events.stream()
                .limit(MAX_EVENTS_IN_NOTIFICATION)
                .map(event -> event.getType() + " (" + event.getId() + ")")
                .collect(Collectors.joining(", "));
        int remaining = events.size() - MAX_EVENTS_IN_NOTIFICATION;
        return "The following events timed out while processing: "
                + eventList
                + (remaining > 0 ? " and " + remaining + " more" : "");
    }
}
