package trd.home.tcg.dao;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import trd.home.tcg.constant.CardFoilType;

class CardmarketCardTest {

    @Test
    void storesTheCardmarketValues() {
        CardmarketCard card = new CardmarketCard();
        card.setId("card-1");
        card.setLink("https://www.cardmarket.com/en/Magic/Products/Singles/Test-Card");
        card.setFoilType(CardFoilType.ETCHED_FOIL);

        assertAll(
                () -> assertEquals("card-1", card.getId()),
                () -> assertEquals("https://www.cardmarket.com/en/Magic/Products/Singles/Test-Card", card.getLink()),
                () -> assertEquals(CardFoilType.ETCHED_FOIL, card.getFoilType()));
    }
}
