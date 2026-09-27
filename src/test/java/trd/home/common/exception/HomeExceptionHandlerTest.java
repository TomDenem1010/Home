package trd.home.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;

class HomeExceptionHandlerTest {

    private final FrontendNotificationPublisher notificationPublisher = mock(FrontendNotificationPublisher.class);
    private final HomeExceptionHandler handler = new HomeExceptionHandler(notificationPublisher);

    @Test
    void handlesHomeExceptionWithErrorNotification() {
        handler.handle(new TestHomeException("Invalid request"));
        verify(notificationPublisher).publish(FrontendNotificationType.ERROR, "Invalid request");
    }

    @Test
    void handlesUnexpectedExceptionAsInternalServerError() {
        var response = handler.handleUnexpectedException(new IllegalStateException("Database unavailable"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().get("message"));
        verifyNoInteractions(notificationPublisher);
    }

    @Test
    void ignoresDisconnectedAsyncClient() {
        handler.handleDisconnectedClient();
        verifyNoInteractions(notificationPublisher);
    }

    private static class TestHomeException extends HomeException {

        private TestHomeException(String message) {
            super(message);
        }
    }
}
