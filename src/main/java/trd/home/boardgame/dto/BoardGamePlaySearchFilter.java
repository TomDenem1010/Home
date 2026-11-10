package trd.home.boardgame.dto;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public record BoardGamePlaySearchFilter(
        String name,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
        Integer ratingMin,
        Integer ratingMax) {}
