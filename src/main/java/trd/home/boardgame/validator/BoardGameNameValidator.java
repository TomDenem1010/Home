package trd.home.boardgame.validator;

import trd.home.boardgame.dto.BoardGameInput;
import trd.home.boardgame.exception.InvalidBoardGameException;

public class BoardGameNameValidator implements BoardGameInputValidator {
    @Override
    public void validate(BoardGameInput input) {
        if (input.name() == null || input.name().isBlank() || input.name().length() > 255) {
            throw new InvalidBoardGameException("Game name is required and must not exceed 255 characters.");
        }
    }
}
