package trd.home.tcg.dto;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import trd.home.common.logging.LogMethodCall;
import trd.home.tcg.dao.CardmarketDeck;

public record CardmarketDeckDto(
        @NonNull String id, @NonNull String name, @NonNull Set<CardmarketDeckCardDto> cards) {

    @LogMethodCall
    public static CardmarketDeckDto from(CardmarketDeck deck) {
        Set<CardmarketDeckCardDto> cards = deck.getCurrentVersion().getCards().stream()
                .map(deckCard -> new CardmarketDeckCardDto(
                        Objects.requireNonNullElse(deckCard.getId(), ""),
                        Objects.requireNonNullElse(deck.getId(), ""),
                        Objects.requireNonNullElse(deckCard.getCard().getId(), ""),
                        deckCard.getQuantity()))
                .collect(Collectors.toSet());

        return new CardmarketDeckDto(
                Objects.requireNonNullElse(deck.getId(), ""), Objects.requireNonNullElse(deck.getName(), ""), cards);
    }
}
