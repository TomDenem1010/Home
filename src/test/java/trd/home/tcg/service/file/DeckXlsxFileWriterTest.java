package trd.home.tcg.service.file;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.junit.jupiter.api.Test;
import trd.home.tcg.exception.DeckExcelExportException;

class DeckXlsxFileWriterTest {
    private final FileRouterService router = mock(FileRouterService.class);
    private final DeckXlsxFileWriter writer = new DeckXlsxFileWriter(router);
    private final Path directory = Path.of("tcg", "excel", "export");
    private final Path target = directory.resolve("active-decks_event-id.xlsx");
    private final Path pending = directory.resolve("active-decks_event-id.xlsx.pending");
    private final byte[] content = {1, 2, 3};

    @Test
    void createsExportDirectoryAndPublishesCompletedWorkbook() {
        when(router.getExcelExportFilePath()).thenReturn(directory);
        try (var files = mockStatic(Files.class)) {
            assertEquals(target, writer.write("event-id", content));
            files.verify(() -> Files.createDirectories(directory));
            files.verify(() -> Files.write(pending, content));
            files.verify(() -> Files.move(pending, target, StandardCopyOption.REPLACE_EXISTING));
            files.verifyNoMoreInteractions();
        }
    }

    @Test
    void preservesWriteFailureAndRemovesIncompleteExport() {
        when(router.getExcelExportFilePath()).thenReturn(directory);
        var cause = new IOException("disk full");
        try (var files = mockStatic(Files.class)) {
            files.when(() -> Files.write(pending, content)).thenThrow(cause);
            var failure = assertThrows(DeckExcelExportException.class, () -> writer.write("event-id", content));
            assertSame(cause, failure.getCause());
            files.verify(() -> Files.deleteIfExists(pending));
            files.verify(() -> Files.move(pending, target, StandardCopyOption.REPLACE_EXISTING), never());
        }
    }

    @Test
    void preservesMoveFailureAndCleanupFailure() {
        when(router.getExcelExportFilePath()).thenReturn(directory);
        var cause = new IOException("move failed");
        var cleanup = new IOException("cleanup failed");
        try (var files = mockStatic(Files.class)) {
            files.when(() -> Files.move(pending, target, StandardCopyOption.REPLACE_EXISTING))
                    .thenThrow(cause);
            files.when(() -> Files.deleteIfExists(pending)).thenThrow(cleanup);
            var failure = assertThrows(DeckExcelExportException.class, () -> writer.write("event-id", content));
            assertSame(cause, failure.getCause());
            assertArrayEquals(new Throwable[] {cleanup}, cause.getSuppressed());
        }
    }

    @Test
    void rejectsUnsafeEventIdsBeforeAccessingFilesystem() {
        for (var id : new String[] {null, "", "../outside", "folder/file"}) {
            assertThrows(IllegalArgumentException.class, () -> writer.write(id, content));
        }
        verifyNoInteractions(router);
    }
}
