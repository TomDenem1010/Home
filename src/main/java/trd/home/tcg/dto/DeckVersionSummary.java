package trd.home.tcg.dto;

import java.util.List;
import trd.home.tcg.constant.CardFoilType;

public record DeckVersionSummary(String version, List<CardChange> added, List<CardChange> removed) {
    public record CardChange(String name, String link, CardFoilType foilType, int quantity) {}
}
