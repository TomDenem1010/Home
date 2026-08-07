package trd.home.tcg.dto;

import java.math.BigDecimal;

public record CardmarketDeckPriceSummary(String deckName, BigDecimal sumFromInEuro, BigDecimal sumTrendInEuro) {}
