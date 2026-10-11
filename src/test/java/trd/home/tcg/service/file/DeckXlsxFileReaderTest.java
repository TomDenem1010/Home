package trd.home.tcg.service.file;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import trd.home.tcg.dto.DeckExcelData;
import trd.home.tcg.exception.DeckImportException;
import trd.home.tcg.service.excel.*;

class DeckXlsxFileReaderTest {
    private static final String PATTERN = "classpath*:tcg/deck/excel/import/*.xlsx";
    private final FileRouterService router = mock(FileRouterService.class);

    @Test
    void readsInMemoryWorkbooksInResourceOrderAndPreservesSourceBytes() {
        var tables = new ExcelTableService();
        var exporter = new DeckExcelExportService(
                new DeckExcelSheetService(tables), new DeckExcelSummarySheetService(tables), tables);
        var first = new DeckExcelData("First", "v1", List.of());
        var second = new DeckExcelData("Second", "v2", List.of());
        byte[] firstBytes = exporter.export(List.of(first));
        var firstResource = resource("a.xlsx", firstBytes);
        var secondResource = resource("b.xlsx", exporter.export(List.of(second)));
        var reader = reader(new DeckXlsxReader(), List.of(secondResource, firstResource));
        assertEquals(List.of(first, second), reader.read());
        assertArrayEquals(firstBytes, firstResource.getByteArray());
        assertEquals(List.of(first, second), reader.read());
    }

    @Test
    void usesInjectedRouterPatternWithoutModification() {
        var parser = mock(DeckXlsxReader.class);
        var workbook = resource("backup.xlsx", new byte[] {1});
        var decks = List.of(new DeckExcelData("Deck", "v5", List.of()));
        when(parser.read(any())).thenReturn(decks);
        assertEquals(decks, reader(parser, List.of(workbook)).read());
        verify(router).getExcelImportFilePath();
        verify(router, never()).getCsvImportFilePath();
    }

    @Test
    void reportsMissingWorkbooksWithoutParsing() {
        var parser = mock(DeckXlsxReader.class);
        assertThrows(DeckImportException.class, () -> reader(parser, List.of()).read());
        verifyNoInteractions(parser);
    }

    @Test
    void propagatesInvalidWorkbook() {
        var parser = mock(DeckXlsxReader.class);
        var failure = new DeckImportException("Invalid workbook");
        when(parser.read(any())).thenThrow(failure);
        assertSame(
                failure,
                assertThrows(
                        DeckImportException.class,
                        () -> reader(parser, List.of(resource("broken.xlsx", new byte[] {1})))
                                .read()));
    }

    @Test
    void wrapsResourceReadFailureAndPreservesCause() throws Exception {
        var workbook = mock(Resource.class);
        var cause = new IOException("Unreadable workbook");
        when(workbook.getDescription()).thenReturn("broken.xlsx");
        when(workbook.getFilename()).thenReturn("broken.xlsx");
        when(workbook.getInputStream()).thenThrow(cause);
        var parser = mock(DeckXlsxReader.class);
        var exception = assertThrows(
                DeckImportException.class,
                () -> reader(parser, List.of(workbook)).read());
        assertSame(cause, exception.getCause());
        verifyNoInteractions(parser);
    }

    private DeckXlsxFileReader reader(DeckXlsxReader parser, List<Resource> resources) {
        when(router.getExcelImportFilePath()).thenReturn(PATTERN);
        return new DeckXlsxFileReader(router, parser) {
            @Override
            protected List<Resource> readResources(String resourcePattern) {
                assertEquals(PATTERN, resourcePattern);
                return resources;
            }
        };
    }

    private static ByteArrayResource resource(String filename, byte[] content) {
        return new ByteArrayResource(content, filename) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }
}
