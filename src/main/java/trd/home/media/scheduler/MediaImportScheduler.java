package trd.home.media.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.media.service.MediaImportService;

@Slf4j
@Component
public class MediaImportScheduler {
    private final ApplicationEventRepository eventRepository;
    private final MediaImportService mediaImportService;
    private final FrontendNotificationPublisher notificationPublisher;

    public MediaImportScheduler(
            ApplicationEventRepository eventRepository,
            MediaImportService mediaImportService,
            FrontendNotificationPublisher notificationPublisher) {
        this.eventRepository = eventRepository;
        this.mediaImportService = mediaImportService;
        this.notificationPublisher = notificationPublisher;
    }

    @Scheduled(fixedDelayString = "${media.scheduler.import.delay:5s}")
    public void processNextEvent() {
        eventRepository
                .findFirstByTypeAndStatusOrderByCreatedAtAsc(EventType.IMPORT_MEDIA, EventStatus.TO_DO)
                .ifPresent(event -> {
                    event.markProcessing();
                    eventRepository.save(event);
                    FrontendNotificationType notificationType;
                    String notificationMessage;
                    try {
                        int importedVideos = mediaImportService.importPath(event.getMessage());
                        event.markDone();
                        notificationType = FrontendNotificationType.SUCCESS;
                        notificationMessage = "Imported videos: " + importedVideos;
                    } catch (RuntimeException exception) {
                        log.error("Failed to import media from path '{}'", event.getMessage(), exception);
                        event.markFailed(exception);
                        notificationType = FrontendNotificationType.ERROR;
                        notificationMessage = "Failed to import media: " + exception.getMessage();
                    }
                    eventRepository.save(event);
                    notificationPublisher.publish(event.getCreatedBy(), notificationType, notificationMessage);
                });
        log.info("Finished processing media import event");
    }
}
