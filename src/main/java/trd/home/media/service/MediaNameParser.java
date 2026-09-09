package trd.home.media.service;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import trd.home.common.logging.LogMethodCall;
import trd.home.media.dto.ParsedVideoName;
import trd.home.media.exception.*;

@Service
public class MediaNameParser {
    @LogMethodCall
    public ParsedVideoName parse(String fileName) {
        int extension = fileName.lastIndexOf('.');
        String stem = extension > 0 ? fileName.substring(0, extension) : fileName;
        int separator = stem.indexOf(" - ");
        if (separator < 1) throw new InvalidVideoNameException("Invalid video filename: " + fileName);
        String name = stem.substring(separator + 3).strip();
        var actors = Arrays.stream(stem.substring(0, separator).split(" & ", -1))
                .map(String::strip)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (name.isEmpty()
                || name.length() > 255
                || actors.stream().anyMatch(actor -> actor.isEmpty() || actor.length() > 255)) {
            throw new InvalidVideoNameException("Invalid video filename: " + fileName);
        }
        return new ParsedVideoName(name, actors);
    }
}
