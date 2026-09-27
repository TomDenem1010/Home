package trd.home.tcg.validator;

import trd.home.tcg.dto.CardSearchFilter;
import trd.home.tcg.exception.InvalidCardSearchFilterException;

public class CardSearchPriceValidator extends CardSearchFilterValidator {
    @Override
    public void validate(CardSearchFilter filter) {
        if (filter.priceMin() != null && filter.priceMin().signum() < 0) {
            throw new InvalidCardSearchFilterException("Minimum price must not be negative.");
        }
        if (filter.priceMax() != null && filter.priceMax().signum() < 0) {
            throw new InvalidCardSearchFilterException("Maximum price must not be negative.");
        }
        if (filter.priceMin() != null
                && filter.priceMax() != null
                && filter.priceMin().compareTo(filter.priceMax()) > 0) {
            throw new InvalidCardSearchFilterException("Minimum price must not exceed maximum price.");
        }
        if ((filter.priceMin() != null || filter.priceMax() != null) && filter.priceType() == null) {
            throw new InvalidCardSearchFilterException("Price type is required when filtering by price.");
        }
    }
}
