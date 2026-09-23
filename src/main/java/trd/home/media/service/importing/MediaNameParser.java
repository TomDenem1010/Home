package trd.home.media.service.importing;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import trd.home.media.dto.ParsedVideoName;
import trd.home.media.exception.InvalidVideoNameException;

@Service
public class MediaNameParser {

    private static final String TITLE_SEPARATOR = " - ";
    private static final String ACTOR_SEPARATOR = " & ";

    public ParsedVideoName parse(String fileName) {
        int extension = fileName.lastIndexOf('.');
        String stem = extension > 0 ? fileName.substring(0, extension) : fileName;
        int separator = stem.indexOf(TITLE_SEPARATOR);
        if (separator < 1) {
            throw invalidFileName(fileName);
        }
        String name = stem.substring(separator + TITLE_SEPARATOR.length()).strip();
        var actors = Arrays.stream(stem.substring(0, separator).split(ACTOR_SEPARATOR, -1))
                .map(String::strip)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (name.isEmpty()
                || name.length() > 255
                || actors.stream().anyMatch(actor -> actor.isEmpty() || actor.length() > 255)) {
            throw invalidFileName(fileName);
        }
        return new ParsedVideoName(name, actors);
    }

    private static InvalidVideoNameException invalidFileName(String fileName) {
        return new InvalidVideoNameException("Invalid video filename: " + fileName);
    }
}
