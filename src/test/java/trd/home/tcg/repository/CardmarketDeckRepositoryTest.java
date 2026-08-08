package trd.home.tcg.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dao.CardmarketDeckVersion;

class CardmarketDeckRepositoryTest {

    private final CardmarketDeckRepository repository = mock(CardmarketDeckRepository.class, CALLS_REAL_METHODS);

    @Test
    void mapsActiveDeckPriceSummaries() {
        var projection = mock(CardmarketDeckRepository.CardmarketDeckPriceProjection.class);
        when(projection.getDeckId()).thenReturn("deck-id");
        when(projection.getDeckName()).thenReturn("Deck");
        when(projection.getSumFromInEuro()).thenReturn(new BigDecimal("10.50"));
        when(projection.getSumTrendInEuro()).thenReturn(new BigDecimal("11.50"));
        when(repository.calculateActiveDeckPriceSummaryProjections()).thenReturn(List.of(projection));

        var result = repository.calculateActiveDeckPriceSummaries();

        assertEquals(1, result.size());
        assertEquals("deck-id", result.getFirst().deckId());
        assertEquals(new BigDecimal("10.50"), result.getFirst().sumFromInEuro());
    }

    @Test
    void calculatesHistoryTotalsAndPreservesNullPrices() {
        var projection = mock(CardmarketDeckRepository.CardmarketDeckCardPriceProjection.class);
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        when(projection.getCardName()).thenReturn("Card");
        when(projection.getQuantity()).thenReturn(2);
        when(projection.getFirstFromInEuro()).thenReturn(new BigDecimal("1.00"));
        when(projection.getFirstTrendInEuro()).thenReturn(new BigDecimal("2.00"));
        when(projection.getFirstPriceCreatedAt()).thenReturn(createdAt);
        when(projection.getLatestFromInEuro()).thenReturn(new BigDecimal("3.00"));
        when(projection.getLatestTrendInEuro()).thenReturn(new BigDecimal("4.00"));
        when(projection.getLatestPriceCreatedAt()).thenReturn(createdAt);
        when(repository.calculateDeckCardPriceSummaryProjections("deck-id")).thenReturn(List.of(projection));

        var result = repository.calculateDeckPriceHistorySummary("deck-id");

        assertEquals("deck-id", result.deckId());
        assertEquals(1, result.cards().size());
        assertEquals(new BigDecimal("2.00"), result.sumFirstFromInEuro());
        assertEquals(new BigDecimal("4.00"), result.sumFirstTrendInEuro());
        assertEquals(new BigDecimal("6.00"), result.sumLatestFromInEuro());
        assertEquals(new BigDecimal("8.00"), result.sumLatestTrendInEuro());
    }

    @Test
    void findsDeckDtoByUuid() {
        CardmarketDeck deck = new CardmarketDeck();
        deck.setId("deck-id");
        deck.setName("Deck");
        CardmarketDeckVersion version = new CardmarketDeckVersion();
        version.addCard(new CardmarketCard(), 1);
        deck.addVersion(version);
        when(repository.findEntityById("deck-id")).thenReturn(Optional.of(deck));

        var result = repository.findByUuid("deck-id");

        assertTrue(result.isPresent());
        assertEquals("deck-id", result.orElseThrow().id());
    }
}
