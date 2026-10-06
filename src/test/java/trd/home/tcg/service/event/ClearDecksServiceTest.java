package trd.home.tcg.service.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.ApplicationEventProcessor;
import trd.home.common.event.ApplicationEventQueue;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.tcg.service.deck.CardmarketDeckClearer;

class ClearDecksServiceTest {
    private final ApplicationEventQueue queue = mock(ApplicationEventQueue.class);
    private final FrontendNotificationPublisher notifications = mock(FrontendNotificationPublisher.class);
    private final CardmarketDeckClearer clearer = mock(CardmarketDeckClearer.class);
    private final ClearDecksService service =
            new ClearDecksService(new ApplicationEventProcessor(queue, notifications), clearer);

    @Test
    void clearsDecksAndReportsSuccess() {
        var event = new ApplicationEvent(EventType.CLEAR_DECKS);
        service.process(event);
        verify(clearer).clear();
        assertEquals(EventStatus.DONE, event.getStatus());
        verify(notifications).publish(null, FrontendNotificationType.SUCCESS, "Decks were cleared successfully.");
    }

    @Test
    void reportsFailure() {
        var event = new ApplicationEvent(EventType.CLEAR_DECKS);
        doThrow(new IllegalStateException("database failure")).when(clearer).clear();
        service.process(event);
        assertEquals(EventStatus.ERROR, event.getStatus());
        verify(notifications).publish(null, FrontendNotificationType.ERROR, "Failed to clear decks: database failure");
    }
}
