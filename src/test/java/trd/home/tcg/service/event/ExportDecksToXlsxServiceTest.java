package trd.home.tcg.service.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import trd.home.common.constant.*;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.*;
import trd.home.tcg.dto.DeckExcelData;
import trd.home.tcg.service.application.TcgQueryService;
import trd.home.tcg.service.excel.DeckExcelExportService;
import trd.home.tcg.service.file.DeckXlsxFileWriter;

class ExportDecksToXlsxServiceTest {
    private final ApplicationEventQueue events = mock(ApplicationEventQueue.class);
    private final FrontendNotificationPublisher notifications = mock(FrontendNotificationPublisher.class);
    private final TcgQueryService queries = mock(TcgQueryService.class);
    private final DeckExcelExportService exporter = mock(DeckExcelExportService.class);
    private final DeckXlsxFileWriter writer = mock(DeckXlsxFileWriter.class);
    private final ExportDecksToXlsxService service = new ExportDecksToXlsxService(
            new ApplicationEventProcessor(events, notifications), queries, exporter, writer);

    @Test
    void exportsDecksAtProcessingTimeAndNotifiesAfterWriting() {
        var event = spy(new ApplicationEvent(EventType.EXPORT_DECKS_TO_XLSX));
        doReturn("event-id").when(event).getId();
        var decks = List.of(new DeckExcelData("Deck", "v1", List.of()));
        byte[] content = {1, 2, 3};
        when(queries.getDeckExcelData()).thenReturn(decks);
        when(exporter.export(decks)).thenReturn(content);
        when(writer.write("event-id", content)).thenReturn(Path.of("active-decks_event-id.xlsx"));
        service.process(event);
        assertEquals(EventStatus.DONE, event.getStatus());
        var order = inOrder(queries, exporter, writer, notifications);
        order.verify(queries).getDeckExcelData();
        order.verify(exporter).export(decks);
        order.verify(writer).write("event-id", content);
        order.verify(notifications)
                .publish(
                        event.getCreatedBy(),
                        FrontendNotificationType.SUCCESS,
                        "XLSX deck exporting completed: active-decks_event-id.xlsx");
    }

    @Test
    void doesNotWriteWhenWorkbookGenerationFails() {
        var event = new ApplicationEvent(EventType.EXPORT_DECKS_TO_XLSX);
        when(queries.getDeckExcelData()).thenReturn(List.of());
        when(exporter.export(List.of())).thenThrow(new IllegalStateException("workbook"));
        service.process(event);
        assertEquals(EventStatus.ERROR, event.getStatus());
        verifyNoInteractions(writer);
        verify(notifications)
                .publish(event.getCreatedBy(), FrontendNotificationType.ERROR, "Failed to export XLSX decks: workbook");
    }

    @Test
    void marksEventFailedWhenFileWritingFails() {
        var event = spy(new ApplicationEvent(EventType.EXPORT_DECKS_TO_XLSX));
        doReturn("event-id").when(event).getId();
        when(queries.getDeckExcelData()).thenReturn(List.of());
        byte[] content = {1};
        when(exporter.export(List.of())).thenReturn(content);
        when(writer.write("event-id", content)).thenThrow(new IllegalStateException("disk full"));
        service.process(event);
        assertEquals(EventStatus.ERROR, event.getStatus());
        verify(notifications)
                .publish(
                        event.getCreatedBy(), FrontendNotificationType.ERROR, "Failed to export XLSX decks: disk full");
    }

    @Test
    void doesNotGenerateOrWriteWhenReadingDeckDataFails() {
        var event = new ApplicationEvent(EventType.EXPORT_DECKS_TO_XLSX);
        when(queries.getDeckExcelData()).thenThrow(new IllegalStateException("database"));
        service.process(event);
        assertEquals(EventStatus.ERROR, event.getStatus());
        verifyNoInteractions(exporter, writer);
    }
}
