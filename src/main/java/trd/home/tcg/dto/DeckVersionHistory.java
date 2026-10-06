package trd.home.tcg.dto;

import java.util.List;

public record DeckVersionHistory(String deckName, List<DeckVersionSummary> versions) {}
