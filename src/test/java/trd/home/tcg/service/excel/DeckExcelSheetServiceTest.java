package trd.home.tcg.service.excel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.dto.DeckExcelData;

class DeckExcelSheetServiceTest {
    private final ExcelTableService tables = mock(ExcelTableService.class);
    private final DeckExcelSheetService service = new DeckExcelSheetService(tables);

    @BeforeEach
    void prepareInMemoryCells() {
        doAnswer(invocation -> {
                    XSSFSheet sheet = invocation.getArgument(0);
                    String[] headers = invocation.getArgument(1);
                    int rows = invocation.getArgument(2);
                    for (int row = 0; row <= Math.max(1, rows); row++) {
                        var cells = sheet.createRow(row);
                        for (int column = 0; column < headers.length; column++) cells.createCell(column);
                    }
                    return null;
                })
                .when(tables)
                .prepare(any(), any(), anyInt());
        when(tables.totals(any(), anyInt(), anyInt())).thenAnswer(invocation -> {
            XSSFSheet sheet = invocation.getArgument(0);
            int rows = invocation.getArgument(1);
            return sheet.createRow(Math.max(1, rows) + 2);
        });
    }

    @Test
    void populatesValuesHyperlinksPriceStylesAndQuantityFormulas() throws Exception {
        try (var workbook = new XSSFWorkbook()) {
            var style = workbook.createCellStyle();
            when(tables.priceStyle(workbook)).thenReturn(style);
            var card = new DeckExcelData.Card(
                    3, "https://example.test/Card", CardFoilType.FOIL, new BigDecimal("1.2345"), new BigDecimal("2.5"));
            var summary = service.create(workbook, new DeckExcelData("Deck", "v2", List.of(card)));
            var sheet = workbook.getSheetAt(0);
            var row = sheet.getRow(1);
            assertEquals(3, row.getCell(0).getNumericCellValue());
            assertEquals(card.link(), row.getCell(1).getHyperlink().getAddress());
            assertEquals("FOIL", row.getCell(2).getStringCellValue());
            assertEquals(1.2345, row.getCell(3).getNumericCellValue());
            assertEquals(2.5, row.getCell(5).getNumericCellValue());
            assertEquals("IF(D2=\"\",\"\",A2*D2)", row.getCell(4).getCellFormula());
            assertEquals("IF(F2=\"\",\"\",A2*F2)", row.getCell(6).getCellFormula());
            for (int column = 3; column < 7; column++)
                assertEquals(style, row.getCell(column).getCellStyle());
            assertEquals(new DeckExcelSheetService.SheetSummary("Deck", "v2", "Deck", 4), summary);
            verify(tables).setSum(sheet.getRow(3), 0, 2);
            verify(tables).setEuroSum(sheet.getRow(3), 4, 2);
            verify(tables).setEuroSum(sheet.getRow(3), 6, 2);
        }
    }

    @Test
    void keepsMissingPricesBlankAndUntrustedTextAsText() throws Exception {
        try (var workbook = new XSSFWorkbook()) {
            service.create(
                    workbook,
                    new DeckExcelData(
                            "Deck", "v1", List.of(new DeckExcelData.Card(1, "=1+1", CardFoilType.NO, null, null))));
            var row = workbook.getSheetAt(0).getRow(1);
            assertEquals(CellType.STRING, row.getCell(1).getCellType());
            assertEquals("=1+1", row.getCell(1).getStringCellValue());
            assertNull(row.getCell(1).getHyperlink());
            assertEquals(CellType.BLANK, row.getCell(3).getCellType());
            assertEquals(CellType.BLANK, row.getCell(5).getCellType());
        }
    }

    @Test
    void resolvesSheetNameCollisionsIncludingTruncatedNames() throws Exception {
        try (var workbook = new XSSFWorkbook()) {
            workbook.createSheet("Summary");
            var names = List.of("Summary", "SUMMARY", "Deck/a", "Deck:a", "x".repeat(40), "x".repeat(40), "   ");
            var expected = List.of(
                    "Summary (2)",
                    "SUMMARY (3)",
                    "Deck a",
                    "Deck a (2)",
                    "x".repeat(31),
                    "x".repeat(27) + " (2)",
                    "Deck");
            for (int index = 0; index < names.size(); index++) {
                var result = service.create(workbook, new DeckExcelData(names.get(index), "v1", List.of()));
                assertEquals(expected.get(index), result.sheetName());
            }
        }
    }

    @Test
    void createsSafeTableNamesAndReservesSummaryName() throws Exception {
        try (var workbook = new XSSFWorkbook()) {
            var names = List.of("Deck name", "DeckSummary", "A1", "R1C1", "R", "123Deck", "   ");
            var expected = List.of("Deck_name", "DeckSummary_2", "_A1", "_R1C1", "_R", "_123Deck", "Deck");
            for (int index = 0; index < names.size(); index++) {
                service.create(workbook, new DeckExcelData(names.get(index), "v1", List.of()));
                verify(tables).finish(workbook.getSheetAt(index), expected.get(index), 0, 7);
            }
        }
    }
}
