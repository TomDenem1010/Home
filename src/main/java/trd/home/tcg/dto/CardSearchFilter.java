package trd.home.tcg.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.constant.CardGameType;
import trd.home.tcg.constant.CardPriceType;

@Getter
@Setter
public class CardSearchFilter {
    private CardFoilType foilType;
    private String name;
    private CardGameType cardGameType;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private CardPriceType priceType = CardPriceType.FROM;
}
