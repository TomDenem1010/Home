package trd.home.tcg.exception;

import trd.home.common.exception.HomeException;

public class CardmarketPriceNotFoundException extends HomeException {

    public CardmarketPriceNotFoundException(String message) {
        super(message);
    }

    public CardmarketPriceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
