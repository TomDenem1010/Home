package trd.home.tcg.scheduler;

import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.ApplicationEventQueue;
import trd.home.tcg.service.event.SaveDecksFromXlsxService;

class SaveDecksFromXlsxSchedulerTest {
    @Test
    void claimsOnlyXlsxEventsAndDelegatesProcessing() {
        var events = mock(ApplicationEventQueue.class);
        var service = mock(SaveDecksFromXlsxService.class);
        var scheduler = new SaveDecksFromXlsxScheduler(events, service);
        scheduler.processNextEvent();
        verifyNoInteractions(service);
        var event = new ApplicationEvent(EventType.SAVE_DECKS_FROM_XLSX, "id");
        when(events.claimNext(EventType.SAVE_DECKS_FROM_XLSX)).thenReturn(Optional.of(event));
        scheduler.processNextEvent();
        verify(service).process(event);
    }
}
