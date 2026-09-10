package trd.home.common.exception;

import java.util.Collections;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import trd.home.common.logging.LogMethodCall;

@RestControllerAdvice
public class HomeExceptionHandler {

    private static final String UNEXPECTED_ERROR_MESSAGE = "An unexpected error occurred";

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleDisconnectedClient() {
        // The SSE response is already unusable, so no error body can or should be written.
    }

    @ExceptionHandler(HomeException.class)
    @LogMethodCall
    public ResponseEntity<Map<String, String>> handle(HomeException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap("message", exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    @LogMethodCall
    public ResponseEntity<Map<String, String>> handleUnexpectedException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", UNEXPECTED_ERROR_MESSAGE));
    }
}
