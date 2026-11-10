package trd.home.boardgame.dto;

import java.time.LocalDateTime;
import trd.home.boardgame.dao.BoardGamePlay;

public record BoardGamePlayDto(String id, String boardGameId, String boardGameName, LocalDateTime time, int rating) {
    public static BoardGamePlayDto from(BoardGamePlay play) {
        return new BoardGamePlayDto(
                play.getId(),
                play.getBoardGame().getId(),
                play.getBoardGame().getName(),
                play.getTime(),
                play.getRating());
    }
}
