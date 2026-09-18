package trd.home.media.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.common.logging.LogMethodCall;
import trd.home.media.dto.MediaFile;
import trd.home.media.exception.InvalidMediaPathException;
import trd.home.media.exception.UnableToReadMediaException;

@Service
@RequiredArgsConstructor
public class MediaFileReader {
    private static final Set<String> EXTENSIONS =
            Set.of("mp4", "m4v", "webm", "mkv", "avi", "mov", "wmv", "mpeg", "mpg", "ogv", "ts");
    private final MediaNameParser parser;

    @LogMethodCall
    public List<MediaFile> read(Path root) {
        try {
            Path directory = root.toRealPath();
            if (!Files.isDirectory(directory)) {
                throw new InvalidMediaPathException("The import path must be a directory.");
            }
            try (var paths = Files.walk(directory)) {
                return paths.filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                        .filter(this::isVideo)
                        .sorted()
                        .map(this::toMediaFile)
                        .toList();
            }
        } catch (IOException | java.io.UncheckedIOException | SecurityException exception) {
            throw new UnableToReadMediaException("Unable to read media directory: " + root, exception);
        }
    }

    private MediaFile toMediaFile(Path path) {
        Path parent = path.getParent();
        Path folderName = parent.getFileName();
        String fileName = path.getFileName().toString();
        if (parent.toString().length() > 1000
                || fileName.length() > 1000
                || folderName == null
                || folderName.toString().length() > 255) {
            throw new InvalidMediaPathException("Unsupported media path: " + path);
        }
        return new MediaFile(path, parser.parse(fileName));
    }

    private boolean isVideo(Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot >= 0 && EXTENSIONS.contains(name.substring(dot + 1).toLowerCase(Locale.ROOT));
    }
}
