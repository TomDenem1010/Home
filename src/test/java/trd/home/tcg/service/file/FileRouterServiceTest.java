package trd.home.tcg.service.file;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class FileRouterServiceTest {
    private final Path directory = Path.of("router-test-root").toAbsolutePath().normalize();

    @Test
    void returnsReadyToUseResourcePatternsWithEncodedSpacesAndCorrectExtensions() {
        var root = directory.resolve("TCG files");
        var router = new FileRouterService(root.toString());
        assertEquals(root.toUri().toString() + "/csv/import/*.csv", router.getCsvImportFilePath());
        assertEquals(root.toUri().toString() + "/excel/import/*.xlsx", router.getExcelImportFilePath());
        assertTrue(router.getCsvImportFilePath().contains("TCG%20files"));
    }

    @Test
    void resolvesRelativeRootAndNormalizesParentSegments() {
        var router =
                new FileRouterService(Path.of("data", "previous", "..", "tcg").toString());
        var root = Path.of("data", "tcg").toAbsolutePath().normalize();
        assertEquals(root.resolve("excel/export"), router.getExcelExportFilePath());
        assertEquals(
                root.resolve("csv/import").toUri().toString().replaceAll("/$", "") + "/*.csv",
                router.getCsvImportFilePath());
        assertEquals(
                root.resolve("excel/import").toUri().toString().replaceAll("/$", "") + "/*.xlsx",
                router.getExcelImportFilePath());
    }

    @Test
    void keepsDifferentRootsIndependent() {
        var first = new FileRouterService(directory.resolve("first").toString());
        var second = new FileRouterService(directory.resolve("second").toString());
        assertNotEquals(first.getCsvImportFilePath(), second.getCsvImportFilePath());
        assertEquals(directory.resolve("first").toUri().toString() + "/csv/import/*.csv", first.getCsvImportFilePath());
    }

    @Test
    void acceptsFileUriRoots() {
        var root = directory.resolve("TCG files");
        var router = new FileRouterService(root.toUri().toString());
        assertEquals(root.toUri().toString() + "/csv/import/*.csv", router.getCsvImportFilePath());
        assertEquals(root.toUri().toString() + "/excel/import/*.xlsx", router.getExcelImportFilePath());
    }

    @Test
    void routesExportUnderTheSameFilesystemRoot() {
        var router = new FileRouterService(directory.toUri().toString());
        assertEquals(directory.resolve("excel/export"), router.getExcelExportFilePath());
    }

    @Test
    void acceptsRootWithoutFileUriPrefix() {
        var root = Path.of("C:/DockerData/Home/tcg").toAbsolutePath().normalize();
        var router = new FileRouterService("C:/DockerData/Home/tcg");

        assertEquals(
                root.resolve("csv/import").toUri().toString().replaceAll("/$", "") + "/*.csv",
                router.getCsvImportFilePath());
    }

    @Test
    void rejectsBlankRoot() {
        assertThrows(IllegalArgumentException.class, () -> new FileRouterService(" "));
    }
}
