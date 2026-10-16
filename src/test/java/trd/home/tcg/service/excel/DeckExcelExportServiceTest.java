package trd.home.tcg.service.excel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.apache.poi.xssf.usermodel.*;
import org.junit.jupiter.api.Test;
import trd.home.tcg.dto.DeckExcelData;
import trd.home.tcg.exception.DeckExcelExportException;

class DeckExcelExportServiceTest {
    private final DeckExcelSheetService deckSheets = mock(DeckExcelSheetService.class);
    private final DeckExcelSummarySheetService summarySheets = mock(DeckExcelSummarySheetService.class);
    private final ExcelTableService tables = mock(ExcelTableService.class);
    private final DeckExcelExportService exporter = new DeckExcelExportService(deckSheets, summarySheets, tables);

    @Test
    void delegatesSheetsEvaluatesFormulasAndSerializesToMemory() throws Exception {
        var summary = mock(XSSFSheet.class);
        var deckSheet = mock(XSSFSheet.class);
        var helper = mock(XSSFCreationHelper.class, RETURNS_DEEP_STUBS);
        var deck = new DeckExcelData("Deck", "v1", List.of());
        var totals = new DeckExcelSheetService.SheetSummary("Deck", "v1", "Deck", 4);
        when(deckSheets.create(any(), eq(deck))).thenReturn(totals);
        try (var workbooks = mockConstruction(XSSFWorkbook.class, (workbook, context) -> {
            when(workbook.createSheet("Summary")).thenReturn(summary);
            when(workbook.getCreationHelper()).thenReturn(helper);
            when(workbook.getNumberOfSheets()).thenReturn(2);
            when(workbook.getSheetAt(0)).thenReturn(summary);
            when(workbook.getSheetAt(1)).thenReturn(deckSheet);
            doAnswer(invocation -> {
                        OutputStream output = invocation.getArgument(0);
                        output.write(new byte[] {1, 2, 3});
                        return null;
                    })
                    .when(workbook)
                    .write(any(OutputStream.class));
        })) {
            assertArrayEquals(new byte[] {1, 2, 3}, exporter.export(List.of(deck)));
            var workbook = workbooks.constructed().getFirst();
            var order = inOrder(deckSheets, summarySheets, helper.createFormulaEvaluator(), tables, workbook);
            order.verify(deckSheets).create(workbook, deck);
            order.verify(summarySheets).populate(summary, List.of(totals));
            order.verify(helper.createFormulaEvaluator()).evaluateAll();
            order.verify(tables).fitColumns(summary);
            order.verify(tables).fitColumns(deckSheet);
            order.verify(workbook).setActiveSheet(0);
            order.verify(workbook).write(any(OutputStream.class));
            order.verify(workbook).close();
        }
    }

    @Test
    void emptyLibraryStillPopulatesSummary() throws Exception {
        var summary = mock(XSSFSheet.class);
        try (var workbooks = mockConstruction(XSSFWorkbook.class, (workbook, context) -> {
            when(workbook.createSheet("Summary")).thenReturn(summary);
            when(workbook.getCreationHelper()).thenReturn(mock(XSSFCreationHelper.class, RETURNS_DEEP_STUBS));
        })) {
            exporter.export(List.of());
            verifyNoInteractions(deckSheets);
            verify(summarySheets).populate(summary, List.of());
            verify(workbooks.constructed().getFirst()).close();
        }
    }

    @Test
    void propagatesSheetFailureAndClosesWorkbook() throws Exception {
        var deck = new DeckExcelData("Deck", "v1", List.of());
        var failure = new IllegalStateException("sheet failed");
        when(deckSheets.create(any(), eq(deck))).thenThrow(failure);
        try (var workbooks = mockConstruction(XSSFWorkbook.class)) {
            assertSame(failure, assertThrows(IllegalStateException.class, () -> exporter.export(List.of(deck))));
            verifyNoInteractions(summarySheets, tables);
            verify(workbooks.constructed().getFirst()).close();
        }
    }

    @Test
    void wrapsWorkbookWriteFailureAndPreservesCause() throws Exception {
        var failure = new IOException("write failed");
        try (var workbooks = mockConstruction(XSSFWorkbook.class, (workbook, context) -> {
            when(workbook.getCreationHelper()).thenReturn(mock(XSSFCreationHelper.class, RETURNS_DEEP_STUBS));
            doThrow(failure).when(workbook).write(any(OutputStream.class));
        })) {
            var error = assertThrows(DeckExcelExportException.class, () -> exporter.export(List.of()));
            assertSame(failure, error.getCause());
            verify(workbooks.constructed().getFirst()).close();
        }
    }
}
