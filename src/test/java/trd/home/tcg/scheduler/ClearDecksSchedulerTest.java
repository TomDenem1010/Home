package trd.home.tcg.scheduler;

import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.ApplicationEventQueue;
import trd.home.tcg.service.event.ClearDecksService;

class ClearDecksSchedulerTest {
    private final ApplicationEventQueue queue = mock(ApplicationEventQueue.class);
    private final ClearDecksService service = mock(ClearDecksService.class);
    private final ClearDecksScheduler scheduler = new ClearDecksScheduler(queue, service);

    @Test
    void claimsAndProcessesClearEvent() {
        var event = new ApplicationEvent(EventType.CLEAR_DECKS);
        when(queue.claimNext(EventType.CLEAR_DECKS)).thenReturn(Optional.of(event));
        scheduler.processNextEvent();
        verify(service).process(event);
    }

    @Test
    void doesNothingWithoutPendingEvent() {
        scheduler.processNextEvent();
        verifyNoInteractions(service);
    }
}
