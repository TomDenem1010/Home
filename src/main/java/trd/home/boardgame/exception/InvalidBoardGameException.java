package trd.home.boardgame.exception;

import trd.home.common.exception.HomeException;

public class InvalidBoardGameException extends HomeException {
    public InvalidBoardGameException(String message) {
        super(message);
    }
}
