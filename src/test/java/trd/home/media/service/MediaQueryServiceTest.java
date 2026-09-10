package trd.home.media.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import trd.home.media.constant.MediaStatus;
import trd.home.media.dao.*;
import trd.home.media.exception.MediaVideoNotFoundException;
import trd.home.media.repository.*;

class MediaQueryServiceTest {

    private final ActorRepository actors = mock(ActorRepository.class);
    private final FolderRepository folders = mock(FolderRepository.class);
    private final VideoRepository videos = mock(VideoRepository.class);
    private final MediaQueryService service = new MediaQueryService(actors, folders, videos);

    @Test
    void mapsAllActiveQueriesToDtos() {
        Actor actor = actor("actor-1", "Alice");
        Folder folder = folder("folder-1", "C:\\Media\\Movies", "Movies");
        Video video = video("video-1", "Film", folder, actor);
        when(actors.findByStatusOrderByName(MediaStatus.ACTIVE)).thenReturn(List.of(actor));
        when(folders.findByStatusOrderByPath(MediaStatus.ACTIVE)).thenReturn(List.of(folder));
        when(videos.findDistinctByStatusOrderByName(MediaStatus.ACTIVE)).thenReturn(List.of(video));
        when(videos.findDistinctByStatusAndActorsIdOrderByName(MediaStatus.ACTIVE, "actor-1"))
                .thenReturn(List.of(video));
        when(videos.findDistinctByStatusAndFolderIdOrderByName(MediaStatus.ACTIVE, "folder-1"))
                .thenReturn(List.of(video));

        assertEquals("Alice", service.activeActors().getFirst().name());
        assertEquals("Movies", service.activeFolders().getFirst().type());
        assertEquals("Film", service.activeVideos().getFirst().name());
        assertEquals("video-1", service.videosByActor("actor-1").getFirst().id());
        assertEquals("video-1", service.videosByFolder("folder-1").getFirst().id());
    }

    @Test
    void resolvesNormalizedVideoPath() {
        Folder folder = folder("folder-1", "media", "media");
        Video video = video("video-1", "Film", folder);
        video.setFileName("film.mp4");
        when(videos.findByIdAndStatus("video-1", MediaStatus.ACTIVE)).thenReturn(Optional.of(video));

        assertEquals(
                java.nio.file.Path.of("media").toAbsolutePath().normalize().resolve("film.mp4"),
                service.videoPath("video-1"));
    }

    @Test
    void rejectsMissingVideoAndPathTraversal() {
        assertThrows(MediaVideoNotFoundException.class, () -> service.videoPath("missing"));
        Folder folder = folder("folder-1", "media", "media");
        Video video = video("video-1", "Film", folder);
        video.setFileName("..\\outside.mp4");
        when(videos.findByIdAndStatus("video-1", MediaStatus.ACTIVE)).thenReturn(Optional.of(video));

        assertThrows(MediaVideoNotFoundException.class, () -> service.videoPath("video-1"));
    }

    private Actor actor(String id, String name) {
        Actor actor = new Actor();
        actor.setId(id);
        actor.setName(name);
        return actor;
    }

    private Folder folder(String id, String path, String type) {
        Folder folder = new Folder();
        folder.setId(id);
        folder.setPath(path);
        folder.setType(type);
        return folder;
    }

    private Video video(String id, String name, Folder folder, Actor... actors) {
        Video video = new Video();
        video.setId(id);
        video.setName(name);
        video.setFolder(folder);
        video.setActors(Set.of(actors));
        return video;
    }
}
