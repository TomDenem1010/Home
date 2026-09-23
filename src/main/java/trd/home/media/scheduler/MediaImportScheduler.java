package trd.home.media.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import trd.home.common.constant.EventType;
import trd.home.common.event.ApplicationEventQueue;
import trd.home.media.service.event.MediaImportEventProcessor;

@Component
@RequiredArgsConstructor
public class MediaImportScheduler {

    private final ApplicationEventQueue eventQueue;
    private final MediaImportEventProcessor eventProcessor;

    @Scheduled(fixedDelayString = "${media.scheduler.import.delay:5s}")
    public void processNextEvent() {
        eventQueue.claimNext(EventType.IMPORT_MEDIA).ifPresent(eventProcessor::process);
    }
}
