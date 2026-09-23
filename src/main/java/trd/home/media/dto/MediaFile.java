package trd.home.media.dto;

import java.nio.file.Path;
import lombok.NonNull;

public record MediaFile(@NonNull Path path, @NonNull ParsedVideoName video) {}
