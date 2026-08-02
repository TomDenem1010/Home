package trd.home.tcg.exception;

import trd.home.common.exception.HomeException;

public class FailedToLaunchBrowser extends HomeException {

    public FailedToLaunchBrowser(String message, Throwable cause) {
        super(message, cause);
    }
}
