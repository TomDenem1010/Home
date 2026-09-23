package trd.home.common.event;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.repository.ApplicationEventRepository;

@Service
@RequiredArgsConstructor
public class ApplicationEventQueue {

    private final ApplicationEventRepository repository;

    public ApplicationEvent enqueue(EventType type, String message) {
        return repository.save(new ApplicationEvent(type, message));
    }

    @Transactional
    public Optional<ApplicationEvent> claimNext(EventType type) {
        return repository
                .findFirstByTypeAndStatusOrderByCreatedAtAsc(type, EventStatus.TO_DO)
                .map(event -> {
                    event.markProcessing();
                    return repository.save(event);
                });
    }

    public Optional<ApplicationEvent> findById(String id) {
        return repository.findById(id);
    }

    public List<ApplicationEvent> findPending(EventType type, int limit) {
        return repository.findTop100ByTypeAndStatusOrderByCreatedAtAsc(type, EventStatus.TO_DO).stream()
                .limit(limit)
                .toList();
    }

    public List<ApplicationEvent> findStuckBefore(Instant threshold) {
        return repository.findAllByStatusAndLastModifiedAtBeforeOrderByLastModifiedAtAsc(
                EventStatus.PROCESSING, threshold);
    }

    public ApplicationEvent save(ApplicationEvent event) {
        return repository.save(event);
    }

    public List<ApplicationEvent> saveAll(List<ApplicationEvent> events) {
        return repository.saveAll(events);
    }
}
