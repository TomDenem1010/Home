package trd.home.media.service.tag;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import trd.home.media.constant.MediaStatus;
import trd.home.media.dao.Video;
import trd.home.media.dao.VideoTag;
import trd.home.media.exception.MediaVideoNotFoundException;
import trd.home.media.exception.VideoTagNotFoundException;
import trd.home.media.repository.VideoRepository;
import trd.home.media.repository.VideoTagRepository;

class VideoTagAssignmentServiceTest {
    private final VideoRepository videos = mock(VideoRepository.class);
    private final VideoTagRepository tags = mock(VideoTagRepository.class);
    private final VideoTagAssignmentService service = new VideoTagAssignmentService(videos, tags);

    @Test
    void listsAllTagsWithAssignmentState() {
        var video = activeVideo();
        var first = tag("first");
        var second = tag("second");
        video.getTags().add(second);
        when(tags.findAllByOrderByNameAsc()).thenReturn(List.of(first, second));
        var result = service.tagsForVideo("video");
        assertEquals(
                List.of("first", "second"),
                result.stream().map(item -> item.id()).toList());
        assertFalse(result.getFirst().assigned());
        assertTrue(result.getLast().assigned());
        assertEquals("SECOND", result.getLast().name());
    }

    @Test
    void returnsEmptyListWhenNoTagsExist() {
        activeVideo();
        when(tags.findAllByOrderByNameAsc()).thenReturn(List.of());
        assertEquals(List.of(), service.tagsForVideo("video"));
    }

    @Test
    void addsTagWithoutDuplicatingExistingAssignment() {
        var video = activeVideo();
        var tag = tag("tag");
        when(tags.findById("tag")).thenReturn(Optional.of(tag));
        service.setAssigned("video", "tag", true);
        service.setAssigned("video", "tag", true);
        assertEquals(1, video.getTags().size());
        assertTrue(video.getTags().contains(tag));
    }

    @Test
    void doesNotDuplicateAssignmentWhenRepositoryReturnsAnotherInstanceOfSameTag() {
        var video = activeVideo();
        var existing = tag("tag");
        video.getTags().add(existing);
        when(tags.findById("tag")).thenReturn(Optional.of(tag("tag")));

        service.setAssigned("video", "tag", true);

        assertEquals(java.util.Set.of(existing), video.getTags());
    }

    @Test
    void addsNewTagWhenAnotherTagIsAlreadyAssigned() {
        var video = activeVideo();
        var existing = tag("existing");
        var added = tag("new");
        video.getTags().add(existing);
        when(tags.findById("new")).thenReturn(Optional.of(added));

        service.setAssigned("video", "new", true);

        assertEquals(java.util.Set.of(existing, added), video.getTags());
    }

    @Test
    void removesTagByIdAndKeepsOtherAssignments() {
        var video = activeVideo();
        video.getTags().add(tag("tag"));
        var other = tag("other");
        video.getTags().add(other);
        when(tags.findById("tag")).thenReturn(Optional.of(tag("tag")));
        service.setAssigned("video", "tag", false);
        service.setAssigned("video", "tag", false);
        assertEquals(java.util.Set.of(other), video.getTags());
        verify(tags, never()).delete(any());
    }

    @Test
    void rejectsUnavailableVideoForReadAndWrite() {
        when(videos.findByIdAndStatus("missing", MediaStatus.ACTIVE)).thenReturn(Optional.empty());
        assertThrows(MediaVideoNotFoundException.class, () -> service.tagsForVideo("missing"));
        assertThrows(MediaVideoNotFoundException.class, () -> service.setAssigned("missing", "tag", true));
        verifyNoInteractions(tags);
    }

    @Test
    void rejectsMissingTagWithoutChangingAssignments() {
        var video = activeVideo();
        var existing = tag("existing");
        video.getTags().add(existing);
        when(tags.findById("missing")).thenReturn(Optional.empty());
        assertThrows(VideoTagNotFoundException.class, () -> service.setAssigned("video", "missing", true));
        assertEquals(java.util.Set.of(existing), video.getTags());
    }

    private Video activeVideo() {
        var video = new Video();
        when(videos.findByIdAndStatus("video", MediaStatus.ACTIVE)).thenReturn(Optional.of(video));
        return video;
    }

    private VideoTag tag(String id) {
        var tag = new VideoTag();
        tag.setId(id);
        tag.setName(id.toUpperCase(java.util.Locale.ROOT));
        return tag;
    }
}
