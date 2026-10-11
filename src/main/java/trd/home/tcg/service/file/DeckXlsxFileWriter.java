package trd.home.tcg.service.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.tcg.exception.DeckExcelExportException;

@Service
@RequiredArgsConstructor
public class DeckXlsxFileWriter {
    private final FileRouterService router;

    public Path write(String eventId, byte[] content) {
        if (eventId == null || !eventId.matches("[a-zA-Z0-9-]+")) {
            throw new IllegalArgumentException("Invalid export event ID");
        }
        Path directory = router.getExcelExportFilePath();
        Path target = directory.resolve("active-decks_" + eventId + ".xlsx");
        Path pending = directory.resolve("active-decks_" + eventId + ".xlsx.pending");
        try {
            Files.createDirectories(directory);
            Files.write(pending, content);
            Files.move(pending, target, StandardCopyOption.REPLACE_EXISTING);
            return target;
        } catch (IOException exception) {
            try {
                Files.deleteIfExists(pending);
            } catch (IOException cleanupFailure) {
                exception.addSuppressed(cleanupFailure);
            }
            throw new DeckExcelExportException("Unable to write Excel export to " + target, exception);
        }
    }
}
