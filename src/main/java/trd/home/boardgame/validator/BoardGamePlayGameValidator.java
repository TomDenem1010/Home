package trd.home.boardgame.validator;

import trd.home.boardgame.dto.BoardGamePlayInput;
import trd.home.boardgame.exception.InvalidBoardGameException;

public class BoardGamePlayGameValidator implements BoardGamePlayInputValidator {
    @Override
    public void validate(BoardGamePlayInput input) {
        if (input.boardGameId() == null
                || input.boardGameId().isBlank()
                || input.boardGameId().length() > 255) {
            throw new InvalidBoardGameException("Select a board game.");
        }
    }
}
