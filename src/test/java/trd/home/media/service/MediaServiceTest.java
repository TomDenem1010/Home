package trd.home.media.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;
import trd.home.common.constant.EventType;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.media.exception.MediaVideoNotFoundException;

class MediaServiceTest {
    private final MediaQueryService query = mock(MediaQueryService.class);
    private final ApplicationEventRepository eventRepository = mock(ApplicationEventRepository.class);
    private final FrontendNotificationPublisher notificationPublisher = mock(FrontendNotificationPublisher.class);
    private final MediaService service = new MediaService(query, eventRepository, notificationPublisher);

    @TempDir
    Path directory;

    @Test
    void createsMediaImportEvent() {
        service.importPath("C:\\Media");

        InOrder order = inOrder(eventRepository, notificationPublisher);
        order.verify(eventRepository)
                .save(argThat(
                        event -> event.getType() == EventType.IMPORT_MEDIA && "C:\\Media".equals(event.getMessage())));
        order.verify(notificationPublisher).publish(FrontendNotificationType.WARNING, "Media import has started.");
    }

    @Test
    void resolvesExistingVideoResource() throws Exception {
        Path video = Files.write(directory.resolve("video.mp4"), new byte[] {1, 2, 3});
        when(query.videoPath("video")).thenReturn(video);

        assertEquals(video, service.videoResource("video").getFile().toPath());
    }

    @Test
    void rejectsMissingFilesAndDirectories() {
        when(query.videoPath("missing")).thenReturn(directory.resolve("missing.mp4"));
        when(query.videoPath("directory")).thenReturn(directory);

        for (String id : new String[] {"missing", "directory"}) {
            assertThrows(MediaVideoNotFoundException.class, () -> service.videoResource(id));
        }
    }

    @Test
    void delegatesMediaQueries() {
        when(query.activeActors()).thenReturn(List.of());
        when(query.activeFolders()).thenReturn(List.of());
        when(query.activeVideos()).thenReturn(List.of());
        when(query.videosByActor("actor")).thenReturn(List.of());
        when(query.videosByFolder("folder")).thenReturn(List.of());

        assertSame(query.activeActors(), service.activeActors());
        assertSame(query.activeFolders(), service.activeFolders());
        assertSame(query.activeVideos(), service.activeVideos());
        assertSame(query.videosByActor("actor"), service.videosByActor("actor"));
        assertSame(query.videosByFolder("folder"), service.videosByFolder("folder"));
    }
}
