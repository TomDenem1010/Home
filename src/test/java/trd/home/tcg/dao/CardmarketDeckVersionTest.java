package trd.home.tcg.dao;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import trd.home.tcg.constant.DeckStatus;

class CardmarketDeckVersionTest {

    @Test
    void retainsEarlierCardListWhenANewVersionIsAdded() {
        CardmarketDeck deck = new CardmarketDeck();
        CardmarketCard removedCard = card("removed-card");
        CardmarketCard addedCard = card("added-card");

        CardmarketDeckVersion firstVersion = new CardmarketDeckVersion();
        firstVersion.setVersion("v1");
        firstVersion.addCard(removedCard, 1);
        deck.addVersion(firstVersion);

        CardmarketDeckVersion secondVersion = new CardmarketDeckVersion();
        secondVersion.setVersion("v2");
        secondVersion.addCard(addedCard, 2);
        deck.addVersion(secondVersion);

        assertAll(
                () -> assertSame(secondVersion, deck.getCurrentVersion()),
                () -> assertEquals(2, deck.getVersions().size()),
                () -> assertEquals(
                        removedCard, firstVersion.getCards().iterator().next().getCard()),
                () -> assertEquals(
                        addedCard, secondVersion.getCards().iterator().next().getCard()));
    }

    @Test
    void softDeletePreservesTheDeckAndVersions() {
        CardmarketDeck deck = new CardmarketDeck();
        CardmarketDeckVersion version = new CardmarketDeckVersion();
        version.setVersion("v1");
        deck.addVersion(version);
        Instant deletedAt = Instant.parse("2026-08-01T12:00:00Z");

        deck.delete(deletedAt);

        assertAll(
                () -> assertEquals(DeckStatus.DELETED, deck.getStatus()),
                () -> assertEquals(deletedAt, deck.getDeletedAt()),
                () -> assertSame(version, deck.getCurrentVersion()));
    }

    @Test
    void addsCardWithBackReferenceAndQuantity() {
        CardmarketDeckVersion version = new CardmarketDeckVersion();
        version.setId("version-id");
        CardmarketCard card = card("card-1");

        version.addCard(card, 3);

        CardmarketDeckCard deckCard = version.getCards().iterator().next();
        assertAll(
                () -> assertEquals(1, version.getCards().size()),
                () -> assertEquals("version-id", version.getId()),
                () -> assertSame(version, deckCard.getDeckVersion()),
                () -> assertSame(card, deckCard.getCard()),
                () -> assertEquals(3, deckCard.getQuantity()));
    }

    @Test
    void rejectsNonPositiveCardQuantities() {
        CardmarketDeckVersion version = new CardmarketDeckVersion();
        CardmarketCard card = card("card-1");

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> version.addCard(card, 0)),
                () -> assertThrows(IllegalArgumentException.class, () -> version.addCard(card, -1)),
                () -> assertEquals(0, version.getCards().size()));
    }

    private static CardmarketCard card(String id) {
        CardmarketCard card = new CardmarketCard();
        card.setId(id);
        return card;
    }
}
