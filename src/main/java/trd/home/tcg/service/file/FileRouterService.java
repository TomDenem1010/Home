package trd.home.tcg.service.file;

import java.net.URI;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileRouterService {
    private final String rootResourcePath;

    public FileRouterService(@Value("${tcg.file.root-path}") String rootPath) {
        if (rootPath.isBlank()) {
            throw new IllegalArgumentException("TCG file root path must not be blank");
        }
        if (rootPath.startsWith("classpath*:") || rootPath.startsWith("classpath:")) {
            this.rootResourcePath = rootPath.replaceAll("/+$", "") + "/";
        } else {
            Path directory = rootPath.startsWith("file:") ? Path.of(URI.create(rootPath)) : Path.of(rootPath);
            String resourcePath = directory.toAbsolutePath().normalize().toUri().toString();
            this.rootResourcePath = resourcePath.endsWith("/") ? resourcePath : resourcePath + "/";
        }
    }

    public String getCsvImportFilePath() {
        return importResourcePattern("csv", "*.csv");
    }

    public String getExcelImportFilePath() {
        return importResourcePattern("excel", "*.xlsx");
    }

    private String importResourcePattern(String format, String filePattern) {
        return rootResourcePath + format + "/import/" + filePattern;
    }
}
