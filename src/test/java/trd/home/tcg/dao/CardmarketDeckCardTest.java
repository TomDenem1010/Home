package trd.home.tcg.dao;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class CardmarketDeckCardTest {

    @Test
    void linksACardToItsDeckVersionWithQuantity() {
        CardmarketCard card = new CardmarketCard();
        card.setId("card-1");
        CardmarketDeckVersion version = new CardmarketDeckVersion();
        version.setId("version-1");

        CardmarketDeckCard deckCard = new CardmarketDeckCard();
        deckCard.setId("deck-card-1");
        deckCard.setCard(card);
        deckCard.setDeckVersion(version);
        deckCard.setQuantity(4);

        assertAll(
                () -> assertEquals("deck-card-1", deckCard.getId()),
                () -> assertSame(card, deckCard.getCard()),
                () -> assertSame(version, deckCard.getDeckVersion()),
                () -> assertEquals(4, deckCard.getQuantity()));
    }
}
