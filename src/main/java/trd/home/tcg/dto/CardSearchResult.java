package trd.home.tcg.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.NonNull;
import trd.home.tcg.constant.CardFoilType;

public record CardSearchResult(
        @NonNull String id,
        @NonNull String name,
        @NonNull String link,
        @NonNull CardFoilType foilType,
        @NonNull Long quantity,
        BigDecimal priceFrom,
        BigDecimal priceTrend,
        @NonNull List<CardSearchDeck> decks) {}
