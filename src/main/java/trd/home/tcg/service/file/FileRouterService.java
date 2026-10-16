package trd.home.tcg.service.file;

import java.net.URI;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileRouterService {
    private final Path rootDirectory;

    public FileRouterService(@Value("${tcg.file.root-path}") String rootPath) {
        if (rootPath.isBlank()) {
            throw new IllegalArgumentException("TCG file root path must not be blank");
        }
        Path directory = rootPath.startsWith("file:") ? Path.of(URI.create(rootPath)) : Path.of(rootPath);
        this.rootDirectory = directory.toAbsolutePath().normalize();
    }

    public String getCsvImportFilePath() {
        return importResourcePattern("csv", "*.csv");
    }

    public String getExcelImportFilePath() {
        return importResourcePattern("excel", "*.xlsx");
    }

    public Path getExcelExportFilePath() {
        return rootDirectory.resolve("excel").resolve("export");
    }

    private String importResourcePattern(String format, String filePattern) {
        String resourcePath =
                rootDirectory.resolve(format).resolve("import").toUri().toString();
        return (resourcePath.endsWith("/") ? resourcePath : resourcePath + "/") + filePattern;
    }
}
