package trd.home.tcg.dto;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dao.CardmarketDeckCard;

class CardmarketDeckDtoTest {

    @Test
    void mapsDeckAndItsCards() {
        CardmarketDeck deck = deck("deck-id", "My deck");
        CardmarketDeckCard firstDeckCard = deckCard("deck-card-1", deck, "card-id-1", 4);
        CardmarketDeckCard secondDeckCard = deckCard("deck-card-2", deck, "card-id-2", 2);
        deck.setCards(Set.of(firstDeckCard, secondDeckCard));

        CardmarketDeckDto dto = CardmarketDeckDto.from(deck);

        assertAll(
                () -> assertEquals("deck-id", dto.id()),
                () -> assertEquals("My deck", dto.name()),
                () -> assertEquals(Set.of(
                        new CardmarketDeckCardDto("deck-card-1", "deck-id", "card-id-1", 4),
                        new CardmarketDeckCardDto("deck-card-2", "deck-id", "card-id-2", 2)), dto.cards()));
    }

    @Test
    void mapsDeckWithoutCards() {
        CardmarketDeckDto dto = CardmarketDeckDto.from(deck("deck-id", "Empty deck"));

        assertAll(
                () -> assertEquals("deck-id", dto.id()),
                () -> assertEquals("Empty deck", dto.name()),
                () -> assertEquals(Set.of(), dto.cards()));
    }

    private static CardmarketDeck deck(String id, String name) {
        CardmarketDeck deck = new CardmarketDeck();
        deck.setId(id);
        deck.setName(name);
        return deck;
    }

    private static CardmarketDeckCard deckCard(String id, CardmarketDeck deck, String cardId, int quantity) {
        CardmarketCard card = new CardmarketCard();
        card.setId(cardId);

        CardmarketDeckCard deckCard = new CardmarketDeckCard();
        deckCard.setId(id);
        deckCard.setDeck(deck);
        deckCard.setCard(card);
        deckCard.setQuantity(quantity);
        return deckCard;
    }
}
