package trd.home.tcg.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.NonNull;

public record CardmarketDeckPriceHistorySummary(
        @NonNull String deckId,
        @NonNull List<CardmarketDeckCardPriceSummary> cards,
        @NonNull BigDecimal sumLatestFromInEuro,
        @NonNull BigDecimal sumLatestTrendInEuro) {}
