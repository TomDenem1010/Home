package trd.home.tcg.service.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.constant.DeckStatus;
import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dao.CardmarketDeckVersion;
import trd.home.tcg.exception.DeckEditException;
import trd.home.tcg.repository.CardmarketCardRepository;
import trd.home.tcg.repository.CardmarketDeckRepository;

class DeckEditServiceTest {
    private final CardmarketDeckRepository decks = mock(CardmarketDeckRepository.class);
    private final CardmarketCardRepository cards = mock(CardmarketCardRepository.class);
    private final DeckEditService service = new DeckEditService(decks, cards);
    private final CardmarketDeck deck = new CardmarketDeck();
    private final CardmarketDeckVersion original = new CardmarketDeckVersion();
    private final CardmarketCard card = new CardmarketCard();

    DeckEditServiceTest() {
        deck.setId("deck");
        deck.setName("Deck");
        original.setId("old");
        original.setVersion("v9");
        card.setId("card");
        card.setLink("https://www.cardmarket.com/en/Magic/Products/Singles/Test/Card");
        original.addCard(card, 3);
        deck.addVersion(original);
        when(decks.findById("deck")).thenReturn(Optional.of(deck));
    }

    @Test
    void editExceptionUsesCentralNotificationHandling() {
        var publisher = mock(trd.home.common.event.FrontendNotificationPublisher.class);
        var handler = new trd.home.common.exception.HomeExceptionHandler(publisher);
        handler.handle(new DeckEditException("Select at least one card."));
        verify(publisher).publish(trd.home.common.event.FrontendNotificationType.ERROR, "Select at least one card.");
    }

    @Test
    void removalCreatesEmptyVersionAndPreservesHistoryAndCard() {
        service.removeCards("deck", "old", List.of("card"));
        assertEquals("v10", deck.getCurrentVersion().getVersion());
        assertTrue(deck.getCurrentVersion().getCards().isEmpty());
        assertEquals(3, original.getCards().iterator().next().getQuantity());
        assertEquals(2, deck.getVersions().size());
        verifyNoInteractions(cards);
        verify(decks).save(deck);
    }

    @Test
    void existingCardQuantityIsIncreasedOnlyInNewVersion() {
        when(cards.findByLinkAndFoilType(card.getLink(), CardFoilType.NO)).thenReturn(Optional.of(card));
        service.addCard("deck", "old", 2, card.getLink(), CardFoilType.NO);
        assertEquals(5, deck.getCurrentVersion().getCards().iterator().next().getQuantity());
        assertEquals(3, original.getCards().iterator().next().getQuantity());
        verify(cards, never()).save(any());
    }

    @Test
    void newCardIsPersistedAndOtherCardsAreCopied() {
        when(cards.save(any())).thenAnswer(call -> call.getArgument(0));
        service.addCard("deck", "old", 1, card.getLink(), CardFoilType.FOIL);
        assertEquals(2, deck.getCurrentVersion().getCards().size());
        assertEquals(1, original.getCards().size());
        verify(cards).save(any());
    }

    @Test
    void invalidSelectionsDoNotCreateVersions() {
        assertThrows(DeckEditException.class, () -> service.removeCards("deck", "old", List.of()));
        assertThrows(DeckEditException.class, () -> service.removeCards("deck", "old", null));
        assertThrows(DeckEditException.class, () -> service.removeCards("deck", "old", List.of("foreign")));
        assertEquals(1, deck.getVersions().size());
        verify(decks, never()).save(any());
    }

    @Test
    void staleVersionAndInactiveOrMissingDeckAreRejected() {
        assertThrows(DeckEditException.class, () -> service.removeCards("deck", "stale", List.of("card")));
        deck.setStatus(DeckStatus.ARCHIVED);
        assertThrows(DeckEditException.class, () -> service.getDeck("deck"));
        assertThrows(DeckEditException.class, () -> service.getDeck("missing"));
        verify(decks, never()).save(any());
    }

    @Test
    void invalidCardInputAndOverflowDoNotChangeDeck() {
        for (String link :
                List.of("https://evil.example/Products/Card", "http://www.cardmarket.com/Products/Card", "invalid"))
            assertThrows(DeckEditException.class, () -> service.addCard("deck", "old", 1, link, CardFoilType.NO));
        assertThrows(DeckEditException.class, () -> service.addCard("deck", "old", 0, card.getLink(), CardFoilType.NO));
        assertThrows(DeckEditException.class, () -> service.addCard("deck", "old", 1, card.getLink(), null));
        assertThrows(
                DeckEditException.class,
                () -> service.addCard("deck", "old", Integer.MAX_VALUE, card.getLink(), CardFoilType.NO));
        assertSame(original, deck.getCurrentVersion());
        verifyNoInteractions(cards);
    }

    @Test
    void editorReturnsCurrentCards() {
        var result = service.getDeck("deck");
        assertEquals("old", result.versionId());
        assertEquals("Deck", result.name());
        assertEquals(3, result.cards().getFirst().quantity());
        assertEquals("card", result.cards().getFirst().id());
    }

    @Test
    void repositoryFailureIsPropagated() {
        when(decks.save(deck)).thenThrow(new IllegalStateException("save failed"));
        assertThrows(IllegalStateException.class, () -> service.removeCards("deck", "old", List.of("card")));
    }
}
