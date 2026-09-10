package trd.home.media.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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

    @Test
    void recursivelyReadsSupportedVideoFilesInSortedOrder() throws IOException {
        Path movies = Files.createDirectory(directory.resolve("Movies"));
        Files.createFile(movies.resolve("Bob - Second.WEBM"));
        Files.createFile(movies.resolve("Alice - First.mp4"));
        Files.createFile(movies.resolve("ignored.txt"));
        Files.createFile(movies.resolve("no-extension"));

        var files = reader.read(directory);

        assertEquals(
                List.of("Alice - First.mp4", "Bob - Second.WEBM"),
                files.stream().map(file -> file.path().getFileName().toString()).toList());
        assertEquals("First", files.getFirst().video().name());
    }

    @Test
    void doesNotTreatDirectoryOrHiddenExtensionAsVideoFile() throws IOException {
        Files.createDirectory(directory.resolve("Actor - Directory.mp4"));
        Files.createFile(directory.resolve(".mp4"));

        assertThrows(InvalidVideoNameException.class, () -> reader.read(directory));
    }
}
