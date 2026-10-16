package trd.home.tcg.service.file;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import trd.home.tcg.dto.DeckCsvData;
import trd.home.tcg.exception.DeckCsvExportException;
import trd.home.tcg.validator.CsvDeckCardValidator;
import trd.home.tcg.validator.CsvDeckNameValidator;

@Service
@RequiredArgsConstructor
public class DeckCsvFileWriter {
    private final FileRouterService router;

    public Path write(DeckCsvData deck) {
        String filename = deck.name() + "_" + deck.version() + ".csv";
        if (filename.chars().anyMatch(character -> character < 32 || "<>:\"/\\|?*".indexOf(character) >= 0)) {
            throw new DeckCsvExportException("Invalid CSV export filename: " + filename);
        }
        String content = deck.cards().stream()
                .sorted(Comparator.comparing((DeckCsvData.Card card) -> card.name(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(card -> card.link())
                        .thenComparing(card -> card.foilType().name()))
                .map(card -> card.quantity() + "," + card.link() + ","
                        + card.foilType().name() + "\n")
                .collect(Collectors.joining());
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        var resource = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
        new CsvDeckNameValidator().validateResource(resource);
        new CsvDeckCardValidator().validateResource(resource);
        Path directory = router.getCsvExportFilePath();
        Path target = directory.resolve(filename);
        Path pending = null;
        try {
            Files.createDirectories(directory);
            pending = Files.createTempFile(directory, "deck-", ".csv.pending");
            Files.write(pending, bytes);
            Files.move(pending, target, StandardCopyOption.REPLACE_EXISTING);
            return target;
        } catch (IOException exception) {
            if (pending != null) {
                try {
                    Files.deleteIfExists(pending);
                } catch (IOException cleanupFailure) {
                    exception.addSuppressed(cleanupFailure);
                }
            }
            throw new DeckCsvExportException("Unable to write CSV export to " + target, exception);
        }
    }
}
