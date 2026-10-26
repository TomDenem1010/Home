package trd.home.tcg.dto;

import java.util.List;
import trd.home.tcg.constant.CardFoilType;

public record DeckEditor(String id, String name, String versionId, String version, List<Card> cards) {
    public record Card(String id, String link, CardFoilType foilType, int quantity) {}
}
