package trd.home.auth.validator;

import trd.home.auth.dto.ApplicationLogSearchFilter;
import trd.home.auth.exception.InvalidApplicationLogSearchException;

public class ApplicationLogSearchPagingValidator extends ApplicationLogSearchFilterValidator {
    @Override
    public void validate(ApplicationLogSearchFilter filter, int page, int size) {
        if (page < 0 || size < 1 || size > 200) {
            throw new InvalidApplicationLogSearchException(
                    "Page must not be negative and page size must be between 1 and 200.");
        }
    }
}
