package trd.home.common.playwright;

import lombok.NonNull;

public record BrowserPage(int statusCode, @NonNull String content) {}
