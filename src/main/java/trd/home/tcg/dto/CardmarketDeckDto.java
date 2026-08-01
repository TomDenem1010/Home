package trd.home.tcg.dto;

import java.util.Set;
import java.util.stream.Collectors;
import trd.home.tcg.dao.CardmarketDeck;

public record CardmarketDeckDto(String id, String name, Set<CardmarketDeckCardDto> cards) {

    public static CardmarketDeckDto from(CardmarketDeck deck) {
        Set<CardmarketDeckCardDto> cards = deck.getCurrentVersion().getCards().stream()
                .map(deckCard -> new CardmarketDeckCardDto(
                        deckCard.getId(), deck.getId(), deckCard.getCard().getId(), deckCard.getQuantity()))
                .collect(Collectors.toSet());

        return new CardmarketDeckDto(deck.getId(), deck.getName(), cards);
    }
}
