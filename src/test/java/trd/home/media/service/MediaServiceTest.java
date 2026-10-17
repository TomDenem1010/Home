package trd.home.media.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import trd.home.media.dto.VideoTagDto;
import trd.home.media.service.application.MediaCommandService;
import trd.home.media.service.query.MediaQueryService;
import trd.home.media.service.tag.VideoTagAssignmentService;
import trd.home.media.service.tag.VideoTagService;

class MediaServiceTest {
    private final MediaQueryService query = mock(MediaQueryService.class);
    private final MediaCommandService commands = mock(MediaCommandService.class);
    private final VideoTagService videoTags = mock(VideoTagService.class);
    private final VideoTagAssignmentService assignments = mock(VideoTagAssignmentService.class);
    private final MediaService service = new MediaService(query, commands, videoTags, assignments);

    @Test
    void delegatesTagAssignmentsAndQueries() {
        var tags = List.of(new VideoTagDto("tag", "TRAVEL", true));
        when(assignments.tagsForVideo("video")).thenReturn(tags);
        assertSame(tags, service.videoTags("video"));
        service.setVideoTagAssigned("video", "tag", true);
        verify(assignments).setAssigned("video", "tag", true);
    }

    @Test
    void delegatesVideoTagCreation() {
        service.createVideoTag("Travel");
        verify(videoTags).create("Travel");
    }

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
