package trd.home.tcg.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dao.CardmarketDeckCard;
import trd.home.tcg.service.file.DeckFileReader;

class DeckFileReaderTest {

    @Test
    void readsDecksFromResources() {
        List<CardmarketDeck> decks = new DeckFileReader().read();

        CardmarketDeck deck = decks.getFirst();
        CardmarketDeckCard firstCard =
                deck.getCurrentVersion().getCards().iterator().next();
        assertAll(
                () -> assertEquals(1, decks.size()),
                () -> assertEquals("Kilo Apogee Mind", deck.getName()),
                () -> assertEquals("v1", deck.getCurrentVersion().getVersion()),
                () -> assertEquals(99, deck.getCurrentVersion().getCards().size()),
                () -> assertEquals(1, firstCard.getQuantity()),
                () -> assertEquals(
                        "https://www.cardmarket.com/en/Magic/Products/Singles/Commander-Edge-of-Eternities/Kilo-Apogee-Mind?language=1",
                        firstCard.getCard().getLink()),
                () -> assertEquals(CardFoilType.FOIL, firstCard.getCard().getFoilType()));
    }
}
