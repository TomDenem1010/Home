package trd.home.media.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import trd.home.media.service.application.MediaCommandService;
import trd.home.media.service.query.MediaQueryService;

class MediaServiceTest {
    private final MediaQueryService query = mock(MediaQueryService.class);
    private final MediaCommandService commands = mock(MediaCommandService.class);
    private final MediaService service = new MediaService(query, commands);

    @Test
    void createsMediaImportEvent() {
        service.importPath("C:\\Media");

        verify(commands).importPath("C:\\Media");
    }

    @Test
    void delegatesVideoResourceWithoutLoggingLayerLogic() {
        Resource resource = mock(Resource.class);
        when(commands.videoResource("video")).thenReturn(resource);

        assertSame(resource, service.videoResource("video"));
    }

    @Test
    void delegatesMediaQueries() {
        when(query.activeActors()).thenReturn(List.of());
        when(query.activeFolders()).thenReturn(List.of());
        when(query.videosByActor("actor")).thenReturn(List.of());
        when(query.videosByFolder("folder")).thenReturn(List.of());

        assertSame(query.activeActors(), service.activeActors());
        assertSame(query.activeFolders(), service.activeFolders());
        assertSame(query.videosByActor("actor"), service.videosByActor("actor"));
        assertSame(query.videosByFolder("folder"), service.videosByFolder("folder"));
    }
}
