package trd.home.common.dto;

import trd.home.common.event.FrontendNotificationType;

public record FrontendEvent(FrontendNotificationType type, String message) {}
