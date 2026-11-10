package trd.home.boardgame.validator;

import trd.home.boardgame.dto.BoardGameInput;
import trd.home.boardgame.exception.InvalidBoardGameException;

public class BoardGameClassificationValidator implements BoardGameInputValidator {
    @Override
    public void validate(BoardGameInput input) {
        if (input.state() == null || input.type() == null) {
            throw new InvalidBoardGameException("Game state and type are required.");
        }
    }
}
