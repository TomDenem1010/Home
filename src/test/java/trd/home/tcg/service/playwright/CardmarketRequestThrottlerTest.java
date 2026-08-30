package trd.home.tcg.service.playwright;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
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
        assertTrue(IntStream.range(0, 100)
                .mapToLong(ignored -> throttler.nextDelayMillis())
                .anyMatch(delay -> delay > 5_000));
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

    @Test
    void acceptsZeroMinimumDelay() {
        CardmarketRequestThrottler throttler = new CardmarketRequestThrottler(Duration.ZERO, Duration.ofMillis(1));

        assertTrue(IntStream.range(0, 100)
                .mapToLong(ignored -> throttler.nextDelayMillis())
                .allMatch(delay -> delay == 0 || delay == 1));
    }

    @Test
    void restoresInterruptStatusAndLogsInterruptedWait() {
        Logger logger = (Logger) LoggerFactory.getLogger(CardmarketRequestThrottler.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        Thread.currentThread().interrupt();

        try {
            CardmarketRequestThrottler throttler =
                    new CardmarketRequestThrottler(Duration.ofSeconds(1), Duration.ofSeconds(1));

            ThrottlerException exception = assertThrows(ThrottlerException.class, throttler::waitBeforeNextRequest);

            assertTrue(Thread.currentThread().isInterrupted());
            assertTrue(exception.getCause() instanceof InterruptedException);
            List<ILoggingEvent> errorEvents = appender.list.stream()
                    .filter(event -> event.getLevel() == Level.ERROR)
                    .toList();
            assertEquals(1, errorEvents.size());
            assertEquals(
                    "Interrupted while waiting before the next Cardmarket request",
                    errorEvents.getFirst().getFormattedMessage());
            assertEquals(
                    InterruptedException.class.getName(),
                    errorEvents.getFirst().getThrowableProxy().getClassName());
        } finally {
            Thread.interrupted();
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}
