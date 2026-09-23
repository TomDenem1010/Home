package trd.home.common.dto;

import lombok.NonNull;
import trd.home.common.event.FrontendNotificationType;

public record FrontendEvent(
        @NonNull String username,
        @NonNull FrontendNotificationType type,
        @NonNull String message) {}
