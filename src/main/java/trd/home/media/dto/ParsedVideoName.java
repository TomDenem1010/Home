package trd.home.media.dto;

import java.util.Set;

public record ParsedVideoName(String name, Set<String> actors) {
    public ParsedVideoName {
        actors = Set.copyOf(actors);
    }
}
