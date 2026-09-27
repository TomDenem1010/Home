package trd.home.auth.dto;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public record ApplicationLogSearchFilter(
        String text,
        String createdBy,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timeStart,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timeEnd,
        Boolean error) {}
