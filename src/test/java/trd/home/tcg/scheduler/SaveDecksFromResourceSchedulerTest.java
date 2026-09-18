package trd.home.tcg.scheduler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.tcg.service.SaveDecksFromResourceService;

class SaveDecksFromResourceSchedulerTest {

    private final ApplicationEventRepository eventRepository = mock(ApplicationEventRepository.class);
    private final SaveDecksFromResourceService service = mock(SaveDecksFromResourceService.class);
    private final SaveDecksFromResourceScheduler scheduler =
            new SaveDecksFromResourceScheduler(eventRepository, service);

    @Test
    void delegatesOldestPendingEventToService() {
        ApplicationEvent event = new ApplicationEvent(EventType.SAVE_DECKS_FROM_RESOURCE);
        when(eventRepository.findFirstByTypeAndStatusOrderByCreatedAtAsc(
                        EventType.SAVE_DECKS_FROM_RESOURCE, EventStatus.TO_DO))
                .thenReturn(Optional.of(event));

        scheduler.processNextEvent();

        verify(service).process(event);
    }

    @Test
    void doesNotCallServiceWithoutPendingEvent() {
        scheduler.processNextEvent();

        verifyNoInteractions(service);
    }
}
