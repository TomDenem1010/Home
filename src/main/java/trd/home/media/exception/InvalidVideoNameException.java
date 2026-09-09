package trd.home.media.exception;

import trd.home.common.exception.HomeException;

public class InvalidVideoNameException extends HomeException {
    public InvalidVideoNameException(String message) {
        super(message);
    }
}
