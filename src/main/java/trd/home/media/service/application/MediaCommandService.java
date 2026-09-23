package trd.home.media.service.application;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.media.exception.MediaVideoNotFoundException;
import trd.home.media.service.query.MediaQueryService;

@Service
@RequiredArgsConstructor
public class MediaCommandService {

    private final MediaQueryService query;
    private final ApplicationEventRepository eventRepository;
    private final FrontendNotificationPublisher notificationPublisher;

    public void importPath(String path) {
        eventRepository.save(new ApplicationEvent(EventType.IMPORT_MEDIA, path));
        notificationPublisher.publish(FrontendNotificationType.WARNING, "Media import has started.");
    }

    public Resource videoResource(String id) {
        var path = query.videoPath(id);
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) || !Files.isReadable(path)) {
            throw new MediaVideoNotFoundException("Video is unavailable: " + id);
        }
        return new FileSystemResource(path);
    }
}
