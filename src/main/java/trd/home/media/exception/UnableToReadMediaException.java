package trd.home.media.exception;

import trd.home.common.exception.HomeException;

public class UnableToReadMediaException extends HomeException {
    public UnableToReadMediaException(String message) {
        super(message);
    }

    public UnableToReadMediaException(String message, Throwable cause) {
        super(message, cause);
    }
}
