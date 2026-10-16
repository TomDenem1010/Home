package trd.home.tcg.scheduler;

import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.ApplicationEventQueue;
import trd.home.tcg.service.event.ExportDecksToCsvService;

class ExportDecksToCsvSchedulerTest {
    @Test
    void claimsOnlyCsvExportEventsAndDelegatesProcessing() {
        var events = mock(ApplicationEventQueue.class);
        var service = mock(ExportDecksToCsvService.class);
        var scheduler = new ExportDecksToCsvScheduler(events, service);
        scheduler.processNextEvent();
        verifyNoInteractions(service);
        var event = new ApplicationEvent(EventType.EXPORT_DECKS_TO_CSV, "id");
        when(events.claimNext(EventType.EXPORT_DECKS_TO_CSV)).thenReturn(Optional.of(event));
        scheduler.processNextEvent();
        verify(service).process(event);
    }
}
