package trd.home.tcg.exception;

import trd.home.common.exception.HomeException;

public class InvalidCardSearchFilterException extends HomeException {
    public InvalidCardSearchFilterException(String message) {
        super(message);
    }
}
