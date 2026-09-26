package trd.home.tcg.dto;

import java.math.BigDecimal;

public record CardSearchPrice(String cardId, BigDecimal fromInEuro, BigDecimal trendInEuro) {}
