package trd.home.media.dto;

import java.util.Set;
import lombok.NonNull;

public record ParsedVideoName(@NonNull String name, @NonNull Set<String> actors) {
    public ParsedVideoName {
        actors = Set.copyOf(actors);
    }
}
