package trd.home.tcg.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CardGameTypeTest {

    @ParameterizedTest
    @EnumSource(CardGameType.class)
    void findsEveryKnownCardGameType(CardGameType cardGameType) {
        assertEquals(cardGameType, CardGameType.findByCardmarketUrlPart(cardGameType.getCardmarketUrlPart()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "Pokemon")
    void returnsNullForUnknownCardGameType(String urlPart) {
        assertNull(CardGameType.findByCardmarketUrlPart(urlPart));
    }
}
