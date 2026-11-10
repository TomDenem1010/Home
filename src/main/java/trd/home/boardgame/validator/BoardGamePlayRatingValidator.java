package trd.home.boardgame.validator;

import trd.home.boardgame.dto.BoardGamePlayInput;
import trd.home.boardgame.exception.InvalidBoardGameException;

public class BoardGamePlayRatingValidator implements BoardGamePlayInputValidator {
    @Override
    public void validate(BoardGamePlayInput input) {
        if (input.rating() == null || input.rating() < 1 || input.rating() > 5) {
            throw new InvalidBoardGameException("Rating must be an integer between 1 and 5.");
        }
    }
}
