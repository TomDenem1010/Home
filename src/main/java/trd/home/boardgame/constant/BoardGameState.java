package trd.home.boardgame.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoardGameState {
    OWN("Own"),
    SOLD("Sold");

    private final String label;
}
