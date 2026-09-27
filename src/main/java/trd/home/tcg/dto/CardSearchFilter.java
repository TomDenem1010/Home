package trd.home.tcg.dto;

import java.math.BigDecimal;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.constant.CardGameType;
import trd.home.tcg.constant.CardPriceType;

public record CardSearchFilter(
        CardFoilType foilType,
        String name,
        CardGameType cardGameType,
        BigDecimal priceMin,
        BigDecimal priceMax,
        CardPriceType priceType) {}
