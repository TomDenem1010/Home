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
import trd.home.tcg.service.event.ImportDecksFromCsvService;

class ImportDecksFromCsvSchedulerTest {

    private final ApplicationEventQueue eventQueue = mock(ApplicationEventQueue.class);
    private final ImportDecksFromCsvService service = mock(ImportDecksFromCsvService.class);
    private final ImportDecksFromCsvScheduler scheduler = new ImportDecksFromCsvScheduler(eventQueue, service);

    @Test
    void delegatesOldestPendingEventToService() {
        ApplicationEvent event = new ApplicationEvent(EventType.IMPORT_DECKS_FROM_CSV);
        when(eventQueue.claimNext(EventType.IMPORT_DECKS_FROM_CSV)).thenReturn(Optional.of(event));

        scheduler.processNextEvent();

        verify(service).process(event);
    }

    @Test
    void doesNotCallServiceWithoutPendingEvent() {
        scheduler.processNextEvent();

        verifyNoInteractions(service);
    }
}
