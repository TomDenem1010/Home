package trd.home.frontend.media;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.ui.ConcurrentModel;
import trd.home.frontend.FrontendPageRenderer;
import trd.home.media.constant.MediaStatus;
import trd.home.media.dto.ActorDto;
import trd.home.media.dto.FolderDto;
import trd.home.media.dto.VideoDto;
import trd.home.media.exception.MediaVideoNotFoundException;
import trd.home.media.service.MediaService;

class MediaFrontendControllerTest {
    private final MediaService service = mock(MediaService.class);
    private final FrontendPageRenderer pages = mock(FrontendPageRenderer.class);
    private final MediaFrontendController controller = new MediaFrontendController(service, pages);

    @Test
    void createsVideoTagAndRedirectsToOperations() {
        assertEquals("redirect:/media/video-operations", controller.createVideoTag("Travel"));
        verify(service).createVideoTag("Travel");
    }

    @Test
    void propagatesVideoTagCreationFailure() {
        var failure = new trd.home.media.exception.InvalidVideoTagException("Invalid name");
        doThrow(failure).when(service).createVideoTag(" ");
        assertSame(
                failure,
                assertThrows(
                        trd.home.media.exception.InvalidVideoTagException.class, () -> controller.createVideoTag(" ")));
    }

    @Test
    void requestsMediaImportAndRedirectsImmediately() {
        assertEquals("redirect:/media/video-operations", controller.importPath("C:\\Media"));
        verify(service).importPath("C:\\Media");
    }

    @Test
    void returnsVideoResourceWithStreamingHeadersWithoutReadingContent() throws Exception {
        var resource = mock(Resource.class);
        when(resource.getFilename()).thenReturn("video.mp4");
        when(resource.contentLength()).thenReturn(5L);
        when(service.videoResource("video")).thenReturn(resource);
        var response = controller.stream("video");
        assertEquals(200, response.getStatusCode().value());
        assertSame(resource, response.getBody());
        assertEquals(MediaType.valueOf("video/mp4"), response.getHeaders().getContentType());
        assertEquals(5, response.getHeaders().getContentLength());
        assertEquals("bytes", response.getHeaders().getFirst("Accept-Ranges"));
        assertEquals("no-store", response.getHeaders().getCacheControl());
        verify(resource, never()).getInputStream();
    }

    @Test
    void propagatesUnavailableVideo() {
        var failure = new MediaVideoNotFoundException("Video is unavailable.");
        when(service.videoResource("missing")).thenThrow(failure);
        assertSame(failure, assertThrows(MediaVideoNotFoundException.class, () -> controller.stream("missing")));
    }

    @Test
    void propagatesResourceMetadataFailure() throws Exception {
        var resource = mock(Resource.class);
        when(resource.getFilename()).thenReturn("film.mp4");
        var failure = new IOException("unavailable");
        when(resource.contentLength()).thenThrow(failure);
        when(service.videoResource("video")).thenReturn(resource);
        assertSame(failure, assertThrows(IOException.class, () -> controller.stream("video")));
    }

    @Test
    void redirectsMediaRootToActorBrowser() {
        assertEquals("redirect:/media/by-actor", controller.index());
    }

    @Test
    void rendersVideoOperationsPage() {
        var model = new ConcurrentModel();
        when(pages.render(
                        model,
                        "/media/video-operations",
                        "Video operations",
                        "Manage videos.",
                        "media/video-operations"))
                .thenReturn("index");
        assertEquals("index", controller.videoOperations(model));
        verify(pages)
                .render(
                        model,
                        "/media/video-operations",
                        "Video operations",
                        "Manage videos.",
                        "media/video-operations");
    }

    @Test
    void assemblesActorAndFolderBrowsersWithAndWithoutSelection() {
        var actor = new ActorDto("actor-1", "Alice", MediaStatus.ACTIVE);
        var folder = new FolderDto("folder-1", "media", "Movies", MediaStatus.ACTIVE);
        var video = new VideoDto("video-1", "Film", folder, Set.of(actor), MediaStatus.ACTIVE);
        when(service.activeActors()).thenReturn(List.of(actor));
        when(service.activeFolders()).thenReturn(List.of(folder));
        when(service.videosByActor("actor-1")).thenReturn(List.of(video));
        when(service.videosByFolder("folder-1")).thenReturn(List.of(video));
        when(pages.render(any(), eq("/media/by-actor"), eq("ByActor"), anyString(), eq("media/library")))
                .thenReturn("actor-view");
        when(pages.render(any(), eq("/media/by-folder"), eq("ByFolder"), anyString(), eq("media/library")))
                .thenReturn("folder-view");
        var model = new ConcurrentModel();
        assertEquals("actor-view", controller.byActor(null, model));
        assertEquals("by-actor", model.getAttribute("mode"));
        assertEquals(List.of(actor), model.getAttribute("actors"));
        assertEquals(List.of(), model.getAttribute("videos"));
        verify(service, never()).videosByActor(any());
        assertEquals("actor-view", controller.byActor("actor-1", model));
        assertEquals("actor-1", model.getAttribute("selectedId"));
        assertEquals(List.of(video), model.getAttribute("videos"));
        assertEquals("folder-view", controller.byFolder(null, model));
        assertEquals("by-folder", model.getAttribute("mode"));
        assertEquals(List.of(folder), model.getAttribute("folders"));
        assertEquals(List.of(), model.getAttribute("videos"));
        verify(service, never()).videosByFolder(any());
        assertEquals("folder-view", controller.byFolder("folder-1", model));
        assertEquals("folder-1", model.getAttribute("selectedId"));
        assertEquals(List.of(video), model.getAttribute("videos"));
        verify(pages, times(2))
                .render(
                        model,
                        "/media/by-actor",
                        "ByActor",
                        "Select an actor or folder, then choose a video to play.",
                        "media/library");
        verify(pages, times(2))
                .render(
                        model,
                        "/media/by-folder",
                        "ByFolder",
                        "Select an actor or folder, then choose a video to play.",
                        "media/library");
    }

    @Test
    void usesBinaryContentTypeForNonVideoResource() throws Exception {
        var resource = mock(Resource.class);
        when(resource.getFilename()).thenReturn("film.txt");
        when(service.videoResource("text")).thenReturn(resource);
        assertEquals(
                MediaType.APPLICATION_OCTET_STREAM,
                controller.stream("text").getHeaders().getContentType());
    }
}
