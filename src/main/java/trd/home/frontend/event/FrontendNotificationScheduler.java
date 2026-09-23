package trd.home.frontend.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import trd.home.common.constant.EventType;
import trd.home.common.dto.FrontendEvent;
import trd.home.common.event.ApplicationEventQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class FrontendNotificationScheduler {

    private final ApplicationEventQueue eventQueue;
    private final FrontendEventService frontendEventService;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${frontend.scheduler.notification.delay:1s}")
    public void processNextEvent() {
        for (var event : eventQueue.findPending(EventType.FRONTEND_NOTIFICATION, 100)) {
            FrontendEvent frontendEvent;
            try {
                frontendEvent = objectMapper.readValue(event.getMessage(), FrontendEvent.class);
            } catch (RuntimeException exception) {
                log.error("Failed to deserialize frontend notification event '{}'", event.getId(), exception);
                event.markFailed(exception);
                eventQueue.save(event);
                continue;
            }
            if (!frontendEventService.hasConnection(frontendEvent.username())) {
                continue;
            }

            if (frontendEventService.sendToUser(frontendEvent.username(), event.getId(), frontendEvent)) {
                return;
            }
        }
    }
}
