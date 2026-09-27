package trd.home.tcg.dto;

import java.math.BigDecimal;
import lombok.NonNull;

public record CardSearchPrice(@NonNull String cardId, BigDecimal fromInEuro, BigDecimal trendInEuro) {}
