package trd.home.tcg.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CardmarketDeckCardPriceSummary(
        String cardName,
        int quantity,
        BigDecimal firstFromInEuro,
        BigDecimal firstTrendInEuro,
        Instant firstPriceCreatedAt,
        BigDecimal latestFromInEuro,
        BigDecimal latestTrendInEuro,
        Instant latestPriceCreatedAt) {}
