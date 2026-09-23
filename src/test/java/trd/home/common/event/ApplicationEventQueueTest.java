package trd.home.common.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.repository.ApplicationEventRepository;

class ApplicationEventQueueTest {

    private final ApplicationEventRepository repository = mock(ApplicationEventRepository.class);
    private final ApplicationEventQueue queue = new ApplicationEventQueue(repository);

    @Test
    void enqueuesTypedEvent() {
        queue.enqueue(EventType.IMPORT_MEDIA, "C:\\Media");

        verify(repository)
                .save(org.mockito.ArgumentMatchers.argThat(
                        event -> event.getType() == EventType.IMPORT_MEDIA && "C:\\Media".equals(event.getMessage())));
    }

    @Test
    void claimsAndMarksOldestPendingEventAsProcessing() {
        ApplicationEvent event = new ApplicationEvent(EventType.IMPORT_MEDIA);
        when(repository.findFirstByTypeAndStatusOrderByCreatedAtAsc(EventType.IMPORT_MEDIA, EventStatus.TO_DO))
                .thenReturn(Optional.of(event));
        when(repository.save(event)).thenReturn(event);

        Optional<ApplicationEvent> claimed = queue.claimNext(EventType.IMPORT_MEDIA);

        assertTrue(claimed.isPresent());
        assertEquals(EventStatus.PROCESSING, event.getStatus());
        InOrder order = inOrder(repository);
        order.verify(repository).findFirstByTypeAndStatusOrderByCreatedAtAsc(EventType.IMPORT_MEDIA, EventStatus.TO_DO);
        order.verify(repository).save(event);
    }

    @Test
    void limitsPendingEvents() {
        List<ApplicationEvent> events = List.of(
                new ApplicationEvent(EventType.FRONTEND_NOTIFICATION),
                new ApplicationEvent(EventType.FRONTEND_NOTIFICATION));
        when(repository.findTop100ByTypeAndStatusOrderByCreatedAtAsc(
                        EventType.FRONTEND_NOTIFICATION, EventStatus.TO_DO))
                .thenReturn(events);

        assertEquals(1, queue.findPending(EventType.FRONTEND_NOTIFICATION, 1).size());
    }
}
