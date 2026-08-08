package trd.home.tcg.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.constant.DeckStatus;
import trd.home.tcg.dao.CardmarketCard;

class CardmarketCardRepositoryTest {

    private final CardmarketCardRepository repository = mock(CardmarketCardRepository.class, CALLS_REAL_METHODS);

    @Test
    void mapsCardsFromActiveDecksToDtos() {
        CardmarketCard card = card("card-id");
        when(repository.findAllInCurrentDeckVersionsByStatus(DeckStatus.ACTIVE)).thenReturn(List.of(card));

        var result = repository.findAllInActiveDeckCurrentVersions();

        assertEquals(1, result.size());
        assertEquals("card-id", result.getFirst().id());
    }

    @Test
    void findsCardDtoByUuid() {
        when(repository.findById("card-id")).thenReturn(Optional.of(card("card-id")));

        var result = repository.findByUuid("card-id");

        assertTrue(result.isPresent());
        assertEquals("card-id", result.orElseThrow().id());
    }

    private static CardmarketCard card(String id) {
        CardmarketCard card = new CardmarketCard();
        card.setId(id);
        card.setLink("https://www.cardmarket.com/en/Magic/Products/Singles/Set/Card");
        card.setFoilType(CardFoilType.NO);
        return card;
    }
}
