package trd.home.tcg.service.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dao.CardmarketDeckVersion;
import trd.home.tcg.repository.CardmarketCardPriceRepository;
import trd.home.tcg.repository.CardmarketCardRepository;
import trd.home.tcg.repository.CardmarketDeckCardRepository;
import trd.home.tcg.repository.CardmarketDeckRepository;

class DeckVersionQueryTest {
    private final CardmarketDeckRepository decks = mock(CardmarketDeckRepository.class);
    private final CardmarketDeckCardRepository cards = mock(CardmarketDeckCardRepository.class);
    private final TcgQueryService queries = new TcgQueryService(
            decks, cards, mock(CardmarketCardRepository.class), mock(CardmarketCardPriceRepository.class));

    @Test
    void comparesNumericVersionsAndTracksQuantitiesAndFoilChanges() {
        var deck = new CardmarketDeck();
        deck.setId("deck");
        deck.setName("Test deck");
        var normal = card("normal", "Alpha", CardFoilType.NO);
        var foil = card("foil", "Alpha", CardFoilType.values()[1]);
        var removed = card("removed", "Beta", CardFoilType.NO);
        var v1 = version(deck, "v1");
        v1.addCard(normal, 1);
        v1.addCard(normal, 1);
        v1.addCard(removed, 1);
        var v10 = version(deck, "v10");
        v10.addCard(normal, 1);
        v10.addCard(foil, 1);
        var v2 = version(deck, "v2");
        v2.addCard(normal, 4);
        var v11 = version(deck, "v11");
        v11.addCard(normal, 1);
        v11.addCard(foil, 1);
        when(decks.findById("deck")).thenReturn(Optional.of(deck));
        when(cards.findAllByDeckVersionIdIn(List.of("v1", "v2", "v10", "v11")))
                .thenReturn(deck.getVersions().stream()
                        .flatMap(v -> v.getCards().stream())
                        .toList());

        var history = queries.getDeckVersionHistory("deck");
        assertEquals("Test deck", history.deckName());
        assertEquals(
                List.of("v1", "v2", "v10", "v11"),
                history.versions().stream().map(v -> v.version()).toList());
        assertEquals(2, history.versions().get(0).added().size());
        assertTrue(history.versions().get(0).removed().isEmpty());
        assertEquals(2, history.versions().get(1).added().getFirst().quantity());
        assertEquals("Beta", history.versions().get(1).removed().getFirst().name());
        assertEquals(3, history.versions().get(2).removed().getFirst().quantity());
        assertEquals(
                foil.getFoilType(), history.versions().get(2).added().getFirst().foilType());
        assertTrue(history.versions().get(3).added().isEmpty());
        assertTrue(history.versions().get(3).removed().isEmpty());
    }

    @Test
    void missingDeckAndDeckWithoutVersionsHaveEmptyHistory() {
        assertTrue(queries.getDeckVersionHistory("missing").versions().isEmpty());
        var deck = new CardmarketDeck();
        deck.setName("Empty");
        when(decks.findById("empty")).thenReturn(Optional.of(deck));
        assertTrue(queries.getDeckVersionHistory("empty").versions().isEmpty());
    }

    private static CardmarketCard card(String id, String name, CardFoilType foilType) {
        var card = new CardmarketCard();
        card.setId(id);
        card.setLink("https://www.cardmarket.com/en/Magic/Products/Singles/Test/" + name);
        card.setFoilType(foilType);
        return card;
    }

    private static CardmarketDeckVersion version(CardmarketDeck deck, String name) {
        var version = new CardmarketDeckVersion();
        version.setId(name);
        version.setVersion(name);
        deck.addVersion(version);
        return version;
    }
}
