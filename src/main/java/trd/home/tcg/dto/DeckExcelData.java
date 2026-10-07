package trd.home.tcg.dto;

import java.math.BigDecimal;
import java.util.List;
import trd.home.tcg.constant.CardFoilType;

public record DeckExcelData(String name, String version, List<Card> cards) {

    public DeckExcelData {
        cards = List.copyOf(cards);
    }

    public record Card(int quantity, String link, CardFoilType foilType, BigDecimal fromEuro, BigDecimal trendEuro) {}
}
