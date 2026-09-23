package trd.home.tcg.dto;

import lombok.NonNull;

public record CardmarketDeckCardDto(
        @NonNull String id, @NonNull String deckId, @NonNull String cardId, int quantity) {}
