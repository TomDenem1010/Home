package trd.home.tcg.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.common.repository.ApplicationEventRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TcgEventProcessor {

    private final ApplicationEventRepository eventRepository;
    private final FrontendNotificationPublisher notificationPublisher;

    public void process(
            ApplicationEvent event, Runnable operation, String successMessage, String failureMessagePrefix) {
        event.markProcessing();
        eventRepository.save(event);

        FrontendNotificationType notificationType;
        String notificationMessage;
        try {
            operation.run();
            event.markDone();
            notificationType = FrontendNotificationType.SUCCESS;
            notificationMessage = successMessage;
        } catch (RuntimeException exception) {
            log.error("Failed to process TCG event '{}'", event.getType(), exception);
            event.markFailed(exception);
            notificationType = FrontendNotificationType.ERROR;
            notificationMessage = failureMessagePrefix + exception.getMessage();
        }

        eventRepository.save(event);
        notificationPublisher.publish(event.getCreatedBy(), notificationType, notificationMessage);
    }
}
