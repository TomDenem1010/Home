package trd.home.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class HomeExceptionHandlerTest {

    private final HomeExceptionHandler handler = new HomeExceptionHandler();

    @Test
    void handlesHomeExceptionAsBadRequest() {
        var response = handler.handle(new TestHomeException("Invalid request"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid request", response.getBody().get("message"));
    }

    @Test
    void handlesUnexpectedExceptionAsInternalServerError() {
        var response = handler.handleUnexpectedException(new IllegalStateException("Database unavailable"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().get("message"));
    }

    private static class TestHomeException extends HomeException {

        private TestHomeException(String message) {
            super(message);
        }
    }
}
