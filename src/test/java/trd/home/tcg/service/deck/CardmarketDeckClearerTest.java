package trd.home.tcg.service.deck;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dao.CardmarketDeckVersion;
import trd.home.tcg.repository.CardmarketDeckCardRepository;
import trd.home.tcg.repository.CardmarketDeckRepository;
import trd.home.tcg.repository.CardmarketDeckVersionRepository;

class CardmarketDeckClearerTest {
    private final CardmarketDeckRepository decks = mock(CardmarketDeckRepository.class);
    private final CardmarketDeckVersionRepository versions = mock(CardmarketDeckVersionRepository.class);
    private final CardmarketDeckCardRepository deckCards = mock(CardmarketDeckCardRepository.class);
    private final CardmarketDeckClearer clearer = new CardmarketDeckClearer(decks, versions, deckCards);

    @Test
    void clearsDeckEntitiesInForeignKeyOrder() {
        var deck = new CardmarketDeck();
        deck.addVersion(new CardmarketDeckVersion());
        var deckList = List.of(deck, new CardmarketDeck());
        when(decks.findAll()).thenReturn(deckList);
        doAnswer(invocation -> {
                    deckList.forEach(value -> assertNull(value.getCurrentVersion()));
                    return deckList;
                })
                .when(decks)
                .saveAllAndFlush(deckList);
        clearer.clear();
        var order = inOrder(decks, versions, deckCards);
        order.verify(decks).findAll();
        order.verify(decks).saveAllAndFlush(deckList);
        order.verify(deckCards).deleteAllInBatch();
        order.verify(versions).deleteAllInBatch();
        order.verify(decks).deleteAllInBatch();
        verifyNoMoreInteractions(decks, versions, deckCards);
    }

    @Test
    void propagatesFailureAndStopsDeletingParentEntities() {
        doThrow(new IllegalStateException("delete failed")).when(deckCards).deleteAllInBatch();
        assertThrows(IllegalStateException.class, clearer::clear);
        verify(decks).saveAllAndFlush(List.of());
        verify(decks, never()).deleteAllInBatch();
        verifyNoInteractions(versions);
    }

    @Test
    void doesNotDeleteAnythingIfClearingReferencesFails() {
        doThrow(new IllegalStateException("flush failed")).when(decks).saveAllAndFlush(List.of());
        assertThrows(IllegalStateException.class, clearer::clear);
        verifyNoInteractions(versions, deckCards);
        verify(decks, never()).deleteAllInBatch();
    }
}
