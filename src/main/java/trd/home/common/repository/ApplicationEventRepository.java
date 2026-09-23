package trd.home.common.repository;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;

public interface ApplicationEventRepository extends JpaRepository<ApplicationEvent, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ApplicationEvent> findFirstByTypeAndStatusOrderByCreatedAtAsc(EventType type, EventStatus status);

    List<ApplicationEvent> findTop100ByTypeAndStatusOrderByCreatedAtAsc(EventType type, EventStatus status);

    List<ApplicationEvent> findAllByStatusAndLastModifiedAtBeforeOrderByLastModifiedAtAsc(
            EventStatus status, Instant lastModifiedBefore);
}
