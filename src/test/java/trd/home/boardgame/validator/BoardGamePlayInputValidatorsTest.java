package trd.home.boardgame.validator;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import trd.home.boardgame.dto.BoardGamePlayInput;
import trd.home.boardgame.exception.InvalidBoardGameException;

class BoardGamePlayInputValidatorsTest {
    private final LocalDateTime time = LocalDateTime.of(2026, 11, 10, 18, 30);

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void requiresGameId(String id) {
        assertThrows(
                InvalidBoardGameException.class,
                () -> new BoardGamePlayGameValidator().validate(new BoardGamePlayInput(id, time, 3)));
    }

    @Test
    void validatesGameIdLength() {
        var validator = new BoardGamePlayGameValidator();
        assertDoesNotThrow(() -> validator.validate(new BoardGamePlayInput("a".repeat(255), time, 3)));
        assertThrows(
                InvalidBoardGameException.class,
                () -> validator.validate(new BoardGamePlayInput("a".repeat(256), time, 3)));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {-1, 0, 6, Integer.MAX_VALUE})
    void rejectsRatingOutsideOneToFive(Integer rating) {
        assertThrows(
                InvalidBoardGameException.class,
                () -> new BoardGamePlayRatingValidator().validate(new BoardGamePlayInput("game", time, rating)));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    void acceptsEveryValidRating(int rating) {
        assertDoesNotThrow(
                () -> new BoardGamePlayRatingValidator().validate(new BoardGamePlayInput("game", time, rating)));
    }

    @Test
    void requiresTimeWithinDatabaseSupportedYears() {
        var validator = new BoardGamePlayTimeValidator();
        assertThrows(
                InvalidBoardGameException.class, () -> validator.validate(new BoardGamePlayInput("game", null, 3)));
        assertThrows(
                InvalidBoardGameException.class,
                () -> validator.validate(new BoardGamePlayInput("game", time.withYear(0), 3)));
        assertThrows(
                InvalidBoardGameException.class,
                () -> validator.validate(new BoardGamePlayInput("game", time.withYear(10000), 3)));
        assertDoesNotThrow(() -> validator.validate(new BoardGamePlayInput("game", time.withYear(1), 3)));
        assertDoesNotThrow(() -> validator.validate(new BoardGamePlayInput("game", time.withYear(9999), 3)));
    }
}
