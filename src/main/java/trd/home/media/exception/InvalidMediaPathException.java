package trd.home.media.exception;

import trd.home.common.exception.HomeException;

public class InvalidMediaPathException extends HomeException {
    public InvalidMediaPathException(String message) {
        super(message);
    }

    public InvalidMediaPathException(String message, Throwable cause) {
        super(message, cause);
    }
}
