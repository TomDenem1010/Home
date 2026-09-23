package trd.home.tcg.service.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.tcg.exception.DeckImportException;

class TcgEventProcessorTest {

    private final ApplicationEventRepository eventRepository = mock(ApplicationEventRepository.class);
    private final FrontendNotificationPublisher notificationPublisher = mock(FrontendNotificationPublisher.class);
    private final TcgEventProcessor processor = new TcgEventProcessor(eventRepository, notificationPublisher);

    @Test
    void completesSuccessfulEventAndPublishesNotification() {
        ApplicationEvent event = new ApplicationEvent(EventType.SAVE_DECKS_FROM_RESOURCE);
        Runnable operation = mock(Runnable.class);
        List<EventStatus> savedStatuses = new ArrayList<>();
        doAnswer(invocation -> {
                    savedStatuses.add(event.getStatus());
                    return event;
                })
                .when(eventRepository)
                .save(event);

        processor.process(event, operation, "Completed", "Failed: ");

        assertEquals(EventStatus.DONE, event.getStatus());
        assertNotNull(event.getProcessedAt());
        verify(operation).run();
        verify(notificationPublisher).publish(null, FrontendNotificationType.SUCCESS, "Completed");
        InOrder order = inOrder(eventRepository, operation, notificationPublisher);
        order.verify(eventRepository).save(event);
        order.verify(operation).run();
        order.verify(eventRepository).save(event);
        order.verify(notificationPublisher).publish(null, FrontendNotificationType.SUCCESS, "Completed");
        verify(eventRepository, times(2)).save(event);
        assertEquals(List.of(EventStatus.PROCESSING, EventStatus.DONE), savedStatuses);
    }

    @Test
    void failsEventWhenOperationFails() {
        ApplicationEvent event = new ApplicationEvent(EventType.SAVE_DECKS_FROM_RESOURCE);
        Runnable operation = mock(Runnable.class);
        doThrow(new DeckImportException("resource missing")).when(operation).run();

        processor.process(event, operation, "Completed", "Failed: ");

        assertEquals(EventStatus.ERROR, event.getStatus());
        assertNull(event.getProcessedAt());
        assertEquals("resource missing", event.getErrorMessage());
        verify(notificationPublisher).publish(null, FrontendNotificationType.ERROR, "Failed: resource missing");
        verify(eventRepository, times(2)).save(event);
    }

    @Test
    void keepsCompletedEventDoneWhenNotificationFails() {
        ApplicationEvent event = new ApplicationEvent(EventType.SAVE_DECKS_FROM_RESOURCE);
        doThrow(new DeckImportException("notification unavailable"))
                .when(notificationPublisher)
                .publish(null, FrontendNotificationType.SUCCESS, "Completed");

        assertThrows(DeckImportException.class, () -> processor.process(event, () -> {}, "Completed", "Failed: "));

        assertEquals(EventStatus.DONE, event.getStatus());
    }
}
