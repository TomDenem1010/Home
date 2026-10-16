package trd.home.tcg.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.ApplicationEventProcessor;
import trd.home.tcg.exception.DeckCsvExportException;
import trd.home.tcg.service.application.TcgQueryService;
import trd.home.tcg.service.file.DeckCsvFileWriter;

@Service
@RequiredArgsConstructor
public class ExportDecksToCsvService {
    private final ApplicationEventProcessor eventProcessor;
    private final TcgQueryService queries;
    private final DeckCsvFileWriter writer;

    public void process(ApplicationEvent event) {
        eventProcessor.process(event, this::exportDecks, "Failed to export CSV decks: ");
    }

    private String exportDecks() {
        var decks = queries.getDeckCsvData();
        if (decks.isEmpty()) {
            throw new DeckCsvExportException("No active decks to export");
        }
        for (var deck : decks) {
            writer.write(deck);
        }
        return "CSV deck exporting completed: " + decks.size() + " exported.";
    }
}
