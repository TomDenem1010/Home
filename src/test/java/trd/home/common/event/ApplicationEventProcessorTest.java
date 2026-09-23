package trd.home.common.event;

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
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.tcg.exception.DeckImportException;

class ApplicationEventProcessorTest {

    private final ApplicationEventQueue eventQueue = mock(ApplicationEventQueue.class);
    private final FrontendNotificationPublisher notificationPublisher = mock(FrontendNotificationPublisher.class);
    private final ApplicationEventProcessor processor =
            new ApplicationEventProcessor(eventQueue, notificationPublisher);

    @Test
    void completesSuccessfulEventAndPublishesNotification() {
        ApplicationEvent event = new ApplicationEvent(EventType.SAVE_DECKS_FROM_RESOURCE);
        @SuppressWarnings("unchecked")
        Supplier<String> operation = mock(Supplier.class);
        org.mockito.Mockito.when(operation.get()).thenReturn("Completed");
        List<EventStatus> savedStatuses = new ArrayList<>();
        doAnswer(invocation -> {
                    savedStatuses.add(event.getStatus());
                    return event;
                })
                .when(eventQueue)
                .save(event);

        processor.process(event, operation, "Failed: ");

        assertEquals(EventStatus.DONE, event.getStatus());
        assertNotNull(event.getProcessedAt());
        InOrder order = inOrder(eventQueue, operation, notificationPublisher);
        order.verify(eventQueue).save(event);
        order.verify(operation).get();
        order.verify(eventQueue).save(event);
        order.verify(notificationPublisher).publish(null, FrontendNotificationType.SUCCESS, "Completed");
        assertEquals(List.of(EventStatus.PROCESSING, EventStatus.DONE), savedStatuses);
    }

    @Test
    void failsEventWhenOperationFails() {
        ApplicationEvent event = new ApplicationEvent(EventType.SAVE_DECKS_FROM_RESOURCE);
        @SuppressWarnings("unchecked")
        Supplier<String> operation = mock(Supplier.class);
        doThrow(new DeckImportException("resource missing")).when(operation).get();

        processor.process(event, operation, "Failed: ");

        assertEquals(EventStatus.ERROR, event.getStatus());
        assertNull(event.getProcessedAt());
        assertEquals("resource missing", event.getErrorMessage());
        verify(notificationPublisher).publish(null, FrontendNotificationType.ERROR, "Failed: resource missing");
        verify(eventQueue, times(2)).save(event);
    }

    @Test
    void keepsCompletedEventDoneWhenNotificationFails() {
        ApplicationEvent event = new ApplicationEvent(EventType.SAVE_DECKS_FROM_RESOURCE);
        doThrow(new DeckImportException("notification unavailable"))
                .when(notificationPublisher)
                .publish(null, FrontendNotificationType.SUCCESS, "Completed");

        assertThrows(DeckImportException.class, () -> processor.process(event, () -> "Completed", "Failed: "));

        assertEquals(EventStatus.DONE, event.getStatus());
    }
}
