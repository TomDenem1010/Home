package trd.home.tcg.service.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import trd.home.common.constant.*;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.*;
import trd.home.tcg.dto.DeckCsvData;
import trd.home.tcg.service.application.TcgQueryService;
import trd.home.tcg.service.file.DeckCsvFileWriter;

class ExportDecksToCsvServiceTest {
    private final ApplicationEventQueue queue = mock(ApplicationEventQueue.class);
    private final FrontendNotificationPublisher notifications = mock(FrontendNotificationPublisher.class);
    private final TcgQueryService queries = mock(TcgQueryService.class);
    private final DeckCsvFileWriter writer = mock(DeckCsvFileWriter.class);
    private final ExportDecksToCsvService service =
            new ExportDecksToCsvService(new ApplicationEventProcessor(queue, notifications), queries, writer);

    @Test
    void writesEveryActiveDeckBeforeReportingSuccess() {
        var first = new DeckCsvData("First", "v1", List.of());
        var second = new DeckCsvData("Second", "v2", List.of());
        when(queries.getDeckCsvData()).thenReturn(List.of(first, second));
        var event = new ApplicationEvent(EventType.EXPORT_DECKS_TO_CSV);
        service.process(event);
        assertEquals(EventStatus.DONE, event.getStatus());
        var order = inOrder(writer, notifications);
        order.verify(writer).write(first);
        order.verify(writer).write(second);
        order.verify(notifications)
                .publish(null, FrontendNotificationType.SUCCESS, "CSV deck exporting completed: 2 exported.");
    }

    @Test
    void failsWhenNoActiveDecksExist() {
        when(queries.getDeckCsvData()).thenReturn(List.of());
        var event = new ApplicationEvent(EventType.EXPORT_DECKS_TO_CSV);
        service.process(event);
        assertEquals(EventStatus.ERROR, event.getStatus());
        verifyNoInteractions(writer);
    }

    @Test
    void stopsAndReportsFailureWhenOneFileCannotBeWritten() {
        var first = new DeckCsvData("First", "v1", List.of());
        var second = new DeckCsvData("Second", "v2", List.of());
        when(queries.getDeckCsvData()).thenReturn(List.of(first, second));
        when(writer.write(first)).thenThrow(new IllegalStateException("disk full"));
        var event = new ApplicationEvent(EventType.EXPORT_DECKS_TO_CSV);
        service.process(event);
        assertEquals(EventStatus.ERROR, event.getStatus());
        verify(writer, never()).write(second);
        verify(notifications).publish(null, FrontendNotificationType.ERROR, "Failed to export CSV decks: disk full");
    }
}
