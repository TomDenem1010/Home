package trd.home.boardgame.validator;

import trd.home.boardgame.dto.BoardGameSearchFilter;

public interface BoardGameSearchValidator {
    void validate(BoardGameSearchFilter filter, int page);
}
