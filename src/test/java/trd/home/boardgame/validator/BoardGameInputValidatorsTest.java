package trd.home.boardgame.validator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import trd.home.boardgame.constant.BoardGameState;
import trd.home.boardgame.constant.BoardGameType;
import trd.home.boardgame.dto.BoardGameInput;
import trd.home.boardgame.exception.InvalidBoardGameException;

class BoardGameInputValidatorsTest {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void rejectsMissingName(String name) {
        assertThrows(InvalidBoardGameException.class, () -> new BoardGameNameValidator().validate(input(name, null)));
    }

    @Test
    void enforcesNameLengthBoundary() {
        var validator = new BoardGameNameValidator();
        assertDoesNotThrow(() -> validator.validate(input("a".repeat(255), null)));
        assertThrows(InvalidBoardGameException.class, () -> validator.validate(input("a".repeat(256), null)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
            strings = {
                " ",
                "https://boardgamegeek.com/boardgame/123",
                "https://www.boardgamegeek.com/boardgame/123/a-game",
                "https://boardgamegeek.com/boardgameexpansion/123/expansion/"
            })
    void acceptsOptionalOrValidBggLink(String link) {
        assertDoesNotThrow(() -> new BoardGameLinkValidator().validate(input("Game", link)));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "http://boardgamegeek.com/boardgame/1",
                "https://example.com/boardgame/1",
                "https://boardgamegeek.com.evil.test/boardgame/1",
                "https://user@boardgamegeek.com/boardgame/1",
                "https://boardgamegeek.com:443/boardgame/1",
                "javascript:alert(1)",
                "https://boardgamegeek.com/boardgame/0",
                "https://boardgamegeek.com/boardgame/abc",
                "https://boardgamegeek.com/collection/user",
                "not a url"
            })
    void rejectsUnsafeOrNonGameLinks(String link) {
        assertThrows(InvalidBoardGameException.class, () -> new BoardGameLinkValidator().validate(input("Game", link)));
    }

    @Test
    void rejectsOversizedBggLink() {
        assertThrows(
                InvalidBoardGameException.class,
                () -> new BoardGameLinkValidator()
                        .validate(input("Game", "https://boardgamegeek.com/boardgame/1/" + "a".repeat(1000))));
    }

    @Test
    void requiresBothClassificationsAndAcceptsEveryEnumValue() {
        var validator = new BoardGameClassificationValidator();
        assertThrows(
                InvalidBoardGameException.class,
                () -> validator.validate(new BoardGameInput("Game", null, null, BoardGameType.BASE)));
        assertThrows(
                InvalidBoardGameException.class,
                () -> validator.validate(new BoardGameInput("Game", null, BoardGameState.OWN, null)));
        for (var state : BoardGameState.values()) {
            for (var type : BoardGameType.values()) {
                assertDoesNotThrow(() -> validator.validate(new BoardGameInput("Game", null, state, type)));
            }
        }
    }

    private BoardGameInput input(String name, String link) {
        return new BoardGameInput(name, link, BoardGameState.OWN, BoardGameType.BASE);
    }
}
