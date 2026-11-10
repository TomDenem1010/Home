package trd.home.boardgame.dto;

import trd.home.boardgame.constant.BoardGameState;
import trd.home.boardgame.constant.BoardGameType;
import trd.home.boardgame.dao.BoardGame;

public record BoardGameDto(String id, String name, String bggLink, BoardGameState state, BoardGameType type) {
    public static BoardGameDto from(BoardGame game) {
        return new BoardGameDto(game.getId(), game.getName(), game.getBggLink(), game.getState(), game.getType());
    }
}
