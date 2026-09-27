package trd.home.auth.validator;

import trd.home.auth.dto.ApplicationLogSearchFilter;
import trd.home.auth.exception.InvalidApplicationLogSearchException;

public class ApplicationLogSearchTimeValidator extends ApplicationLogSearchFilterValidator {
    @Override
    public void validate(ApplicationLogSearchFilter filter, int page, int size) {
        if (filter.timeStart() != null
                && filter.timeEnd() != null
                && filter.timeStart().isAfter(filter.timeEnd())) {
            throw new InvalidApplicationLogSearchException("TimeStart must not be after TimeEnd.");
        }
    }
}
