package trd.home.tcg.service.file;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FileRouterServiceTest {
    @ParameterizedTest
    @ValueSource(strings = {"C:/DockerData/Home/tcg", "/tcg", "data/previous/../tcg"})
    void normalizesPlainFilesystemRootAndRoutesByFormat(String configuredRoot) {
        var root = mock(Path.class);
        var absolute = mock(Path.class);
        var normalized = mock(Path.class);
        when(root.toAbsolutePath()).thenReturn(absolute);
        when(absolute.normalize()).thenReturn(normalized);
        try (var paths = mockStatic(Path.class)) {
            paths.when(() -> Path.of(configuredRoot)).thenReturn(root);
            var router = new FileRouterService(configuredRoot);
            assertRoutes(router, normalized);
        }
    }

    @Test
    void acceptsFileUriWithoutTreatingItAsPlainPath() {
        var uri = URI.create("file:/TCG%20files/");
        var root = mock(Path.class);
        when(root.toAbsolutePath()).thenReturn(root);
        when(root.normalize()).thenReturn(root);
        try (var paths = mockStatic(Path.class)) {
            paths.when(() -> Path.of(uri)).thenReturn(root);
            assertRoutes(new FileRouterService(uri.toString()), root);
            paths.verify(() -> Path.of(uri));
        }
    }

    @Test
    void rejectsBlankRootBeforeResolvingPath() {
        try (var paths = mockStatic(Path.class)) {
            assertThrows(IllegalArgumentException.class, () -> new FileRouterService(" "));
            paths.verifyNoInteractions();
        }
    }

    private static void assertRoutes(FileRouterService router, Path root) {
        var csv = mock(Path.class);
        var excel = mock(Path.class);
        var csvImport = mock(Path.class);
        var excelImport = mock(Path.class);
        var csvExport = mock(Path.class);
        var excelExport = mock(Path.class);
        when(root.resolve("csv")).thenReturn(csv);
        when(root.resolve("excel")).thenReturn(excel);
        when(csv.resolve("import")).thenReturn(csvImport);
        when(excel.resolve("import")).thenReturn(excelImport);
        when(csv.resolve("export")).thenReturn(csvExport);
        when(excel.resolve("export")).thenReturn(excelExport);
        when(csvImport.toUri()).thenReturn(URI.create("file:/TCG%20files/csv/import/"));
        when(excelImport.toUri()).thenReturn(URI.create("file:/TCG%20files/excel/import"));
        assertEquals("file:/TCG%20files/csv/import/*.csv", router.getCsvImportFilePath());
        assertEquals("file:/TCG%20files/excel/import/*.xlsx", router.getExcelImportFilePath());
        assertSame(csvExport, router.getCsvExportFilePath());
        assertSame(excelExport, router.getExcelExportFilePath());
    }
}
