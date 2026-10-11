package trd.home.media.service.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import trd.home.common.constant.EventType;
import trd.home.common.event.ApplicationEventQueue;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.media.exception.MediaVideoNotFoundException;
import trd.home.media.service.query.MediaQueryService;

class MediaCommandServiceTest {
    private final MediaQueryService query = mock(MediaQueryService.class);
    private final ApplicationEventQueue events = mock(ApplicationEventQueue.class);
    private final FrontendNotificationPublisher notifications = mock(FrontendNotificationPublisher.class);
    private final MediaCommandService service = new MediaCommandService(query, events, notifications);

    @Test
    void createsMediaImportEvent() {
        service.importPath("C:\\Media");
        var order = inOrder(events, notifications);
        order.verify(events).enqueue(EventType.IMPORT_MEDIA, "C:\\Media");
        order.verify(notifications).publish(FrontendNotificationType.WARNING, "Media import has started.");
    }

    @Test
    void resolvesExistingVideoResource() throws Exception {
        var video = Path.of("media-tests", "video.mp4").toAbsolutePath().normalize();
        when(query.videoPath("video")).thenReturn(video);
        try (var files = mockStatic(Files.class)) {
            files.when(() -> Files.isRegularFile(video, LinkOption.NOFOLLOW_LINKS))
                    .thenReturn(true);
            files.when(() -> Files.isReadable(video)).thenReturn(true);
            assertEquals(video, service.videoResource("video").getFile().toPath());
        }
    }

    @Test
    void rejectsUnavailableVideoResource() {
        var video = Path.of("media-tests", "missing.mp4");
        when(query.videoPath("missing")).thenReturn(video);
        try (var files = mockStatic(Files.class)) {
            files.when(() -> Files.isRegularFile(video, LinkOption.NOFOLLOW_LINKS))
                    .thenReturn(false);
            assertThrows(MediaVideoNotFoundException.class, () -> service.videoResource("missing"));
        }
    }

    @Test
    void rejectsUnreadableVideoResource() {
        var video = Path.of("media-tests", "video.mp4");
        when(query.videoPath("video")).thenReturn(video);
        try (var files = mockStatic(Files.class)) {
            files.when(() -> Files.isRegularFile(video, LinkOption.NOFOLLOW_LINKS))
                    .thenReturn(true);
            files.when(() -> Files.isReadable(video)).thenReturn(false);
            assertThrows(MediaVideoNotFoundException.class, () -> service.videoResource("video"));
        }
    }
}
