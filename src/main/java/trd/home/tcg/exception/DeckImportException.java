package trd.home.tcg.exception;

import trd.home.common.exception.HomeException;

public class DeckImportException extends HomeException {

    public DeckImportException(String message) {
        super(message);
    }

    public DeckImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
