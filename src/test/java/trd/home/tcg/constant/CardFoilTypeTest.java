package trd.home.tcg.constant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class CardFoilTypeTest {

    @ParameterizedTest
    @EnumSource(value = CardFoilType.class, names = "NO", mode = EnumSource.Mode.EXCLUDE)
    void returnsTrueForEveryFoilType(CardFoilType type) {
        assertTrue(CardFoilType.isFoil(type));
    }

    @ParameterizedTest
    @EnumSource(value = CardFoilType.class, names = "NO")
    void returnsFalseForNoFoilType(CardFoilType type) {
        assertFalse(CardFoilType.isFoil(type));
    }
}
