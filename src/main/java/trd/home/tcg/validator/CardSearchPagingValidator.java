package trd.home.tcg.validator;

import trd.home.tcg.dto.CardSearchFilter;
import trd.home.tcg.exception.InvalidCardSearchFilterException;

public class CardSearchPagingValidator extends CardSearchFilterValidator {
    @Override
    public void validate(CardSearchFilter filter, int page, int size) {
        if (page < 0 || size < 1 || size > 200) {
            throw new InvalidCardSearchFilterException(
                    "Page must not be negative and page size must be between 1 and 200.");
        }
    }
}
