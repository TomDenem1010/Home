package trd.home.tcg.service.file;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import trd.home.tcg.dto.DeckExcelData;
import trd.home.tcg.exception.DeckImportException;
import trd.home.tcg.service.excel.*;

class DeckXlsxFileReaderTest {
    @TempDir
    Path directory;

    @Test
    void readsOnlyXlsxFromTheSameDirectoryAsCsvAndLeavesSourceFilesUntouched() throws Exception {
        var tables = new ExcelTableService();
        var exporter = new DeckExcelExportService(
                new DeckExcelSheetService(tables), new DeckExcelSummarySheetService(tables), tables);
        var first = new DeckExcelData("First", "v1", List.of());
        var second = new DeckExcelData("Second", "v2", List.of());
        var firstBytes = exporter.export(List.of(first));
        Files.write(directory.resolve("a.xlsx"), firstBytes);
        Files.write(directory.resolve("b.xlsx"), exporter.export(List.of(second)));
        Files.writeString(directory.resolve("deck_v1.csv"), "CSV must not be parsed as XLSX");
        var reader = new DeckXlsxFileReader(directory.toUri() + "*.csv", new DeckXlsxReader());
        assertEquals(List.of(first, second), reader.read());
        assertArrayEquals(firstBytes, Files.readAllBytes(directory.resolve("a.xlsx")));
        assertEquals(List.of(first, second), reader.read());
    }

    @Test
    void supportsClasspathPatternAndExactFilePath() throws Exception {
        var parser = mock(DeckXlsxReader.class);
        assertDoesNotThrow(() -> new DeckXlsxFileReader("classpath*:tcg/deck/*.csv", parser));
        Files.write(directory.resolve("backup.xlsx"), new byte[] {1});
        var decks = List.of(new DeckExcelData("Deck", "v5", List.of()));
        when(parser.read(any(InputStream.class))).thenReturn(decks);
        assertEquals(
                decks,
                new DeckXlsxFileReader(directory.resolve("backup.csv").toUri().toString(), parser).read());
    }

    @Test
    void reportsMissingWorkbooksWithoutParsingCsv() throws Exception {
        Files.writeString(directory.resolve("deck.csv"), "CSV");
        var parser = mock(DeckXlsxReader.class);
        var reader = new DeckXlsxFileReader(directory.toUri() + "*.csv", parser);
        assertThrows(DeckImportException.class, reader::read);
        verifyNoInteractions(parser);
    }

    @Test
    void propagatesInvalidWorkbookAndRetainsSourceFile() throws Exception {
        Files.write(directory.resolve("broken.xlsx"), new byte[] {1, 2});
        var reader = new DeckXlsxFileReader(directory.toUri() + "*.csv", new DeckXlsxReader());
        assertThrows(DeckImportException.class, reader::read);
        assertTrue(Files.exists(directory.resolve("broken.xlsx")));
    }
}
