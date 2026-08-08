package trd.home.tcg.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CardmarketDeckCardPriceSummary(
        String cardName,
        int quantity,
        BigDecimal latestFromInEuro,
        BigDecimal latestTrendInEuro,
        Instant latestPriceCreatedAt) {}
