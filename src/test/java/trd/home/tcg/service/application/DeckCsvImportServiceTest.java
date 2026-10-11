package trd.home.tcg.service.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import trd.home.common.constant.EventType;
import trd.home.common.event.*;

class DeckCsvImportServiceTest {
    private final ApplicationEventQueue events = mock(ApplicationEventQueue.class);
    private final FrontendNotificationPublisher notifications = mock(FrontendNotificationPublisher.class);
    private final DeckCsvImportService service = new DeckCsvImportService(events, notifications);

    @Test
    void queuesImportOfAllCsvDecksWithoutSelection() {
        service.importDecks(null);
        var order = inOrder(events, notifications);
        order.verify(events).enqueue(EventType.IMPORT_DECKS_FROM_CSV, null);
        order.verify(notifications).publish(FrontendNotificationType.WARNING, "CSV deck importing has started.");
    }

    @Test
    void queuesImportOfSelectedCsvDeck() {
        service.importDecks("deck-id");
        var order = inOrder(events, notifications);
        order.verify(events).enqueue(EventType.IMPORT_DECKS_FROM_CSV, "deck-id");
        order.verify(notifications).publish(FrontendNotificationType.WARNING, "CSV deck importing has started.");
    }

    @Test
    void doesNotNotifyWhenQueueingFails() {
        var failure = new IllegalStateException("database");
        when(events.enqueue(EventType.IMPORT_DECKS_FROM_CSV, null)).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> service.importDecks(null)));
        verifyNoInteractions(notifications);
    }
}
