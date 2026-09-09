package trd.home.frontend.media;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import trd.home.media.exception.MediaVideoNotFoundException;
import trd.home.media.service.MediaService;

class MediaFrontendControllerTest {
    private final MediaService service = mock(MediaService.class);
    private final org.springframework.test.web.servlet.MockMvc mvc = MockMvcBuilders.standaloneSetup(
                    new MediaFrontendController(service))
            .setControllerAdvice(new trd.home.common.exception.HomeExceptionHandler())
            .build();

    @TempDir
    Path directory;

    @Test
    void streamsRequestedByteRange() throws Exception {
        Path video = Files.write(directory.resolve("video.mp4"), new byte[] {0, 1, 2, 3, 4});
        when(service.videoResource("video")).thenReturn(new FileSystemResource(video));

        mvc.perform(get("/media/videos/video/stream").header("Range", "bytes=1-3"))
                .andExpect(status().isPartialContent())
                .andExpect(header().string("Content-Range", "bytes 1-3/5"))
                .andExpect(content().bytes(new byte[] {1, 2, 3}));
    }

    @Test
    void delegatesUnavailableVideoToHomeExceptionHandler() throws Exception {
        when(service.videoResource("missing")).thenThrow(new MediaVideoNotFoundException("Video is unavailable."));

        mvc.perform(get("/media/videos/missing/stream"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Video is unavailable."));
    }

    @Test
    void actorApiStillReturnsJson() throws Exception {
        when(service.activeActors()).thenReturn(List.of());

        mvc.perform(get("/media/api/actors"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        verify(service).activeActors();
    }
}
