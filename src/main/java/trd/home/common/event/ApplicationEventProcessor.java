package trd.home.common.event;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import trd.home.common.dao.ApplicationEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationEventProcessor {

    private final ApplicationEventQueue eventQueue;
    private final FrontendNotificationPublisher notificationPublisher;

    public void process(ApplicationEvent event, Supplier<String> operation, String failureMessagePrefix) {
        if (event.getStatus() != trd.home.common.constant.EventStatus.PROCESSING) {
            event.markProcessing();
            eventQueue.save(event);
        }

        FrontendNotificationType notificationType;
        String notificationMessage;
        try {
            notificationMessage = operation.get();
            event.markDone();
            notificationType = FrontendNotificationType.SUCCESS;
        } catch (RuntimeException exception) {
            log.error("Failed to process application event '{}'", event.getType(), exception);
            event.markFailed(exception);
            notificationType = FrontendNotificationType.ERROR;
            notificationMessage = failureMessagePrefix + exception.getMessage();
        }

        eventQueue.save(event);
        notificationPublisher.publish(event.getCreatedBy(), notificationType, notificationMessage);
    }
}
