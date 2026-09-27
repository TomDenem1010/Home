package trd.home.auth.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationLogSort {
    CREATED_AT("createdAt"),
    DURATION_MS("durationMs");

    private final String property;
}
