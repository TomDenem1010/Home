package trd.home.media.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.common.repository.ApplicationEventRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaImportEventProcessor {

    private final ApplicationEventRepository eventRepository;
    private final MediaImportService importService;
    private final FrontendNotificationPublisher notificationPublisher;

    public void process(ApplicationEvent event) {
        event.markProcessing();
        eventRepository.save(event);

        FrontendNotificationType notificationType;
        String notificationMessage;
        try {
            int importedVideos = importService.importPath(event.getMessage());
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
    }
}
