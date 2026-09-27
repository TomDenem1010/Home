package trd.home.auth.validator;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import trd.home.auth.dto.ApplicationLogSearchFilter;
import trd.home.auth.exception.InvalidApplicationLogSearchException;

class ApplicationLogSearchFilterValidatorTest {
    private final ApplicationLogSearchPagingValidator paging = new ApplicationLogSearchPagingValidator();
    private final ApplicationLogSearchTimeValidator time = new ApplicationLogSearchTimeValidator();
    private final ApplicationLogSearchFilter empty = new ApplicationLogSearchFilter(null, null, null, null, null);

    @Test
    void acceptsPagingBoundariesAndRejectsInvalidValues() {
        assertDoesNotThrow(() -> paging.validate(empty, 0, 1));
        assertDoesNotThrow(() -> paging.validate(empty, 5, 200));
        assertThrows(InvalidApplicationLogSearchException.class, () -> paging.validate(empty, -1, 50));
        assertThrows(InvalidApplicationLogSearchException.class, () -> paging.validate(empty, 0, 0));
        assertThrows(InvalidApplicationLogSearchException.class, () -> paging.validate(empty, 0, 201));
    }

    @Test
    void acceptsOptionalAndEqualTimeBoundsButRejectsReversedRange() {
        var start = LocalDateTime.parse("2026-09-27T10:00:00");
        var end = start.plusHours(1);
        assertDoesNotThrow(() -> time.validate(empty, 0, 50));
        assertDoesNotThrow(() -> time.validate(filter(start, null), 0, 50));
        assertDoesNotThrow(() -> time.validate(filter(null, end), 0, 50));
        assertDoesNotThrow(() -> time.validate(filter(start, start), 0, 50));
        assertDoesNotThrow(() -> time.validate(filter(start, end), 0, 50));
        assertThrows(InvalidApplicationLogSearchException.class, () -> time.validate(filter(end, start), 0, 50));
    }

    private static ApplicationLogSearchFilter filter(LocalDateTime start, LocalDateTime end) {
        return new ApplicationLogSearchFilter(null, null, start, end, null);
    }
}
