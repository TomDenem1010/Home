package trd.home.common.exception;

import java.time.Duration;

public class EventProcessingTimeoutException extends HomeException {

    public EventProcessingTimeoutException(Duration timeout) {
        super("Event remained in PROCESSING state longer than " + timeout);
    }
}
