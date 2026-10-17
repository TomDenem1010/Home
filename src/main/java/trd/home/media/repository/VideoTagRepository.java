package trd.home.media.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import trd.home.media.dao.VideoTag;

public interface VideoTagRepository extends JpaRepository<VideoTag, String> {
    boolean existsByName(String name);
}
