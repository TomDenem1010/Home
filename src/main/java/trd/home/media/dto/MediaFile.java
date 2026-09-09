package trd.home.media.dto;

import java.nio.file.Path;

public record MediaFile(Path path, ParsedVideoName video) {}
