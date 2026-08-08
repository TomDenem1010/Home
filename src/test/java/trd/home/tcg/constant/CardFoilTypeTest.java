package trd.home.tcg.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest
    @EnumSource(CardFoilType.class)
    void returnsFoilTypeForEveryEnumName(CardFoilType type) {
        assertEquals(type, CardFoilType.fromString(type.name()));
    }

    @Test
    void findsFoilTypeIgnoringCase() {
        assertEquals(CardFoilType.ETCHED_FOIL, CardFoilType.fromString("etched_foil"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"UNKNOWN", "FOIL "})
    void returnsNullForUnknownValue(String value) {
        assertNull(CardFoilType.fromString(value));
    }
}
