package trd.home.tcg.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.common.logging.LogMethodCall;
import trd.home.tcg.exception.ChromeLaunchException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChromeLauncher {

    private static final String STARTED_MESSAGE = "Chrome startup has started.";
    private static final String SUCCESS_MESSAGE = "Chrome was started successfully.";
    private static final String FAILURE_MESSAGE_PREFIX = "Failed to start Chrome: ";

    private final ChromeProcessStarter processStarter;
    private final FrontendNotificationPublisher notificationPublisher;

    @LogMethodCall
    public void start() {
        notificationPublisher.publish(FrontendNotificationType.WARNING, STARTED_MESSAGE);
        try {
            processStarter.start();
        } catch (RuntimeException exception) {
            log.error("Failed to start Chrome for Cardmarket price collection", exception);
            notificationPublisher.publish(
                    FrontendNotificationType.ERROR, FAILURE_MESSAGE_PREFIX + exception.getMessage());
            throw new ChromeLaunchException("Unable to start Chrome", exception);
        }
        notificationPublisher.publish(FrontendNotificationType.SUCCESS, SUCCESS_MESSAGE);
    }
}
