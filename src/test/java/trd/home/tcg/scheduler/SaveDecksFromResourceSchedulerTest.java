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
import trd.home.tcg.service.event.SaveDecksFromResourceService;

class SaveDecksFromResourceSchedulerTest {

    private final ApplicationEventQueue eventQueue = mock(ApplicationEventQueue.class);
    private final SaveDecksFromResourceService service = mock(SaveDecksFromResourceService.class);
    private final SaveDecksFromResourceScheduler scheduler = new SaveDecksFromResourceScheduler(eventQueue, service);

    @Test
    void delegatesOldestPendingEventToService() {
        ApplicationEvent event = new ApplicationEvent(EventType.SAVE_DECKS_FROM_RESOURCE);
        when(eventQueue.claimNext(EventType.SAVE_DECKS_FROM_RESOURCE)).thenReturn(Optional.of(event));

        scheduler.processNextEvent();

        verify(service).process(event);
    }

    @Test
    void doesNotCallServiceWithoutPendingEvent() {
        scheduler.processNextEvent();

        verifyNoInteractions(service);
    }
}
