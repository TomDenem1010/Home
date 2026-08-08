package trd.home.tcg.exception;

import trd.home.common.exception.HomeException;

public class CardmarketRateLimitException extends HomeException {

    public CardmarketRateLimitException(String message) {
        super(message);
    }
}
