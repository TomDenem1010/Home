package trd.home.tcg.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.ApplicationEventProcessor;
import trd.home.tcg.service.application.TcgQueryService;
import trd.home.tcg.service.excel.DeckExcelExportService;
import trd.home.tcg.service.file.DeckXlsxFileWriter;

@Service
@RequiredArgsConstructor
public class ExportDecksToXlsxService {
    private final ApplicationEventProcessor eventProcessor;
    private final TcgQueryService queries;
    private final DeckExcelExportService exporter;
    private final DeckXlsxFileWriter writer;

    public void process(ApplicationEvent event) {
        eventProcessor.process(event, () -> exportDecks(event), "Failed to export XLSX decks: ");
    }

    private String exportDecks(ApplicationEvent event) {
        var content = exporter.export(queries.getDeckExcelData());
        var file = writer.write(event.getId(), content);
        return "XLSX deck exporting completed: " + file.getFileName();
    }
}
