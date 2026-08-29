package trd.home.tcg.exception;

import trd.home.common.exception.HomeException;

public class WrongCardQuantityException extends HomeException {

    public WrongCardQuantityException(String message) {
        super(message);
    }
}
