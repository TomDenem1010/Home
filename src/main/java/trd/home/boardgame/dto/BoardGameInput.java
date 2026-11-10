package trd.home.boardgame.dto;

import trd.home.boardgame.constant.BoardGameState;
import trd.home.boardgame.constant.BoardGameType;

public record BoardGameInput(String name, String bggLink, BoardGameState state, BoardGameType type) {}
