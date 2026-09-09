package trd.home.media.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import trd.home.media.constant.MediaStatus;
import trd.home.media.dao.Actor;

public interface ActorRepository extends JpaRepository<Actor, String> {
    Optional<Actor> findByName(String value);

    List<Actor> findByStatusOrderByName(MediaStatus status);
}
