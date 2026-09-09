package trd.home.media.exception;

import trd.home.common.exception.HomeException;

public class UnableToGenerateActorKeyException extends HomeException {
    public UnableToGenerateActorKeyException(String message) {
        super(message);
    }

    public UnableToGenerateActorKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
