package trd.home.tcg.exception;

import trd.home.common.exception.HomeException;

public class WrongCardLineException extends HomeException {

    public WrongCardLineException(String message) {
        super(message);
    }
}
