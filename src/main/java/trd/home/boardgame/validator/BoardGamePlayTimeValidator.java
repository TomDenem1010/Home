package trd.home.boardgame.validator;

import trd.home.boardgame.dto.BoardGamePlayInput;
import trd.home.boardgame.exception.InvalidBoardGameException;

public class BoardGamePlayTimeValidator implements BoardGamePlayInputValidator {
    @Override
    public void validate(BoardGamePlayInput input) {
        if (input.time() == null || input.time().getYear() < 1 || input.time().getYear() > 9999) {
            throw new InvalidBoardGameException("Enter a valid play date and time.");
        }
    }
}
