package trd.home.tcg.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.ApplicationEventProcessor;
import trd.home.tcg.service.excel.DeckXlsxSaver;
import trd.home.tcg.service.file.DeckXlsxFileReader;

@Service
@RequiredArgsConstructor
public class ImportDecksFromXlsxService {
    private final ApplicationEventProcessor eventProcessor;
    private final DeckXlsxFileReader reader;
    private final DeckXlsxSaver saver;

    public void process(ApplicationEvent event) {
        eventProcessor.process(event, this::importDecks, "Failed to import XLSX decks: ");
    }

    private String importDecks() {
        int imported = 0;
        int skipped = 0;
        for (var deck : reader.read()) {
            if (saver.save(deck)) {
                imported++;
            } else {
                skipped++;
            }
        }
        return "XLSX deck importing completed: " + imported + " imported, " + skipped + " unchanged.";
    }
}
