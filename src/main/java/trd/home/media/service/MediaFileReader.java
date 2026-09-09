package trd.home.media.service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.springframework.stereotype.Service;
import trd.home.common.logging.LogMethodCall;
import trd.home.media.dto.MediaFile;
import trd.home.media.exception.*;

@Service
public class MediaFileReader {
    private static final Set<String> EXTENSIONS =
            Set.of("mp4", "m4v", "webm", "mkv", "avi", "mov", "wmv", "mpeg", "mpg", "ogv", "ts");
    private final MediaNameParser parser;

    public MediaFileReader(MediaNameParser parser) {
        this.parser = parser;
    }

    @LogMethodCall
    public List<MediaFile> read(Path root) {
        try {
            Path directory = root.toRealPath();
            if (!Files.isDirectory(directory))
                throw new InvalidMediaPathException("The import path must be a directory.");
            try (var paths = Files.walk(directory)) {
                return paths.filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                        .filter(this::isVideo)
                        .sorted()
                        .map(path -> {
                            if (path.getParent().toString().length() > 1000
                                    || path.getFileName().toString().length() > 1000
                                    || path.getParent().getFileName() == null
                                    || path.getParent().getFileName().toString().length() > 255) {
                                throw new InvalidMediaPathException("Unsupported media path: " + path);
                            }
                            return new MediaFile(
                                    path, parser.parse(path.getFileName().toString()));
                        })
                        .toList();
            }
        } catch (IOException | java.io.UncheckedIOException | SecurityException exception) {
            throw new UnableToReadMediaException("Unable to read media directory: " + root, exception);
        }
    }

    private boolean isVideo(Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot >= 0 && EXTENSIONS.contains(name.substring(dot + 1).toLowerCase(Locale.ROOT));
    }
}
