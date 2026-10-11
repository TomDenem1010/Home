package trd.home.tcg.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.ApplicationEventProcessor;
import trd.home.tcg.service.excel.DeckXlsxSaver;
import trd.home.tcg.service.file.DeckXlsxFileReader;

@Service
@RequiredArgsConstructor
public class SaveDecksFromXlsxService {
    private final ApplicationEventProcessor eventProcessor;
    private final DeckXlsxFileReader reader;
    private final DeckXlsxSaver saver;

    public void process(ApplicationEvent event) {
        eventProcessor.process(event, this::saveDecks, "Failed to save XLSX decks: ");
    }

    private String saveDecks() {
        int saved = 0;
        int skipped = 0;
        for (var deck : reader.read()) {
            if (saver.save(deck)) {
                saved++;
            } else {
                skipped++;
            }
        }
        return "XLSX deck saving completed: " + saved + " saved, " + skipped + " unchanged.";
    }
}
