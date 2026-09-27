package trd.home.tcg.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import trd.home.tcg.constant.CardPriceType;
import trd.home.tcg.dto.CardSearchFilter;
import trd.home.tcg.exception.InvalidCardSearchFilterException;

class CardSearchFilterValidatorTest {
    private final CardSearchPriceValidator prices = new CardSearchPriceValidator();

    @Test
    void validatesPagingBoundaries() {
        var paging = new CardSearchPagingValidator();
        var filter = filter(null, null, null);
        assertDoesNotThrow(() -> paging.validate(filter, 0, 1));
        assertDoesNotThrow(() -> paging.validate(filter, Integer.MAX_VALUE, 200));
        assertThrows(InvalidCardSearchFilterException.class, () -> paging.validate(filter, -1, 50));
        assertThrows(InvalidCardSearchFilterException.class, () -> paging.validate(filter, 0, 0));
        assertThrows(InvalidCardSearchFilterException.class, () -> paging.validate(filter, 0, 201));
    }

    @Test
    void rejectsNegativePricesReversedRangesAndMissingPriceType() {
        for (CardSearchFilter filter : List.of(
                filter("-1", null, CardPriceType.FROM),
                filter(null, "-1", CardPriceType.TREND),
                filter("10", "5", CardPriceType.FROM),
                filter("0", null, null),
                filter(null, "5", null))) {
            assertThrows(InvalidCardSearchFilterException.class, () -> prices.validate(filter, 0, 50));
        }
    }

    @Test
    void acceptsOptionalBoundsZeroAndEqualPrices() {
        for (CardSearchFilter filter : List.of(
                filter(null, null, null),
                filter("0", null, CardPriceType.FROM),
                filter(null, "0", CardPriceType.FROM),
                filter(null, "5", CardPriceType.TREND),
                filter("5.0", "5.00", CardPriceType.FROM))) {
            assertDoesNotThrow(() -> prices.validate(filter, 0, 50));
        }
    }

    private static CardSearchFilter filter(String min, String max, CardPriceType type) {
        return new CardSearchFilter(
                null,
                null,
                null,
                min == null ? null : new BigDecimal(min),
                max == null ? null : new BigDecimal(max),
                type);
    }
}
