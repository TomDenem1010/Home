package trd.home.media.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import trd.home.media.constant.MediaStatus;
import trd.home.media.dao.Video;

public interface VideoRepository extends JpaRepository<Video, String> {
    Optional<Video> findByFolderIdAndNameAndActorKey(String folderId, String name, String actorKey);

    @EntityGraph(attributePaths = {"folder", "actors"})
    List<Video> findDistinctByStatusOrderByName(MediaStatus status);

    @EntityGraph(attributePaths = {"folder", "actors"})
    List<Video> findDistinctByStatusAndActorsIdOrderByName(MediaStatus status, String actorId);

    @EntityGraph(attributePaths = {"folder", "actors"})
    List<Video> findDistinctByStatusAndFolderIdOrderByName(MediaStatus status, String folderId);

    Optional<Video> findByIdAndStatus(String id, MediaStatus status);
}
