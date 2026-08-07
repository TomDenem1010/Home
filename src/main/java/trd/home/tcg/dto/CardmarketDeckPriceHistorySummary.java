package trd.home.tcg.dto;

import java.math.BigDecimal;
import java.util.List;

public record CardmarketDeckPriceHistorySummary(
        String deckId,
        List<CardmarketDeckCardPriceSummary> cards,
        BigDecimal sumFirstFromInEuro,
        BigDecimal sumFirstTrendInEuro,
        BigDecimal sumLatestFromInEuro,
        BigDecimal sumLatestTrendInEuro) {}
