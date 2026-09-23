package trd.home.tcg.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.NonNull;

public record CardmarketDeckCardPriceSummary(
        @NonNull String cardName,
        @NonNull String cardLink,
        int quantity,
        @NonNull BigDecimal latestFromInEuro,
        @NonNull BigDecimal latestTrendInEuro,
        @NonNull Instant latestPriceCreatedAt) {}
