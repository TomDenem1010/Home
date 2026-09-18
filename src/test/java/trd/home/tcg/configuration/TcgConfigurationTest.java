package trd.home.tcg.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class TcgConfigurationTest {

    private final TcgConfiguration configuration = new TcgConfiguration();

    @Test
    void createsTcgBeans() {
        var encodingValidator = configuration.resourceDeckEncodingValidator();
        var nameValidator = configuration.resourceDeckNameValidator();
        var cardValidator = configuration.resourceDeckCardValidator();

        assertNotNull(encodingValidator);
        assertNotNull(nameValidator);
        assertNotNull(cardValidator);
        assertNotNull(configuration.deckFileReader(encodingValidator, nameValidator, cardValidator));
    }

    @Test
    void deckPriceUpdatesUseOneWorkerThread() throws Exception {
        var executor = configuration.deckPriceUpdateExecutor();
        var runningTasks = new AtomicInteger();
        var maximumConcurrentTasks = new AtomicInteger();
        var firstTaskStarted = new CountDownLatch(1);
        var releaseFirstTask = new CountDownLatch(1);
        try {
            var first = executor.submit(() -> {
                int running = runningTasks.incrementAndGet();
                maximumConcurrentTasks.accumulateAndGet(running, Math::max);
                firstTaskStarted.countDown();
                try {
                    releaseFirstTask.await();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(exception);
                } finally {
                    runningTasks.decrementAndGet();
                }
            });
            assertEquals(true, firstTaskStarted.await(1, TimeUnit.SECONDS));

            var second = executor.submit(() -> {
                int running = runningTasks.incrementAndGet();
                maximumConcurrentTasks.accumulateAndGet(running, Math::max);
                runningTasks.decrementAndGet();
            });

            assertNotEquals(true, second.isDone());
            releaseFirstTask.countDown();
            first.get(1, TimeUnit.SECONDS);
            second.get(1, TimeUnit.SECONDS);
            assertEquals(1, maximumConcurrentTasks.get());
        } finally {
            releaseFirstTask.countDown();
            executor.shutdownNow();
        }
    }
}
