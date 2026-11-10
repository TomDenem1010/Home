package trd.home.boardgame.dto;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public record BoardGamePlayInput(
        String boardGameId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
        Integer rating) {}
