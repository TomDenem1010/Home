package trd.home.tcg.exception;

import trd.home.common.exception.HomeException;

public class ThrottlerException extends HomeException {

    public ThrottlerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ThrottlerException(String message) {
        super(message);
    }
}
