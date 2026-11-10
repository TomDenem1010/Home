package trd.home.boardgame.exception;

import trd.home.common.exception.HomeException;

public class BoardGameNotFoundException extends HomeException {
    public BoardGameNotFoundException(String id) {
        super("Board game not found: " + id);
    }
}
