package trd.home.media.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import trd.home.media.exception.MediaVideoNotFoundException;

class MediaServiceTest {
    private final MediaQueryService query = mock(MediaQueryService.class);
    private final MediaService service = new MediaService(query, mock(MediaImportService.class));

    @TempDir
    Path directory;

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
}
