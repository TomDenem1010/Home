package trd.home.media.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import trd.home.media.constant.MediaStatus;
import trd.home.media.dao.Folder;

public interface FolderRepository extends JpaRepository<Folder, String> {
    Optional<Folder> findByPath(String value);

    List<Folder> findByStatusOrderByPath(MediaStatus status);
}
