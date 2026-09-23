package trd.home.tcg.scheduler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.ApplicationEventQueue;
import trd.home.tcg.service.event.RefreshDeckPricesService;

class RefreshDeckPricesSchedulerTest {

    private final ApplicationEventQueue eventQueue = mock(ApplicationEventQueue.class);
    private final RefreshDeckPricesService service = mock(RefreshDeckPricesService.class);
    private final RefreshDeckPricesScheduler scheduler = new RefreshDeckPricesScheduler(eventQueue, service);

    @Test
    void delegatesOldestPendingEventToService() {
        ApplicationEvent event = new ApplicationEvent(EventType.REFRESH_DECK_PRICES);
        when(eventQueue.claimNext(EventType.REFRESH_DECK_PRICES)).thenReturn(Optional.of(event));

        scheduler.processNextEvent();

        verify(service).process(event);
    }

    @Test
    void doesNotCallServiceWithoutPendingEvent() {
        scheduler.processNextEvent();

        verifyNoInteractions(service);
    }
}
