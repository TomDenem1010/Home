package trd.home.media.service.importing;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import trd.home.media.dto.ParsedVideoName;
import trd.home.media.exception.*;

class MediaFileReaderTest {
    @ParameterizedTest
    @CsvSource({"1001, 10, 10", "10, 1001, 10", "10, 10, 256", "10, 10, -1"})
    void rejectsUnsupportedPathComponentsBeforeParsing(int parentLength, int fileLength, int folderLength)
            throws Exception {
        var parser = Mockito.mock(MediaNameParser.class);
        var mockedReader = new MediaFileReader(parser);
        var root = Mockito.mock(Path.class);
        var video = Mockito.mock(Path.class);
        var parent = Mockito.mock(Path.class);
        var fileName = Mockito.mock(Path.class);
        var folderName = Mockito.mock(Path.class);
        Mockito.when(root.toRealPath()).thenReturn(root);
        Mockito.when(video.getFileName()).thenReturn(fileName);
        Mockito.when(video.getParent()).thenReturn(parent);
        Mockito.when(fileName.toString()).thenReturn("a".repeat(fileLength - 4) + ".mp4");
        Mockito.when(parent.toString()).thenReturn("p".repeat(parentLength));
        Mockito.when(parent.getFileName()).thenReturn(folderLength < 0 ? null : folderName);
        if (folderLength >= 0) Mockito.when(folderName.toString()).thenReturn("f".repeat(folderLength));
        try (var files = Mockito.mockStatic(Files.class)) {
            files.when(() -> Files.isDirectory(root)).thenReturn(true);
            files.when(() -> Files.walk(root)).thenReturn(Stream.of(video));
            files.when(() -> Files.isRegularFile(video, NOFOLLOW_LINKS)).thenReturn(true);
            assertThrows(InvalidMediaPathException.class, () -> mockedReader.read(root));
            Mockito.verifyNoInteractions(parser);
        }
    }

    private final MediaFileReader reader = new MediaFileReader(new MediaNameParser());

    private final Path directory = Path.of("media-tests").toAbsolutePath().normalize();
    private final Path root = Mockito.mock(Path.class);

    @Test
    void wrapsDirectoryReadFailureAndPreservesCause() throws IOException {
        var cause = new IOException("Directory unavailable");
        Mockito.when(root.toRealPath()).thenThrow(cause);
        var exception = assertThrows(UnableToReadMediaException.class, () -> reader.read(root));
        assertSame(cause, exception.getCause());
    }

    @Test
    void rejectsFileInsteadOfDirectory() throws IOException {
        var file = directory.resolve("video.mp4");
        Mockito.when(root.toRealPath()).thenReturn(file);
        try (var files = Mockito.mockStatic(Files.class)) {
            files.when(() -> Files.isDirectory(file)).thenReturn(false);
            assertThrows(InvalidMediaPathException.class, () -> reader.read(root));
        }
    }

    @Test
    void preservesInvalidVideoNameFailure() throws IOException {
        var files = directoryFiles(directory.resolve("Actor - .mp4"));
        try (files) {
            assertThrows(InvalidVideoNameException.class, () -> reader.read(root));
        }
    }

    @Test
    void recursivelyReadsSupportedVideoFilesInSortedOrder() throws IOException {
        var movies = directory.resolve("Movies");
        var files = directoryFiles(
                movies.resolve("Bob - Second.WEBM"),
                movies.resolve("Alice - First.mp4"),
                movies.resolve("ignored.txt"),
                movies.resolve("no-extension"));
        try (files) {
            var result = reader.read(root);
            assertEquals(
                    List.of("Alice - First.mp4", "Bob - Second.WEBM"),
                    result.stream()
                            .map(file -> file.path().getFileName().toString())
                            .toList());
            assertEquals("First", result.getFirst().video().name());
        }
    }

    @Test
    void doesNotTreatDirectoryOrHiddenExtensionAsVideoFile() throws IOException {
        var folder = directory.resolve("Actor - Directory.mp4");
        try (var files = directoryFiles(folder, directory.resolve(".mp4"))) {
            files.when(() -> Files.isRegularFile(folder, NOFOLLOW_LINKS)).thenReturn(false);
            assertTrue(reader.read(root).isEmpty());
        }
    }

    @Test
    void readsVideoWithoutActors() throws IOException {
        var files = directoryFiles(directory.resolve("Film.mp4"));
        try (files) {
            var result = reader.read(root);
            assertEquals(1, result.size());
            assertEquals("Film", result.getFirst().video().name());
            assertTrue(result.getFirst().video().actors().isEmpty());
        }
    }

    @Test
    void excludesDirectoryEvenWhenItsNameLooksLikeAValidVideo() throws IOException {
        var folder = directory.resolve("Actor - Directory.mp4");
        try (var files = directoryFiles(folder)) {
            files.when(() -> Files.isRegularFile(folder, NOFOLLOW_LINKS)).thenReturn(false);
            assertTrue(reader.read(root).isEmpty());
        }
    }

    @Test
    void acceptsExactMaximumPathComponentLengths() throws IOException {
        MediaNameParser parser = Mockito.mock(MediaNameParser.class);
        MediaFileReader mockedReader = new MediaFileReader(parser);
        Path root = Mockito.mock(Path.class);
        Path resolvedRoot = Mockito.mock(Path.class);
        Path video = Mockito.mock(Path.class);
        Path parent = Mockito.mock(Path.class);
        Path parentName = Mockito.mock(Path.class);
        Path fileName = Mockito.mock(Path.class);
        String videoName = "A".repeat(996) + ".mp4";
        ParsedVideoName parsed = new ParsedVideoName("Film", Set.of("Actor"));

        Mockito.when(root.toRealPath()).thenReturn(resolvedRoot);
        Mockito.when(video.getFileName()).thenReturn(fileName);
        Mockito.when(video.getParent()).thenReturn(parent);
        Mockito.when(fileName.toString()).thenReturn(videoName);
        Mockito.when(parent.toString()).thenReturn("P".repeat(1000));
        Mockito.when(parent.getFileName()).thenReturn(parentName);
        Mockito.when(parentName.toString()).thenReturn("D".repeat(255));
        Mockito.when(parser.parse(videoName)).thenReturn(parsed);

        try (MockedStatic<Files> files = Mockito.mockStatic(Files.class)) {
            files.when(() -> Files.isDirectory(resolvedRoot)).thenReturn(true);
            files.when(() -> Files.walk(resolvedRoot)).thenReturn(Stream.of(video));
            files.when(() -> Files.isRegularFile(video, NOFOLLOW_LINKS)).thenReturn(true);

            var result = mockedReader.read(root);

            assertEquals(1, result.size());
            assertSame(video, result.getFirst().path());
            assertSame(parsed, result.getFirst().video());
        }
    }

    private MockedStatic<Files> directoryFiles(Path... paths) throws IOException {
        Mockito.when(root.toRealPath()).thenReturn(directory);
        var files = Mockito.mockStatic(Files.class);
        files.when(() -> Files.isDirectory(directory)).thenReturn(true);
        files.when(() -> Files.walk(directory)).thenReturn(Stream.of(paths));
        for (var path : paths) {
            files.when(() -> Files.isRegularFile(path, NOFOLLOW_LINKS)).thenReturn(true);
        }
        return files;
    }
}
