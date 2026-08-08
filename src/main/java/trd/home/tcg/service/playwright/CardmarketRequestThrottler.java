package trd.home.tcg.service.playwright;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CardmarketRequestThrottler {

    private final Duration requestDelay;

    public CardmarketRequestThrottler(@Value("${tcg.cardmarket.request-delay:10s}") Duration requestDelay) {
        this.requestDelay = requestDelay;
    }

    public void waitBeforeNextRequest() {
        try {
            Thread.sleep(requestDelay.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting before the next Cardmarket request", exception);
        }
    }
}
