package trd.home.common.event;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.dto.FrontendEvent;
import trd.home.common.exception.EventProcessingTimeoutException;

@Slf4j
@Service
public class StuckApplicationEventService {

    private static final int MAX_EVENTS_IN_NOTIFICATION = 20;
    private static final String SYSTEM_USER = "system";

    private final ApplicationEventQueue eventQueue;
    private final FrontendNotificationPublisher notificationPublisher;
    private final ObjectMapper objectMapper;
    private final Duration processingTimeout;

    public StuckApplicationEventService(
            ApplicationEventQueue eventQueue,
            FrontendNotificationPublisher notificationPublisher,
            ObjectMapper objectMapper,
            @Value("${event.processing-timeout:1h}") Duration processingTimeout) {
        this.eventQueue = eventQueue;
        this.notificationPublisher = notificationPublisher;
        this.objectMapper = objectMapper;
        this.processingTimeout = processingTimeout;
    }

    public void failStuckEvents() {
        List<ApplicationEvent> stuckEvents = findStuckEvents();
        if (stuckEvents.isEmpty()) {
            return;
        }

        markFailed(stuckEvents);
        notifyUsers(stuckEvents);
        log.info("Published notifications for {} stuck events.", stuckEvents.size());
    }

    private List<ApplicationEvent> findStuckEvents() {
        return eventQueue.findStuckBefore(Instant.now().minus(processingTimeout));
    }

    private void markFailed(List<ApplicationEvent> events) {
        var timeoutException = new EventProcessingTimeoutException(processingTimeout);
        events.forEach(event -> event.markFailed(timeoutException));
        eventQueue.saveAll(events);
    }

    private void notifyUsers(List<ApplicationEvent> events) {
        events.stream()
                .collect(Collectors.groupingBy(event -> recipientUsername(event)))
                .forEach((username, userEvents) -> notificationPublisher.publish(
                        username, FrontendNotificationType.ERROR, timeoutMessage(userEvents)));
    }

    private String recipientUsername(ApplicationEvent event) {
        if (event.getType() == EventType.FRONTEND_NOTIFICATION) {
            try {
                return objectMapper
                        .readValue(event.getMessage(), FrontendEvent.class)
                        .username();
            } catch (RuntimeException exception) {
                log.error("Unable to read the recipient of frontend event '{}'.", event.getId(), exception);
            }
        }
        return Objects.requireNonNullElse(event.getCreatedBy(), SYSTEM_USER);
    }

    private String timeoutMessage(List<ApplicationEvent> events) {
        String eventList = events.stream()
                .limit(MAX_EVENTS_IN_NOTIFICATION)
                .map(event -> event.getType() + " (" + event.getId() + ")")
                .collect(Collectors.joining(", "));
        int remainingCount = events.size() - MAX_EVENTS_IN_NOTIFICATION;
        String remaining = remainingCount > 0 ? " and " + remainingCount + " more" : "";
        return "The following events timed out while processing: " + eventList + remaining;
    }
}
