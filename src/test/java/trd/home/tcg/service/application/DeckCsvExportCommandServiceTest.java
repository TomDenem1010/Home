package trd.home.tcg.service.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import trd.home.common.constant.EventType;
import trd.home.common.event.*;

class DeckCsvExportCommandServiceTest {
    private final ApplicationEventQueue events = mock(ApplicationEventQueue.class);
    private final FrontendNotificationPublisher notifications = mock(FrontendNotificationPublisher.class);
    private final DeckCsvExportCommandService commands = new DeckCsvExportCommandService(events, notifications);

    @Test
    void queuesDedicatedEventWithoutFilePayload() {
        commands.exportDecks();
        var order = inOrder(events, notifications);
        order.verify(events).enqueue(EventType.EXPORT_DECKS_TO_CSV, null);
        order.verify(notifications).publish(FrontendNotificationType.WARNING, "CSV deck exporting has started.");
    }

    @Test
    void doesNotNotifyWhenQueueingFails() {
        var failure = new IllegalStateException("database");
        when(events.enqueue(EventType.EXPORT_DECKS_TO_CSV, null)).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, commands::exportDecks));
        verifyNoInteractions(notifications);
    }
}
