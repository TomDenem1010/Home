package trd.home.tcg.exception;

import trd.home.common.exception.HomeException;

public class WrongDeckNameException extends HomeException {

    public WrongDeckNameException(String message) {
        super(message);
    }
}
