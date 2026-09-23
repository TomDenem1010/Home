package trd.home.tcg.service.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.repository.CardmarketDeckRepository;
import trd.home.tcg.service.deck.CardmarketDeckSaver;
import trd.home.tcg.service.file.DeckFileReader;

class SaveDecksFromResourceServiceTest {

    private final ApplicationEventRepository eventRepository = mock(ApplicationEventRepository.class);
    private final CardmarketDeckSaver deckSaver = mock(CardmarketDeckSaver.class);
    private final DeckFileReader deckFileReader = mock(DeckFileReader.class);
    private final CardmarketDeckRepository deckRepository = mock(CardmarketDeckRepository.class);
    private final FrontendNotificationPublisher notificationPublisher = mock(FrontendNotificationPublisher.class);
    private final SaveDecksFromResourceService service = new SaveDecksFromResourceService(
            new TcgEventProcessor(eventRepository, notificationPublisher), deckSaver, deckFileReader, deckRepository);

    @Test
    void readsAndSavesEveryResourceDeck() {
        ApplicationEvent event = new ApplicationEvent(EventType.SAVE_DECKS_FROM_RESOURCE);
        CardmarketDeck firstDeck = new CardmarketDeck();
        CardmarketDeck secondDeck = new CardmarketDeck();
        when(deckFileReader.read()).thenReturn(List.of(firstDeck, secondDeck));

        service.process(event);

        verify(deckSaver).save(firstDeck);
        verify(deckSaver).save(secondDeck);
        assertEquals(EventStatus.DONE, event.getStatus());
        verify(notificationPublisher).publish(null, FrontendNotificationType.SUCCESS, "Decks were saved successfully.");
    }

    @Test
    void savesOnlyTheResourceMatchingTheSelectedDeck() {
        ApplicationEvent event = new ApplicationEvent(EventType.SAVE_DECKS_FROM_RESOURCE, "selected-id");
        CardmarketDeck selected = deck("Selected");
        CardmarketDeck other = deck("Other");
        when(deckRepository.findById("selected-id")).thenReturn(Optional.of(deck("Selected")));
        when(deckFileReader.read()).thenReturn(List.of(other, selected));

        service.process(event);

        assertEquals(EventStatus.DONE, event.getStatus());
        verify(deckSaver).save(selected);
        verify(deckSaver, never()).save(other);
    }

    @Test
    void failsWhenSelectedDeckDoesNotExist() {
        ApplicationEvent event = new ApplicationEvent(EventType.SAVE_DECKS_FROM_RESOURCE, "missing-id");
        when(deckRepository.findById("missing-id")).thenReturn(Optional.empty());
        when(deckFileReader.read()).thenReturn(List.of());

        service.process(event);

        assertEquals(EventStatus.ERROR, event.getStatus());
        verify(notificationPublisher)
                .publish(null, FrontendNotificationType.ERROR, "Failed to save decks: Deck not found: missing-id");
    }

    @Test
    void failsWhenSelectedDeckHasNoResourceFile() {
        ApplicationEvent event = new ApplicationEvent(EventType.SAVE_DECKS_FROM_RESOURCE, "selected-id");
        when(deckRepository.findById("selected-id")).thenReturn(Optional.of(deck("Selected")));
        when(deckFileReader.read()).thenReturn(List.of(deck("Other")));

        service.process(event);

        assertEquals(EventStatus.ERROR, event.getStatus());
        verify(notificationPublisher)
                .publish(
                        null,
                        FrontendNotificationType.ERROR,
                        "Failed to save decks: No resource file found for deck: Selected");
        verify(deckSaver, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private static CardmarketDeck deck(String name) {
        CardmarketDeck deck = new CardmarketDeck();
        deck.setName(name);
        return deck;
    }
}
