package trd.home.media.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import trd.home.media.constant.MediaStatus;
import trd.home.media.dao.Video;

public interface VideoRepository extends JpaRepository<Video, String> {
    Optional<Video> findByFolderIdAndNameAndActorKey(String folderId, String name, String actorKey);

    List<Video> findDistinctByStatusOrderByName(MediaStatus status);

    List<Video> findDistinctByStatusAndActorsIdOrderByName(MediaStatus status, String actorId);

    List<Video> findDistinctByStatusAndFolderIdOrderByName(MediaStatus status, String folderId);

    Optional<Video> findByIdAndStatus(String id, MediaStatus status);
}
