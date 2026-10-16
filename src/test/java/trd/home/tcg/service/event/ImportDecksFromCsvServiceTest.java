package trd.home.tcg.service.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mockito;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.ApplicationEventProcessor;
import trd.home.common.event.ApplicationEventQueue;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.repository.CardmarketDeckRepository;
import trd.home.tcg.service.deck.DeckCsvSaver;
import trd.home.tcg.service.file.DeckCsvFileReader;

class ImportDecksFromCsvServiceTest {

    private final ApplicationEventQueue eventQueue = mock(ApplicationEventQueue.class);
    private final DeckCsvSaver deckSaver = mock(DeckCsvSaver.class);
    private final DeckCsvFileReader deckCsvFileReader = mock(DeckCsvFileReader.class);
    private final CardmarketDeckRepository deckRepository = mock(CardmarketDeckRepository.class);
    private final FrontendNotificationPublisher notificationPublisher = mock(FrontendNotificationPublisher.class);
    private final ImportDecksFromCsvService service = new ImportDecksFromCsvService(
            new ApplicationEventProcessor(eventQueue, notificationPublisher),
            deckSaver,
            deckCsvFileReader,
            deckRepository);

    @Test
    void reportsWrappedDeckSaveFailure() {
        var deck = new CardmarketDeck();
        when(deckCsvFileReader.read()).thenReturn(List.of(deck));
        Mockito.doThrow(new IllegalStateException("save failed"))
                .when(deckSaver)
                .save(deck);
        var event = new ApplicationEvent(EventType.IMPORT_DECKS_FROM_CSV);
        service.process(event);
        assertEquals(EventStatus.ERROR, event.getStatus());
        assertEquals("Unable to import decks from CSV", event.getErrorMessage());
    }

    @Test
    void readsAndImportsEveryCsvDeck() {
        ApplicationEvent event = new ApplicationEvent(EventType.IMPORT_DECKS_FROM_CSV);
        CardmarketDeck firstDeck = new CardmarketDeck();
        CardmarketDeck secondDeck = new CardmarketDeck();
        when(deckCsvFileReader.read()).thenReturn(List.of(firstDeck, secondDeck));
        when(deckSaver.save(firstDeck)).thenReturn(true);
        when(deckSaver.save(secondDeck)).thenReturn(true);

        service.process(event);

        InOrder order = inOrder(deckSaver);
        order.verify(deckSaver).save(firstDeck);
        order.verify(deckSaver).save(secondDeck);
        assertEquals(EventStatus.DONE, event.getStatus());
        verify(notificationPublisher)
                .publish(
                        null,
                        FrontendNotificationType.SUCCESS,
                        "CSV deck importing completed: 2 imported, 0 unchanged.");
    }

    @Test
    void savesOnlyTheResourceMatchingTheSelectedDeck() {
        ApplicationEvent event = new ApplicationEvent(EventType.IMPORT_DECKS_FROM_CSV, "selected-id");
        CardmarketDeck selected = deck("Selected");
        CardmarketDeck other = deck("Other");
        when(deckRepository.findById("selected-id")).thenReturn(Optional.of(deck("Selected")));
        when(deckCsvFileReader.read()).thenReturn(List.of(other, selected));
        when(deckSaver.save(selected)).thenReturn(true);

        service.process(event);

        assertEquals(EventStatus.DONE, event.getStatus());
        verify(deckSaver).save(selected);
        verify(deckSaver, never()).save(other);
    }

    @Test
    void failsWhenSelectedDeckDoesNotExist() {
        ApplicationEvent event = new ApplicationEvent(EventType.IMPORT_DECKS_FROM_CSV, "missing-id");
        when(deckRepository.findById("missing-id")).thenReturn(Optional.empty());
        when(deckCsvFileReader.read()).thenReturn(List.of());

        service.process(event);

        assertEquals(EventStatus.ERROR, event.getStatus());
        verify(notificationPublisher)
                .publish(
                        null, FrontendNotificationType.ERROR, "Failed to import CSV decks: Deck not found: missing-id");
    }

    @Test
    void failsWhenSelectedDeckHasNoResourceFile() {
        ApplicationEvent event = new ApplicationEvent(EventType.IMPORT_DECKS_FROM_CSV, "selected-id");
        when(deckRepository.findById("selected-id")).thenReturn(Optional.of(deck("Selected")));
        when(deckCsvFileReader.read()).thenReturn(List.of(deck("Other")));

        service.process(event);

        assertEquals(EventStatus.ERROR, event.getStatus());
        verify(notificationPublisher)
                .publish(
                        null,
                        FrontendNotificationType.ERROR,
                        "Failed to import CSV decks: No CSV file found for deck: Selected");
        verify(deckSaver, never()).save(ArgumentMatchers.any());
    }

    @Test
    void failsWhenNoDeckWasImported() {
        when(deckCsvFileReader.read()).thenReturn(List.of());
        var event = new ApplicationEvent(EventType.IMPORT_DECKS_FROM_CSV);
        service.process(event);
        assertEquals(EventStatus.ERROR, event.getStatus());
        assertEquals("No CSV decks imported: 0 imported, 0 unchanged.", event.getErrorMessage());
        verify(notificationPublisher)
                .publish(
                        null,
                        FrontendNotificationType.ERROR,
                        "Failed to import CSV decks: No CSV decks imported: 0 imported, 0 unchanged.");
    }

    @Test
    void failsWhenAllDecksAreUnchanged() {
        when(deckCsvFileReader.read()).thenReturn(List.of(deck("Unchanged")));
        var event = new ApplicationEvent(EventType.IMPORT_DECKS_FROM_CSV);
        service.process(event);
        assertEquals(EventStatus.ERROR, event.getStatus());
        assertEquals("No CSV decks imported: 0 imported, 1 unchanged.", event.getErrorMessage());
    }

    private static CardmarketDeck deck(String name) {
        CardmarketDeck deck = new CardmarketDeck();
        deck.setName(name);
        return deck;
    }
}
