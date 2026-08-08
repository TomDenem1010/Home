package trd.home.tcg.service.playwright;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import trd.home.tcg.exception.ThrottlerException;

@Component
public class CardmarketRequestThrottler {

    private final long minimumDelayMillis;
    private final long maximumDelayMillis;

    public CardmarketRequestThrottler(
            @Value("${tcg.cardmarket.request-delay-min:5s}") Duration minimumDelay,
            @Value("${tcg.cardmarket.request-delay-max:10s}") Duration maximumDelay) {
        minimumDelayMillis = minimumDelay.toMillis();
        maximumDelayMillis = maximumDelay.toMillis();
        if (minimumDelayMillis < 0) {
            throw new ThrottlerException("Minimum Cardmarket request delay must not be negative");
        }
        if (maximumDelayMillis < minimumDelayMillis) {
            throw new ThrottlerException(
                    "Maximum Cardmarket request delay must not be less than the minimum delay");
        }
    }

    public void waitBeforeNextRequest() {
        try {
            Thread.sleep(nextDelayMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ThrottlerException("Interrupted while waiting before the next Cardmarket request", exception);
        }
    }

    long nextDelayMillis() {
        if (minimumDelayMillis == maximumDelayMillis) {
            return minimumDelayMillis;
        }
        return ThreadLocalRandom.current().nextLong(minimumDelayMillis, maximumDelayMillis + 1);
    }
}
