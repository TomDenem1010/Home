package trd.home.tcg.dto;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CardmarketSummaryDtoTest {

    @Test
    void exposesEveryDeckCardPriceSummaryValue() {
        Instant latestPriceCreatedAt = Instant.parse("2026-08-30T12:00:00Z");
        CardmarketDeckCardPriceSummary summary = new CardmarketDeckCardPriceSummary(
                "Black Lotus",
                "https://example.test/black-lotus",
                2,
                new BigDecimal("10.25"),
                new BigDecimal("11.50"),
                latestPriceCreatedAt);

        assertAll(
                () -> assertEquals("Black Lotus", summary.cardName()),
                () -> assertEquals("https://example.test/black-lotus", summary.cardLink()),
                () -> assertEquals(2, summary.quantity()),
                () -> assertEquals(new BigDecimal("10.25"), summary.latestFromInEuro()),
                () -> assertEquals(new BigDecimal("11.50"), summary.latestTrendInEuro()),
                () -> assertEquals(latestPriceCreatedAt, summary.latestPriceCreatedAt()));
    }

    @Test
    void exposesEveryDeckPriceSummaryValue() {
        CardmarketDeckPriceSummary summary =
                new CardmarketDeckPriceSummary("deck-id", "My deck", new BigDecimal("20.75"), new BigDecimal("23.00"));

        assertAll(
                () -> assertEquals("deck-id", summary.deckId()),
                () -> assertEquals("My deck", summary.deckName()),
                () -> assertEquals(new BigDecimal("20.75"), summary.sumFromInEuro()),
                () -> assertEquals(new BigDecimal("23.00"), summary.sumTrendInEuro()));
    }

    @Test
    void exposesEveryDeckCardValue() {
        CardmarketDeckCardDto dto = new CardmarketDeckCardDto("id", "deck-id", "card-id", 3);

        assertAll(
                () -> assertEquals("id", dto.id()),
                () -> assertEquals("deck-id", dto.deckId()),
                () -> assertEquals("card-id", dto.cardId()),
                () -> assertEquals(3, dto.quantity()));
    }
}
