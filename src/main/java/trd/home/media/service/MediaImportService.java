package trd.home.media.service;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import org.springframework.stereotype.Service;
import trd.home.common.logging.LogMethodCall;
import trd.home.media.exception.*;

@Service
public class MediaImportService {
    private final MediaFileReader reader;
    private final MediaPersistenceService persistence;

    public MediaImportService(MediaFileReader reader, MediaPersistenceService persistence) {
        this.reader = reader;
        this.persistence = persistence;
    }

    @LogMethodCall
    public synchronized int importPath(String path) {
        if (path == null || path.isBlank()) throw new InvalidMediaPathException("An import path is required.");
        try {
            return persistence.save(reader.read(Path.of(path)));
        } catch (InvalidPathException exception) {
            throw new InvalidMediaPathException("Invalid import path: " + path, exception);
        }
    }
}
