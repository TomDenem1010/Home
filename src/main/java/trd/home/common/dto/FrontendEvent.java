package trd.home.common.dto;

import trd.home.common.event.FrontendNotificationType;

public record FrontendEvent(String username, FrontendNotificationType type, String message) {}
