package trd.home.common.exception;

import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;

@RestControllerAdvice
@RequiredArgsConstructor
public class HomeExceptionHandler {

    private static final String UNEXPECTED_ERROR_MESSAGE = "An unexpected error occurred";
    private final FrontendNotificationPublisher notificationPublisher;

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleDisconnectedClient() {
        // The SSE response is already unusable, so no error body can or should be written.
    }

    @ExceptionHandler(HomeException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handle(HomeException exception) {
        notificationPublisher.publish(FrontendNotificationType.ERROR, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpectedException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", UNEXPECTED_ERROR_MESSAGE));
    }
}
