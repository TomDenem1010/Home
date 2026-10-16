package trd.home.tcg.service.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import trd.home.common.constant.*;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.*;
import trd.home.tcg.dto.DeckExcelData;
import trd.home.tcg.service.excel.*;
import trd.home.tcg.service.file.DeckXlsxFileReader;

class ImportDecksFromXlsxServiceTest {
    private final ApplicationEventQueue events = mock(ApplicationEventQueue.class);
    private final FrontendNotificationPublisher notifications = mock(FrontendNotificationPublisher.class);
    private final DeckXlsxFileReader reader = mock(DeckXlsxFileReader.class);
    private final DeckXlsxSaver saver = mock(DeckXlsxSaver.class);
    private final ImportDecksFromXlsxService service =
            new ImportDecksFromXlsxService(new ApplicationEventProcessor(events, notifications), reader, saver);

    @Test
    void savesAllDecksAndReportsCounts() {
        var newer = new DeckExcelData("New", "v2", List.of());
        var unchanged = new DeckExcelData("Old", "v1", List.of());
        when(reader.read()).thenReturn(List.of(newer, unchanged));
        when(saver.save(newer)).thenReturn(true);
        var event = new ApplicationEvent(EventType.IMPORT_DECKS_FROM_XLSX, "id");
        service.process(event);
        assertEquals(EventStatus.DONE, event.getStatus());
        verify(saver).save(unchanged);
        verify(notifications)
                .publish(
                        event.getCreatedBy(),
                        FrontendNotificationType.SUCCESS,
                        "XLSX deck importing completed: 1 imported, 1 unchanged.");
    }

    @Test
    void reportsAllImportedDecksWithoutUnchangedDecks() {
        var deck = new DeckExcelData("Deck", "v1", List.of());
        when(reader.read()).thenReturn(List.of(deck));
        when(saver.save(deck)).thenReturn(true);
        var event = new ApplicationEvent(EventType.IMPORT_DECKS_FROM_XLSX);
        service.process(event);
        assertEquals(EventStatus.DONE, event.getStatus());
        verify(notifications)
                .publish(
                        null,
                        FrontendNotificationType.SUCCESS,
                        "XLSX deck importing completed: 1 imported, 0 unchanged.");
    }

    @Test
    void marksFailedWhenPersistenceFails() {
        var deck = new DeckExcelData("Deck", "v1", List.of());
        when(reader.read()).thenReturn(List.of(deck));
        when(saver.save(deck)).thenThrow(new IllegalStateException("database"));
        var event = new ApplicationEvent(EventType.IMPORT_DECKS_FROM_XLSX, "id");
        service.process(event);
        assertEquals(EventStatus.ERROR, event.getStatus());
        verify(notifications)
                .publish(event.getCreatedBy(), FrontendNotificationType.ERROR, "Failed to import XLSX decks: database");
    }

    @Test
    void marksFailedWithoutSavingDecksWhenParsingFails() {
        when(reader.read()).thenThrow(new IllegalArgumentException("invalid workbook"));
        var event = new ApplicationEvent(EventType.IMPORT_DECKS_FROM_XLSX, "id");
        service.process(event);
        assertEquals(EventStatus.ERROR, event.getStatus());
        verifyNoInteractions(saver);
    }
}
