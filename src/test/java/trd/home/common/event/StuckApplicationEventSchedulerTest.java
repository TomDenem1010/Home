package trd.home.common.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

class StuckApplicationEventSchedulerTest {

    private final StuckApplicationEventService eventService = mock(StuckApplicationEventService.class);
    private final StuckApplicationEventScheduler scheduler = new StuckApplicationEventScheduler(eventService);

    @Test
    void delegatesStuckEventProcessingToService() {
        scheduler.failStuckEvents();

        verify(eventService).failStuckEvents();
    }
}
