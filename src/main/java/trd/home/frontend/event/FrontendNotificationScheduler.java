package trd.home.frontend.event;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dto.FrontendEvent;
import trd.home.common.repository.ApplicationEventRepository;

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
        eventRepository
                .findFirstByTypeAndStatusOrderByCreatedAtAsc(EventType.FRONTEND_NOTIFICATION, EventStatus.TO_DO)
                .ifPresent(event -> {
                    event.markProcessing();
                    eventRepository.save(event);
                    try {
                        FrontendEvent frontendEvent = objectMapper.readValue(event.getMessage(), FrontendEvent.class);
                        frontendEventService.sendToAll(frontendEvent);
                        event.markDone();
                    } catch (RuntimeException exception) {
                        event.markFailed(exception);
                    }
                    eventRepository.save(event);
                });
    }
}
