package trd.home.boardgame.validator;

import trd.home.boardgame.dto.BoardGameSearchFilter;
import trd.home.boardgame.exception.InvalidBoardGameException;

public class BoardGameSearchFilterValidator implements BoardGameSearchValidator {
    @Override
    public void validate(BoardGameSearchFilter filter, int page) {
        if (page < 0 || (filter.name() != null && filter.name().length() > 255)) {
            throw new InvalidBoardGameException(
                    "Invalid game search: page must be nonnegative and name at most 255 characters.");
        }
    }
}
