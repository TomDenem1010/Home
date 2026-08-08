package trd.home.tcg.service.playwright;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import trd.home.tcg.exception.ThrottlerException;

class CardmarketRequestThrottlerTest {

    @Test
    void generatesDelayBetweenConfiguredThresholds() {
        CardmarketRequestThrottler throttler =
                new CardmarketRequestThrottler(Duration.ofSeconds(5), Duration.ofSeconds(10));

        IntStream.range(0, 100).forEach(ignored -> {
            long delay = throttler.nextDelayMillis();
            assertTrue(delay >= 5_000);
            assertTrue(delay <= 10_000);
        });
    }

    @Test
    void usesExactDelayWhenThresholdsAreEqual() {
        CardmarketRequestThrottler throttler =
                new CardmarketRequestThrottler(Duration.ofSeconds(5), Duration.ofSeconds(5));

        assertEquals(5_000, throttler.nextDelayMillis());
    }

    @Test
    void rejectsInvalidThresholds() {
        assertThrows(
                ThrottlerException.class,
                () -> new CardmarketRequestThrottler(Duration.ofSeconds(-1), Duration.ofSeconds(5)));
        assertThrows(
                ThrottlerException.class,
                () -> new CardmarketRequestThrottler(Duration.ofSeconds(10), Duration.ofSeconds(5)));
    }
}
