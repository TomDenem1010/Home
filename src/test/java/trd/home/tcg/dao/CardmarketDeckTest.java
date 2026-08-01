package trd.home.tcg.dao;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import trd.home.tcg.constant.DeckStatus;

class CardmarketDeckTest {

    @Test
    void addingAVersionSetsBothSidesOfTheRelationshipAndMakesItCurrent() {
        CardmarketDeck deck = new CardmarketDeck();
        CardmarketDeckVersion version = new CardmarketDeckVersion();
        version.setVersion("v1");

        deck.addVersion(version);

        assertAll(
                () -> assertSame(deck, version.getDeck()),
                () -> assertSame(version, deck.getCurrentVersion()),
                () -> assertEquals(1, deck.getVersions().size()));
    }

    @Test
    void deleteMarksDeckAsDeletedWithoutRemovingVersions() {
        CardmarketDeck deck = new CardmarketDeck();
        CardmarketDeckVersion version = new CardmarketDeckVersion();
        deck.addVersion(version);
        Instant deletedAt = Instant.parse("2026-08-01T12:00:00Z");

        deck.delete(deletedAt);

        assertAll(
                () -> assertEquals(DeckStatus.DELETED, deck.getStatus()),
                () -> assertEquals(deletedAt, deck.getDeletedAt()),
                () -> assertSame(version, deck.getCurrentVersion()),
                () -> assertEquals(1, deck.getVersions().size()));
    }
}
