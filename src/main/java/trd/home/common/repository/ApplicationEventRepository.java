package trd.home.common.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;

public interface ApplicationEventRepository extends JpaRepository<ApplicationEvent, String> {

    Optional<ApplicationEvent> findFirstByTypeAndStatusOrderByCreatedAtAsc(EventType type, EventStatus status);

    List<ApplicationEvent> findTop100ByTypeAndStatusOrderByCreatedAtAsc(EventType type, EventStatus status);

    List<ApplicationEvent> findAllByStatusAndLastModifiedAtBeforeOrderByLastModifiedAtAsc(
            EventStatus status, Instant lastModifiedBefore);
}
