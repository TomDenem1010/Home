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
import org.junit.jupiter.api.io.TempDir;
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
        Files.createFile(directory.resolve("Actor - .mp4"));
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

        assertTrue(reader.read(directory).isEmpty());
    }

    @Test
    void readsVideoWithoutActors() throws IOException {
        Files.createFile(directory.resolve("Film.mp4"));
        var files = reader.read(directory);
        assertEquals(1, files.size());
        assertEquals("Film", files.getFirst().video().name());
        assertTrue(files.getFirst().video().actors().isEmpty());
    }

    @Test
    void excludesDirectoryEvenWhenItsNameLooksLikeAValidVideo() throws IOException {
        Files.createDirectory(directory.resolve("Actor - Directory.mp4"));

        assertTrue(reader.read(directory).isEmpty());
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
}
