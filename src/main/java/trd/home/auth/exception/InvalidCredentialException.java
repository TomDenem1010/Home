package trd.home.auth.exception;

import trd.home.common.exception.HomeException;

public class InvalidCredentialException extends HomeException {

    public InvalidCredentialException(String message) {
        super(message);
    }
}
