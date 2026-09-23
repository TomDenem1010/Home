package trd.home.tcg.dto;

import java.math.BigDecimal;
import lombok.NonNull;

public record CardmarketDeckPriceSummary(
        @NonNull String deckId,
        @NonNull String deckName,
        @NonNull BigDecimal sumFromInEuro,
        @NonNull BigDecimal sumTrendInEuro) {}
