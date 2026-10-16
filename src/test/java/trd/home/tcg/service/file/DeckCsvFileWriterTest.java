package trd.home.tcg.service.file;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import org.junit.jupiter.api.Test;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.dto.DeckCsvData;
import trd.home.tcg.exception.DeckCsvExportException;
import trd.home.tcg.exception.WrongDeckNameException;

class DeckCsvFileWriterTest {
    private final FileRouterService router = mock(FileRouterService.class);
    private final DeckCsvFileWriter writer = new DeckCsvFileWriter(router);
    private final Path directory = Path.of("csv", "export");
    private final Path pending = directory.resolve("deck-test.csv.pending");

    @Test
    void writesSortedUtf8RowsWithoutHeaderUsingDeckNameAndVersion() throws Exception {
        when(router.getCsvExportFilePath()).thenReturn(directory);
        var deck = new DeckCsvData(
                "Árvíz Deck",
                "v12",
                List.of(
                        card("zebra", 3, CardFoilType.NO),
                        card("Alpha", 2, CardFoilType.FOIL),
                        card("beta", 1, CardFoilType.ETCHED_FOIL)));
        try (var files = mockStatic(Files.class)) {
            files.when(() -> Files.createTempFile(directory, "deck-", ".csv.pending"))
                    .thenReturn(pending);
            byte[] expected = ("2,https://www.cardmarket.com/Magic/Products/Singles/Set/Alpha,FOIL\n"
                            + "1,https://www.cardmarket.com/Magic/Products/Singles/Set/beta,ETCHED_FOIL\n"
                            + "3,https://www.cardmarket.com/Magic/Products/Singles/Set/zebra,NO\n")
                    .getBytes(StandardCharsets.UTF_8);
            var target = directory.resolve("Árvíz Deck_v12.csv");
            assertEquals(target, writer.write(deck));
            files.verify(() -> Files.createDirectories(directory));
            files.verify(() -> Files.write(
                    eq(pending),
                    org.mockito.ArgumentMatchers.<byte[]>argThat(bytes -> java.util.Arrays.equals(expected, bytes))));
            files.verify(() -> Files.move(pending, target, StandardCopyOption.REPLACE_EXISTING));
        }
    }

    @Test
    void writesEachDeckToItsOwnFilename() throws Exception {
        when(router.getCsvExportFilePath()).thenReturn(directory);
        try (var files = mockStatic(Files.class)) {
            files.when(() -> Files.createTempFile(directory, "deck-", ".csv.pending"))
                    .thenReturn(pending);
            assertEquals(directory.resolve("First_v1.csv"), writer.write(new DeckCsvData("First", "v1", List.of())));
            assertEquals(directory.resolve("Second_v2.csv"), writer.write(new DeckCsvData("Second", "v2", List.of())));
            files.verify(
                    () -> Files.move(pending, directory.resolve("First_v1.csv"), StandardCopyOption.REPLACE_EXISTING));
            files.verify(
                    () -> Files.move(pending, directory.resolve("Second_v2.csv"), StandardCopyOption.REPLACE_EXISTING));
        }
    }

    @Test
    void rejectsUnsafeOrUnimportableNamesBeforeWriting() {
        try (var files = mockStatic(Files.class)) {
            for (String name : List.of("../escape", "C:\\escape", "deck:name")) {
                assertThrows(DeckCsvExportException.class, () -> writer.write(new DeckCsvData(name, "v1", List.of())));
            }
            assertThrows(
                    WrongDeckNameException.class, () -> writer.write(new DeckCsvData("Deck_extra", "v1", List.of())));
            assertThrows(
                    WrongDeckNameException.class, () -> writer.write(new DeckCsvData("Deck", "latest", List.of())));
            verifyNoInteractions(router);
            files.verifyNoInteractions();
        }
    }

    @Test
    void cleansPendingFileOnWriteFailureAndPreservesCause() throws Exception {
        when(router.getCsvExportFilePath()).thenReturn(directory);
        var cause = new IOException("disk full");
        try (var files = mockStatic(Files.class)) {
            files.when(() -> Files.createTempFile(directory, "deck-", ".csv.pending"))
                    .thenReturn(pending);
            files.when(() -> Files.write(eq(pending), any(byte[].class))).thenThrow(cause);
            var error = assertThrows(
                    DeckCsvExportException.class, () -> writer.write(new DeckCsvData("Deck", "v1", List.of())));
            assertSame(cause, error.getCause());
            files.verify(() -> Files.deleteIfExists(pending));
            files.verify(() -> Files.move(any(), any(), any(StandardCopyOption.class)), never());
        }
    }

    @Test
    void preservesCleanupFailureAsSuppressedException() throws Exception {
        when(router.getCsvExportFilePath()).thenReturn(directory);
        var cause = new IOException("move failed");
        var cleanup = new IOException("cleanup failed");
        try (var files = mockStatic(Files.class)) {
            files.when(() -> Files.createTempFile(directory, "deck-", ".csv.pending"))
                    .thenReturn(pending);
            files.when(() -> Files.move(pending, directory.resolve("Deck_v1.csv"), StandardCopyOption.REPLACE_EXISTING))
                    .thenThrow(cause);
            files.when(() -> Files.deleteIfExists(pending)).thenThrow(cleanup);
            var error = assertThrows(
                    DeckCsvExportException.class, () -> writer.write(new DeckCsvData("Deck", "v1", List.of())));
            assertSame(cause, error.getCause());
            assertArrayEquals(new Throwable[] {cleanup}, cause.getSuppressed());
        }
    }

    private static DeckCsvData.Card card(String name, int quantity, CardFoilType foil) {
        return new DeckCsvData.Card(
                name, quantity, "https://www.cardmarket.com/Magic/Products/Singles/Set/" + name, foil);
    }
}
