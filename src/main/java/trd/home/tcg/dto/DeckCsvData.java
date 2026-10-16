package trd.home.tcg.dto;

import java.util.List;
import trd.home.tcg.constant.CardFoilType;

public record DeckCsvData(String name, String version, List<Card> cards) {
    public DeckCsvData {
        cards = List.copyOf(cards);
    }

    public record Card(String name, int quantity, String link, CardFoilType foilType) {}
}
