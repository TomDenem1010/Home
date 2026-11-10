package trd.home.boardgame.dto;

import trd.home.boardgame.constant.BoardGameState;
import trd.home.boardgame.constant.BoardGameType;

public record BoardGameSearchFilter(String name, BoardGameState state, BoardGameType type) {}
