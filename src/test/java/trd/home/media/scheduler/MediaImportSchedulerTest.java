package trd.home.media.scheduler;

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
import trd.home.media.service.event.MediaImportEventProcessor;

class MediaImportSchedulerTest {

    private final ApplicationEventRepository eventRepository = mock(ApplicationEventRepository.class);
    private final MediaImportEventProcessor eventProcessor = mock(MediaImportEventProcessor.class);
    private final MediaImportScheduler scheduler = new MediaImportScheduler(eventRepository, eventProcessor);

    @Test
    void delegatesOldestPendingEvent() {
        ApplicationEvent event = new ApplicationEvent(EventType.IMPORT_MEDIA, "C:\\Media");
        when(eventRepository.findFirstByTypeAndStatusOrderByCreatedAtAsc(EventType.IMPORT_MEDIA, EventStatus.TO_DO))
                .thenReturn(Optional.of(event));

        scheduler.processNextEvent();

        verify(eventProcessor).process(event);
    }

    @Test
    void doesNotCallProcessorWithoutPendingEvent() {
        scheduler.processNextEvent();

        verifyNoInteractions(eventProcessor);
    }
}
