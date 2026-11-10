package trd.home.boardgame.validator;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import trd.home.boardgame.dto.BoardGamePlaySearchFilter;
import trd.home.boardgame.dto.BoardGameSearchFilter;
import trd.home.boardgame.exception.InvalidBoardGameException;

class BoardGameSearchValidatorsTest {
    private final BoardGameSearchFilterValidator games = new BoardGameSearchFilterValidator();
    private final BoardGamePlaySearchFilterValidator plays = new BoardGamePlaySearchFilterValidator();

    @Test
    void acceptsEmptyAndBoundaryGameFilters() {
        assertDoesNotThrow(() -> games.validate(new BoardGameSearchFilter(null, null, null), 0));
        assertDoesNotThrow(() -> games.validate(new BoardGameSearchFilter("a".repeat(255), null, null), 1));
    }

    @Test
    void rejectsInvalidGamePagingAndName() {
        assertThrows(
                InvalidBoardGameException.class, () -> games.validate(new BoardGameSearchFilter(null, null, null), -1));
        assertThrows(
                InvalidBoardGameException.class,
                () -> games.validate(new BoardGameSearchFilter("a".repeat(256), null, null), 0));
    }

    @Test
    void acceptsUnboundedOneSidedAndInclusivePlayRanges() {
        var time = LocalDateTime.of(2026, 11, 10, 12, 0);
        assertDoesNotThrow(() -> plays.validate(new BoardGamePlaySearchFilter(null, null, null, null, null), 0));
        assertDoesNotThrow(() -> plays.validate(new BoardGamePlaySearchFilter("a".repeat(255), time, time, 1, 5), 0));
        assertDoesNotThrow(() -> plays.validate(new BoardGamePlaySearchFilter(null, time, null, 3, null), 0));
        assertDoesNotThrow(() -> plays.validate(new BoardGamePlaySearchFilter(null, null, time, null, 3), 0));
        assertDoesNotThrow(() -> plays.validate(new BoardGamePlaySearchFilter(null, null, null, 3, 3), 0));
    }

    @Test
    void rejectsReversedAndUnsupportedDates() {
        var time = LocalDateTime.of(2026, 11, 10, 12, 0);
        assertThrows(
                InvalidBoardGameException.class,
                () -> plays.validate(new BoardGamePlaySearchFilter(null, time, time.minusMinutes(1), null, null), 0));
        assertThrows(
                InvalidBoardGameException.class,
                () -> plays.validate(new BoardGamePlaySearchFilter(null, time.withYear(0), null, null, null), 0));
        assertThrows(
                InvalidBoardGameException.class,
                () -> plays.validate(new BoardGamePlaySearchFilter(null, null, time.withYear(10000), null, null), 0));
    }

    @Test
    void rejectsInvalidPlayRatingRangesPagingAndName() {
        assertThrows(
                InvalidBoardGameException.class,
                () -> plays.validate(new BoardGamePlaySearchFilter(null, null, null, 0, null), 0));
        assertThrows(
                InvalidBoardGameException.class,
                () -> plays.validate(new BoardGamePlaySearchFilter(null, null, null, null, 6), 0));
        assertThrows(
                InvalidBoardGameException.class,
                () -> plays.validate(new BoardGamePlaySearchFilter(null, null, null, 5, 4), 0));
        assertThrows(
                InvalidBoardGameException.class,
                () -> plays.validate(new BoardGamePlaySearchFilter(null, null, null, null, null), -1));
        assertThrows(
                InvalidBoardGameException.class,
                () -> plays.validate(new BoardGamePlaySearchFilter("a".repeat(256), null, null, null, null), 0));
    }
}
