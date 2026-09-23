package trd.home.media.service.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;
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

    @TempDir
    Path directory;

    @Test
    void createsMediaImportEvent() {
        service.importPath("C:\\Media");

        InOrder order = inOrder(events, notifications);
        order.verify(events).enqueue(EventType.IMPORT_MEDIA, "C:\\Media");
        order.verify(notifications).publish(FrontendNotificationType.WARNING, "Media import has started.");
    }

    @Test
    void resolvesExistingVideoResource() throws Exception {
        Path video = Files.write(directory.resolve("video.mp4"), new byte[] {1});
        when(query.videoPath("video")).thenReturn(video);

        assertEquals(video, service.videoResource("video").getFile().toPath());
    }

    @Test
    void rejectsUnavailableVideoResource() {
        when(query.videoPath("missing")).thenReturn(directory.resolve("missing.mp4"));

        assertThrows(MediaVideoNotFoundException.class, () -> service.videoResource("missing"));
    }
}
