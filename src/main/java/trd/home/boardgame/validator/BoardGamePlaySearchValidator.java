package trd.home.boardgame.validator;

import trd.home.boardgame.dto.BoardGamePlaySearchFilter;

public interface BoardGamePlaySearchValidator {
    void validate(BoardGamePlaySearchFilter filter, int page);
}
