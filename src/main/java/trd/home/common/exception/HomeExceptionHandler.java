package trd.home.common.exception;

import java.util.Collections;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import trd.home.common.logging.LogMethodCall;

@RestControllerAdvice
public class HomeExceptionHandler {

    private static final String UNEXPECTED_ERROR_MESSAGE = "An unexpected error occurred";

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
