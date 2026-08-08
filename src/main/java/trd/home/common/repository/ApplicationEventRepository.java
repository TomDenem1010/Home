package trd.home.common.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;

public interface ApplicationEventRepository extends JpaRepository<ApplicationEvent, String> {

    Optional<ApplicationEvent> findFirstByTypeAndProcessedAtIsNullAndErrorMessageIsNullOrderByCreatedAtAsc(
            EventType type);
}
