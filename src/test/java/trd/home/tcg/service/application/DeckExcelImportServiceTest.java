package trd.home.tcg.service.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import trd.home.common.constant.EventType;
import trd.home.common.event.*;

class DeckExcelImportServiceTest {
    private final ApplicationEventQueue events = mock(ApplicationEventQueue.class);
    private final FrontendNotificationPublisher notifications = mock(FrontendNotificationPublisher.class);
    private final DeckExcelImportService commands = new DeckExcelImportService(events, notifications);

    @Test
    void queuesDedicatedEventWithoutFilePayload() {
        commands.importDecks();
        var order = inOrder(events, notifications);
        order.verify(events).enqueue(EventType.IMPORT_DECKS_FROM_XLSX, null);
        order.verify(notifications).publish(FrontendNotificationType.WARNING, "XLSX deck importing has started.");
    }

    @Test
    void doesNotNotifyWhenQueueingFails() {
        var failure = new IllegalStateException("database");
        when(events.enqueue(EventType.IMPORT_DECKS_FROM_XLSX, null)).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, commands::importDecks));
        verifyNoInteractions(notifications);
    }
}
