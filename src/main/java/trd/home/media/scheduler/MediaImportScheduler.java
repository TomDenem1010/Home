package trd.home.media.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.media.service.event.MediaImportEventProcessor;

@Component
@RequiredArgsConstructor
public class MediaImportScheduler {

    private final ApplicationEventRepository eventRepository;
    private final MediaImportEventProcessor eventProcessor;

    @Scheduled(fixedDelayString = "${media.scheduler.import.delay:5s}")
    public void processNextEvent() {
        eventRepository
                .findFirstByTypeAndStatusOrderByCreatedAtAsc(EventType.IMPORT_MEDIA, EventStatus.TO_DO)
                .ifPresent(eventProcessor::process);
    }
}
