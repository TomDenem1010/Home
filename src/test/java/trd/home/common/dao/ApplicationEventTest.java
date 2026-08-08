package trd.home.common.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;

class ApplicationEventTest {

    @Test
    void followsSuccessfulLifecycle() {
        ApplicationEvent event = new ApplicationEvent(EventType.REFRESH_DECK_PRICES);

        assertEquals(EventStatus.TO_DO, event.getStatus());
        event.markProcessing();
        assertEquals(EventStatus.PROCESSING, event.getStatus());
        event.markDone();

        assertEquals(EventStatus.DONE, event.getStatus());
        assertNotNull(event.getProcessedAt());
        assertNull(event.getErrorMessage());
    }

    @Test
    void marksFailedEventAsError() {
        ApplicationEvent event = new ApplicationEvent(EventType.SAVE_DECKS_FROM_RESOURCE);

        event.markProcessing();
        event.markFailed(new IllegalStateException("Unable to process event"));

        assertEquals(EventStatus.ERROR, event.getStatus());
        assertNull(event.getProcessedAt());
        assertEquals("Unable to process event", event.getErrorMessage());
    }
}
