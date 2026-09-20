package trd.home.common.browser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.common.exception.ChromeLaunchException;

class ChromeBrowserLauncherTest {

    private final ChromeProcessStarter processStarter = mock(ChromeProcessStarter.class);
    private final FrontendNotificationPublisher notificationPublisher = mock(FrontendNotificationPublisher.class);
    private final ChromeBrowserLauncher launcher = new ChromeBrowserLauncher(processStarter, notificationPublisher);

    @Test
    void publishesStartedAndSuccessfulNotifications() {
        launcher.start();

        InOrder order = inOrder(notificationPublisher, processStarter);
        order.verify(notificationPublisher).publish(FrontendNotificationType.WARNING, "Chrome startup has started.");
        order.verify(processStarter).start();
        order.verify(notificationPublisher)
                .publish(FrontendNotificationType.SUCCESS, "Chrome was started successfully.");
    }

    @Test
    void publishesErrorNotificationWhenChromeCannotStart() {
        ChromeLaunchException failure = new ChromeLaunchException("Process unavailable", null);
        org.mockito.Mockito.doThrow(failure).when(processStarter).start();

        assertThrows(ChromeLaunchException.class, launcher::start);

        verify(notificationPublisher)
                .publish(FrontendNotificationType.ERROR, "Failed to start Chrome: Process unavailable");
    }
}
