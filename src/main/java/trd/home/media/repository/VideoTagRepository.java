package trd.home.media.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import trd.home.media.dao.VideoTag;

public interface VideoTagRepository extends JpaRepository<VideoTag, String> {
    List<VideoTag> findAllByOrderByNameAsc();

    boolean existsByName(String name);
}
