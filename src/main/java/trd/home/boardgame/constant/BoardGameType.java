package trd.home.boardgame.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoardGameType {
    BASE("Base"),
    EXPANSION("Expansion"),
    BASE_AND_EXPANSION("Base and expansion");

    private final String label;
}
