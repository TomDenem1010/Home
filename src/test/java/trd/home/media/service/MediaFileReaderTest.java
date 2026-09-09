package trd.home.media.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import trd.home.media.exception.*;

class MediaFileReaderTest {
    private final MediaFileReader reader = new MediaFileReader(new MediaNameParser());

    @TempDir
    Path directory;

    @Test
    void wrapsDirectoryReadFailureAndPreservesCause() {
        var exception = assertThrows(UnableToReadMediaException.class, () -> reader.read(directory.resolve("missing")));
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void rejectsFileInsteadOfDirectory() throws IOException {
        Path file = Files.createFile(directory.resolve("video.mp4"));
        assertThrows(InvalidMediaPathException.class, () -> reader.read(file));
    }

    @Test
    void preservesInvalidVideoNameFailure() throws IOException {
        Files.createFile(directory.resolve("invalid.mp4"));
        assertThrows(InvalidVideoNameException.class, () -> reader.read(directory));
    }
}
