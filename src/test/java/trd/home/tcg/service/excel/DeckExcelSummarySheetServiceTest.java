package trd.home.tcg.service.excel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.*;
import org.junit.jupiter.api.Test;

class DeckExcelSummarySheetServiceTest {
    private final ExcelTableService tables = mock(ExcelTableService.class);
    private final DeckExcelSummarySheetService service = new DeckExcelSummarySheetService(tables);

    @Test
    void escapesSheetReferencesAndKeepsVersionAsText() throws Exception {
        try (var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Summary");
            var row = sheet.createRow(1);
            for (int column = 0; column < 4; column++) row.createCell(column);
            var totals = sheet.createRow(3);
            var style = workbook.createCellStyle();
            when(tables.priceStyle(workbook)).thenReturn(style);
            when(tables.totals(sheet, 1, 4)).thenReturn(totals);
            service.populate(sheet, List.of(new DeckExcelSheetService.SheetSummary("Deck", "=1+1", "O'Brien", 5)));
            assertEquals("Deck", row.getCell(0).getStringCellValue());
            assertEquals("'O''Brien'!E5", row.getCell(1).getCellFormula());
            assertEquals("'O''Brien'!G5", row.getCell(2).getCellFormula());
            assertEquals(style, row.getCell(1).getCellStyle());
            assertEquals(style, row.getCell(2).getCellStyle());
            assertEquals(CellType.STRING, row.getCell(3).getCellType());
            assertEquals("=1+1", row.getCell(3).getStringCellValue());
            verify(tables).setEuroSum(totals, 1, 2);
            verify(tables).setEuroSum(totals, 2, 2);
            verify(tables).finish(sheet, "DeckSummary", 1, 4);
        }
    }

    @Test
    void emptySummaryStillRequestsGrandTotals() throws Exception {
        try (var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Summary");
            var totals = sheet.createRow(3);
            when(tables.totals(sheet, 0, 4)).thenReturn(totals);
            service.populate(sheet, List.of());
            verify(tables).setEuroSum(totals, 1, 2);
            verify(tables).setEuroSum(totals, 2, 2);
            verify(tables).finish(sheet, "DeckSummary", 0, 4);
        }
    }
}
