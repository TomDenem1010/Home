package trd.home.media.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;
import trd.home.media.constant.MediaStatus;
import trd.home.media.dao.Actor;
import trd.home.media.dao.Folder;
import trd.home.media.dao.Video;

class MediaDtoTest {

    @Test
    void mapsCompleteVideoGraph() {
        Folder folder = new Folder();
        folder.setId("folder-id");
        folder.setPath("media/Movies");
        folder.setType("Movies");
        folder.setStatus(MediaStatus.ACTIVE);
        Actor actor = new Actor();
        actor.setId("actor-id");
        actor.setName("Alice");
        actor.setStatus(MediaStatus.INACTIVE);
        Video video = new Video();
        video.setId("video-id");
        video.setName("Film");
        video.setFolder(folder);
        video.setActors(Set.of(actor));
        video.setStatus(MediaStatus.ACTIVE);

        VideoDto dto = VideoDto.from(video);

        assertEquals("video-id", dto.id());
        assertEquals("Film", dto.name());
        assertEquals("folder-id", dto.folder().id());
        assertEquals("media/Movies", dto.folder().path());
        assertEquals("Movies", dto.folder().type());
        assertEquals(MediaStatus.ACTIVE, dto.folder().status());
        ActorDto actorDto = dto.actors().iterator().next();
        assertEquals("actor-id", actorDto.id());
        assertEquals("Alice", actorDto.name());
        assertEquals(MediaStatus.INACTIVE, actorDto.status());
        assertEquals(MediaStatus.ACTIVE, dto.status());
    }
}
