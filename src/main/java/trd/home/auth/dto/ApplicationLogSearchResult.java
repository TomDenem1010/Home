package trd.home.auth.dto;

import java.time.Instant;

public record ApplicationLogSearchResult(
        String methodName,
        String methodInput,
        String methodOutput,
        String error,
        Instant createdAt,
        String createdBy) {}
