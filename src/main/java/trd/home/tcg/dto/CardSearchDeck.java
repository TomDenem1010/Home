package trd.home.tcg.dto;

import lombok.NonNull;

public record CardSearchDeck(
        @NonNull String id, @NonNull String name, @NonNull Integer quantity) {}
