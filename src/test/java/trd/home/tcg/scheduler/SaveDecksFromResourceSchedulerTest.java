package trd.home.tcg.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.service.CardmarketDeckSaver;
import trd.home.tcg.service.file.DeckFileReader;

class SaveDecksFromResourceSchedulerTest {

    private final ApplicationEventRepository eventRepository = mock(ApplicationEventRepository.class);
    private final CardmarketDeckSaver deckSaver = mock(CardmarketDeckSaver.class);
    private final DeckFileReader deckFileReader = mock(DeckFileReader.class);
    private final SaveDecksFromResourceScheduler scheduler =
            new SaveDecksFromResourceScheduler(eventRepository, deckSaver, deckFileReader);

    @Test
    void processesOldestPendingEvent() {
        ApplicationEvent event = new ApplicationEvent(EventType.SAVE_DECKS_FROM_RESOURCE);
        CardmarketDeck deck = new CardmarketDeck();
        when(eventRepository.findFirstByTypeAndStatusOrderByCreatedAtAsc(
                        EventType.SAVE_DECKS_FROM_RESOURCE, EventStatus.TO_DO))
                .thenReturn(Optional.of(event));
        when(deckFileReader.read()).thenReturn(List.of(deck));

        scheduler.processNextEvent();

        InOrder order = inOrder(eventRepository, deckSaver);
        order.verify(eventRepository).save(event);
        order.verify(deckSaver).save(deck);
        order.verify(eventRepository).save(event);
        assertEquals(EventStatus.DONE, event.getStatus());
        assertNotNull(event.getProcessedAt());
    }

    @Test
    void storesErrorWhenProcessingFails() {
        ApplicationEvent event = new ApplicationEvent(EventType.SAVE_DECKS_FROM_RESOURCE);
        CardmarketDeck deck = new CardmarketDeck();
        when(eventRepository.findFirstByTypeAndStatusOrderByCreatedAtAsc(
                        EventType.SAVE_DECKS_FROM_RESOURCE, EventStatus.TO_DO))
                .thenReturn(Optional.of(event));
        when(deckFileReader.read()).thenReturn(List.of(deck));
        doThrow(new IllegalStateException("Unable to save deck"))
                .when(deckSaver)
                .save(deck);

        scheduler.processNextEvent();

        assertEquals(EventStatus.ERROR, event.getStatus());
        assertNull(event.getProcessedAt());
        assertEquals("Unable to save deck", event.getErrorMessage());
        InOrder order = inOrder(eventRepository, deckSaver);
        order.verify(eventRepository).save(event);
        order.verify(deckSaver).save(deck);
        order.verify(eventRepository).save(event);
    }
}
