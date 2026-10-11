package trd.home.tcg.service.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.common.file.ResourceFileReader;
import trd.home.tcg.dto.DeckExcelData;
import trd.home.tcg.exception.DeckImportException;
import trd.home.tcg.service.excel.DeckXlsxReader;

@Service
@RequiredArgsConstructor
public class DeckXlsxFileReader extends ResourceFileReader {
    private final FileRouterService fileRouterService;
    private final DeckXlsxReader reader;

    public List<DeckExcelData> read() {
        var resources = readResources(fileRouterService.getExcelImportFilePath()).stream()
                .sorted(Comparator.comparing(
                        resource -> Objects.requireNonNull(resource).getDescription()))
                .toList();
        if (resources.isEmpty()) {
            throw new DeckImportException("No XLSX deck workbooks found at the configured deck resource path");
        }
        var decks = new ArrayList<DeckExcelData>();
        for (var resource : resources) {
            try (var input = resource.getInputStream()) {
                decks.addAll(reader.read(input));
            } catch (IOException exception) {
                throw new DeckImportException(
                        "Unable to read XLSX deck resource: " + resource.getFilename(), exception);
            }
        }
        return List.copyOf(decks);
    }
}
