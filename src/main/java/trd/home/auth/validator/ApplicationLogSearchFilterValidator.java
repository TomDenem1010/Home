package trd.home.auth.validator;

import trd.home.auth.dto.ApplicationLogSearchFilter;

public abstract class ApplicationLogSearchFilterValidator {
    public abstract void validate(ApplicationLogSearchFilter filter, int page, int size);
}
