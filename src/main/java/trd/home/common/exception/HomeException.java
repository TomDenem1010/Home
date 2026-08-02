package trd.home.common.exception;

public abstract class HomeException extends RuntimeException {

    protected HomeException(String message) {
        super(message);
    }

    protected HomeException(String message, Throwable cause) {
        super(message, cause);
    }
}
