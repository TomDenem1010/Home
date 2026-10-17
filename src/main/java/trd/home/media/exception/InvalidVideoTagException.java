package trd.home.media.exception;

import trd.home.common.exception.HomeException;

public class InvalidVideoTagException extends HomeException {
    public InvalidVideoTagException(String message) {
        super(message);
    }
}
