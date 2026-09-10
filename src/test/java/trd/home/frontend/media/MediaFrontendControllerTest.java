package trd.home.frontend.media;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import trd.home.media.constant.MediaStatus;
import trd.home.media.dto.ActorDto;
import trd.home.media.dto.FolderDto;
import trd.home.media.dto.VideoDto;
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
    void requestsMediaImportAndRedirectsImmediately() throws Exception {
        mvc.perform(post("/media/import").param("path", "C:\\Media"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/media/server-folder-path"));

        verify(service).importPath("C:\\Media");
    }

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
    void redirectsMediaRootToActorBrowser() throws Exception {
        mvc.perform(get("/media")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/media/by-actor"));
    }

    @Test
    void rendersServerFolderPathPage() throws Exception {
        mvc.perform(get("/media/server-folder-path"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("activePath", "/media/server-folder-path"))
                .andExpect(model().attribute("contentTemplate", "media/server-folder-path"));
    }

    @Test
    void rendersActorAndFolderBrowsersWithAndWithoutSelection() throws Exception {
        ActorDto actor = new ActorDto("actor-1", "Alice", MediaStatus.ACTIVE);
        FolderDto folder = new FolderDto("folder-1", "media", "Movies", MediaStatus.ACTIVE);
        VideoDto video = new VideoDto("video-1", "Film", folder, java.util.Set.of(actor), MediaStatus.ACTIVE);
        when(service.activeActors()).thenReturn(List.of(actor));
        when(service.activeFolders()).thenReturn(List.of(folder));
        when(service.videosByActor("actor-1")).thenReturn(List.of(video));
        when(service.videosByFolder("folder-1")).thenReturn(List.of(video));

        mvc.perform(get("/media/by-actor"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("mode", "by-actor"))
                .andExpect(model().attribute("actors", List.of(actor)))
                .andExpect(model().attribute("videos", List.of()));
        mvc.perform(get("/media/by-actor").param("actorId", "actor-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("selectedId", "actor-1"))
                .andExpect(model().attribute("videos", List.of(video)))
                .andExpect(model().attribute("pageContent", "Select an actor or folder, then choose a video to play."));
        mvc.perform(get("/media/by-folder").param("folderId", "folder-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("mode", "by-folder"))
                .andExpect(model().attribute("folders", List.of(folder)))
                .andExpect(model().attribute("selectedId", "folder-1"))
                .andExpect(model().attribute("videos", List.of(video)));
        verify(service).videosByActor("actor-1");
        verify(service).videosByFolder("folder-1");
    }

    @Test
    void usesVideoMediaTypeOnlyForVideoResources() throws Exception {
        Path mp4 = Files.write(directory.resolve("film.mp4"), new byte[] {1});
        Path text = Files.write(directory.resolve("film.txt"), new byte[] {1});
        when(service.videoResource("mp4")).thenReturn(new FileSystemResource(mp4));
        when(service.videoResource("text")).thenReturn(new FileSystemResource(text));

        mvc.perform(get("/media/videos/mp4/stream")).andExpect(content().contentType("video/mp4"));
        mvc.perform(get("/media/videos/text/stream")).andExpect(content().contentType("application/octet-stream"));
    }
}
