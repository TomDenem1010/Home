package trd.home.media.scheduler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.ApplicationEventQueue;
import trd.home.media.service.event.MediaImportEventProcessor;

class MediaImportSchedulerTest {

    private final ApplicationEventQueue eventQueue = mock(ApplicationEventQueue.class);
    private final MediaImportEventProcessor eventProcessor = mock(MediaImportEventProcessor.class);
    private final MediaImportScheduler scheduler = new MediaImportScheduler(eventQueue, eventProcessor);

    @Test
    void delegatesOldestPendingEvent() {
        ApplicationEvent event = new ApplicationEvent(EventType.IMPORT_MEDIA, "C:\\Media");
        when(eventQueue.claimNext(EventType.IMPORT_MEDIA)).thenReturn(Optional.of(event));

        scheduler.processNextEvent();

        verify(eventProcessor).process(event);
    }

    @Test
    void doesNotCallProcessorWithoutPendingEvent() {
        scheduler.processNextEvent();

        verifyNoInteractions(eventProcessor);
    }
}
