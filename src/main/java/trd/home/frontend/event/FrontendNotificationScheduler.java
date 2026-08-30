package trd.home.frontend.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.dto.FrontendEvent;
import trd.home.common.repository.ApplicationEventRepository;

@Slf4j
@Component
public class FrontendNotificationScheduler {

    private final ApplicationEventRepository eventRepository;
    private final FrontendEventService frontendEventService;
    private final ObjectMapper objectMapper;

    public FrontendNotificationScheduler(
            ApplicationEventRepository eventRepository,
            FrontendEventService frontendEventService,
            ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.frontendEventService = frontendEventService;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${frontend.scheduler.notification.delay:1s}")
    public void processNextEvent() {
        for (var event : eventRepository.findTop100ByTypeAndStatusOrderByCreatedAtAsc(
                EventType.FRONTEND_NOTIFICATION, EventStatus.TO_DO)) {
            FrontendEvent frontendEvent;
            try {
                frontendEvent = objectMapper.readValue(event.getMessage(), FrontendEvent.class);
            } catch (RuntimeException exception) {
                log.error("Failed to deserialize frontend notification event '{}'", event.getId(), exception);
                event.markFailed(exception);
                eventRepository.save(event);
                continue;
            }
            if (!frontendEventService.hasConnection(frontendEvent.username())) {
                continue;
            }

            deliver(event, frontendEvent);
            return;
        }
    }

    private void deliver(ApplicationEvent event, FrontendEvent frontendEvent) {
        event.markProcessing();
        eventRepository.save(event);
        frontendEventService.sendToUser(frontendEvent.username(), frontendEvent);
        event.markDone();
        eventRepository.save(event);
    }
}
