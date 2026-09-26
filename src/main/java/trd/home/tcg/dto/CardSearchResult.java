package trd.home.tcg.dto;

import java.math.BigDecimal;
import java.util.List;
import trd.home.tcg.constant.CardFoilType;

public record CardSearchResult(
        String id,
        String name,
        String link,
        CardFoilType foilType,
        long quantity,
        BigDecimal priceFrom,
        BigDecimal priceTrend,
        List<CardSearchDeck> decks) {}
