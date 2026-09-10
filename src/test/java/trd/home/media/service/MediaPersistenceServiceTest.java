package trd.home.media.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import trd.home.media.constant.MediaStatus;
import trd.home.media.dao.*;
import trd.home.media.dto.*;
import trd.home.media.exception.DuplicateVideoException;
import trd.home.media.repository.*;

class MediaPersistenceServiceTest {

    private final ActorRepository actors = mock(ActorRepository.class);
    private final FolderRepository folders = mock(FolderRepository.class);
    private final VideoRepository videos = mock(VideoRepository.class);
    private final MediaPersistenceService service = new MediaPersistenceService(actors, folders, videos);

    @Test
    void deactivatesOldDataAndCreatesImportedGraph() {
        Actor oldActor = new Actor();
        Folder oldFolder = new Folder();
        Video oldVideo = new Video();
        when(actors.findAll()).thenReturn(List.of(oldActor));
        when(folders.findAll()).thenReturn(List.of(oldFolder));
        when(videos.findAll()).thenReturn(List.of(oldVideo));
        when(folders.findByPath(anyString())).thenReturn(Optional.empty());
        when(folders.save(any())).thenAnswer(invocation -> {
            Folder folder = invocation.getArgument(0);
            folder.setId("folder-1");
            return folder;
        });
        when(actors.findByName(anyString())).thenReturn(Optional.empty());
        when(actors.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(videos.findByFolderIdAndNameAndActorKey(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        int saved = service.save(List.of(new MediaFile(
                Path.of("media", "Movies", "Alice & Bob - Film.mp4"),
                new ParsedVideoName("Film", Set.of("Alice", "Bob")))));

        assertEquals(1, saved);
        assertEquals(MediaStatus.INACTIVE, oldActor.getStatus());
        assertEquals(MediaStatus.INACTIVE, oldFolder.getStatus());
        assertEquals(MediaStatus.INACTIVE, oldVideo.getStatus());
        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(videos).save(captor.capture());
        Video video = captor.getValue();
        assertEquals("Film", video.getName());
        assertEquals("Alice & Bob - Film.mp4", video.getFileName());
        assertEquals(MediaStatus.ACTIVE, video.getStatus());
        assertEquals("folder-1", video.getFolder().getId());
        assertEquals(MediaPersistenceService.actorKey(Set.of("Alice", "Bob")), video.getActorKey());
        assertEquals(
                Set.of("Alice", "Bob"),
                video.getActors().stream().map(Actor::getName).collect(java.util.stream.Collectors.toSet()));
        ArgumentCaptor<Folder> folderCaptor = ArgumentCaptor.forClass(Folder.class);
        verify(folders).save(folderCaptor.capture());
        assertEquals(
                Path.of("media", "Movies").toString(), folderCaptor.getValue().getPath());
        assertEquals("Movies", folderCaptor.getValue().getType());
        ArgumentCaptor<Actor> actorCaptor = ArgumentCaptor.forClass(Actor.class);
        verify(actors, times(2)).save(actorCaptor.capture());
        assertEquals(
                Set.of("Alice", "Bob"),
                actorCaptor.getAllValues().stream().map(Actor::getName).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void reactivatesExistingEntities() {
        Folder folder = new Folder();
        folder.setId("folder-1");
        folder.setStatus(MediaStatus.INACTIVE);
        Actor actor = new Actor();
        actor.setName("Alice");
        actor.setStatus(MediaStatus.INACTIVE);
        Video video = new Video();
        video.setStatus(MediaStatus.INACTIVE);
        when(folders.findByPath(anyString())).thenReturn(Optional.of(folder));
        when(actors.findByName("Alice")).thenReturn(Optional.of(actor));
        when(videos.findByFolderIdAndNameAndActorKey(eq("folder-1"), eq("Film"), anyString()))
                .thenReturn(Optional.of(video));

        service.save(List.of(new MediaFile(
                Path.of("media", "Movies", "Alice - Film.mp4"), new ParsedVideoName("Film", Set.of("Alice")))));

        assertEquals(MediaStatus.ACTIVE, folder.getStatus());
        assertEquals(MediaStatus.ACTIVE, actor.getStatus());
        Video savedVideo = verifyAndReturnSavedVideo();
        assertSame(video, savedVideo);
        assertEquals(MediaStatus.ACTIVE, savedVideo.getStatus());
    }

    @Test
    void rejectsDuplicateIdentityWithinImport() {
        Folder folder = new Folder();
        folder.setId("folder-1");
        when(folders.findByPath(anyString())).thenReturn(Optional.of(folder));
        Actor actor = new Actor();
        when(actors.findByName("Alice")).thenReturn(Optional.of(actor));
        MediaFile first =
                new MediaFile(Path.of("a", "Movies", "one.mp4"), new ParsedVideoName("Film", Set.of("Alice")));
        MediaFile second =
                new MediaFile(Path.of("b", "Movies", "two.mp4"), new ParsedVideoName("Film", Set.of("Alice")));

        assertThrows(DuplicateVideoException.class, () -> service.save(List.of(first, second)));
    }

    @Test
    void actorKeyHasStableSha256Value() {
        assertEquals(
                "8ee2b723e57e8e0d5bc691a1ae7d107ae84c687da81b8294b6be8bac0d115e52",
                MediaPersistenceService.actorKey(Set.of("Alice", "Bob")));
    }

    private Video verifyAndReturnSavedVideo() {
        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(videos).save(captor.capture());
        return captor.getValue();
    }
}
