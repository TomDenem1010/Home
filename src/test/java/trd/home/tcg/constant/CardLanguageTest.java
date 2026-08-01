package trd.home.tcg.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class CardLanguageTest {

    @ParameterizedTest
    @EnumSource(CardLanguage.class)
    void findsEveryKnownCardLanguage(CardLanguage cardLanguage) {
        assertEquals(cardLanguage, CardLanguage.findByCardmarketUrlPart(cardLanguage.getCardmarketUrlPart()));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, 2})
    void returnsNullForUnknownCardLanguage(Integer urlPart) {
        assertNull(CardLanguage.findByCardmarketUrlPart(urlPart));
    }
}
