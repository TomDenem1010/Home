package trd.home.tcg.service.excel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.dto.DeckExcelData;

class DeckExcelExportServiceTest {

    private final ExcelTableService tables = new ExcelTableService();
    private final DeckExcelExportService exporter = new DeckExcelExportService(
            new DeckExcelSheetService(tables), new DeckExcelSummarySheetService(tables), tables);

    @Test
    void formatsAllEuroCellsToTwoDecimalsAndFitsColumnsToDisplayedContent() throws Exception {
        String name = "A deck name considerably longer than the header";
        String link = "https://example.test/" + "long-card-name".repeat(8);
        var deck = new DeckExcelData(name, "v123", List.of(card(9999, link, "123456.7891", "0.1234")));
        try (var workbook = read(List.of(deck))) {
            var sheet = workbook.getSheetAt(1);
            var formatter = new org.apache.poi.ss.usermodel.DataFormatter(Locale.ROOT);
            formatter.setUseCachedValuesForFormulaCells(true);
            assertEquals("123,456.79", formatter.formatCellValue(sheet.getRow(1).getCell(3)));
            assertEquals(123456.7891, sheet.getRow(1).getCell(3).getNumericCellValue(), 0.00000001);
            for (int column = 3; column <= 6; column++) {
                assertEquals(
                        "#,##0.00",
                        sheet.getRow(1).getCell(column).getCellStyle().getDataFormatString());
            }
            assertEquals((link.length() + 2) * 256, sheet.getColumnWidth(1));
            assertEquals((name.length() + 2) * 256, workbook.getSheetAt(0).getColumnWidth(0));
            String total = formatter.formatCellValue(sheet.getRow(3).getCell(4));
            assertTrue(sheet.getColumnWidth(4) >= (total.length() + 2) * 256);
            assertEquals(("quantity".length() + 2) * 256, sheet.getColumnWidth(0));
        }
    }

    @Test
    void givesLongUppercaseFoilTypesEnoughColumnWidth() throws Exception {
        var cards = java.util.Arrays.stream(CardFoilType.values())
                .map(foil -> new DeckExcelData.Card(1, "https://example.test/" + foil.name(), foil, null, null))
                .toList();
        try (var workbook = read(List.of(new DeckExcelData("Foils", "v1", cards)))) {
            var sheet = workbook.getSheetAt(1);
            int longest = cards.stream()
                    .mapToInt(card -> card.foilType().name().length())
                    .max()
                    .orElseThrow();
            assertTrue(sheet.getColumnWidth(2) >= (Math.ceil(longest * 1.25) + 3) * 256);
            for (int index = 0; index < cards.size(); index++) {
                assertEquals(
                        cards.get(index).foilType().name(),
                        sheet.getRow(index + 1).getCell(2).getStringCellValue());
            }
        }
    }

    @Test
    void capsOversizedColumnsAtExcelLimit() throws Exception {
        try (var workbook = read(List.of(new DeckExcelData(
                "Deck", "v1", List.of(card(1, "https://example.test/" + "a".repeat(300), null, null)))))) {
            assertEquals(255 * 256, workbook.getSheetAt(1).getColumnWidth(1));
        }
    }

    @Test
    void namesTablesAfterDecksWhileKeepingSummaryAndHandlingExcelNameRestrictions() throws Exception {
        var names = List.of(
                "KiloApogeeMind",
                "First deck",
                "Deck/a",
                "Deck:a",
                "DeckSummary",
                "DECKSUMMARY",
                "A1",
                "R1C1",
                "R",
                "C",
                "123Deck",
                "Árvíz",
                "   ",
                "x".repeat(256),
                "x".repeat(255) + "y");
        var decks = names.stream()
                .map(name -> new DeckExcelData(name, "v1", List.of()))
                .toList();
        try (var workbook = read(decks)) {
            assertEquals(
                    "DeckSummary", workbook.getSheetAt(0).getTables().getFirst().getName());
            var expected = List.of(
                    "KiloApogeeMind",
                    "First_deck",
                    "Deck_a",
                    "Deck_a_2",
                    "DeckSummary_2",
                    "DECKSUMMARY_3",
                    "_A1",
                    "_R1C1",
                    "_R",
                    "_C",
                    "_123Deck",
                    "Árvíz",
                    "Deck",
                    "x".repeat(255),
                    "x".repeat(253) + "_2");
            var unique = new HashSet<String>();
            unique.add("decksummary");
            for (int index = 0; index < expected.size(); index++) {
                var table = workbook.getSheetAt(index + 1).getTables().getFirst();
                assertEquals(expected.get(index), table.getName());
                assertEquals(table.getName(), table.getDisplayName());
                assertTrue(unique.add(table.getName().toLowerCase(Locale.ROOT)));
                assertTrue(table.getName().length() <= 255);
            }
        }
    }

    @Test
    void exportsRealTablesWithCalculatedCardDeckAndGrandTotals() throws Exception {
        var first = new DeckExcelData(
                "First deck",
                "v2",
                List.of(
                        card(3, "https://example.test/A", "1.2500", "2.5"),
                        card(2, "https://example.test/B", null, "0.1250"),
                        card(1, "https://example.test/C", null, null)));
        var second = new DeckExcelData("Second deck", "v1", List.of(card(2, "https://example.test/D", "10", "12")));
        try (var workbook = read(List.of(first, second))) {
            assertEquals(3, workbook.getNumberOfSheets());
            assertEquals("Summary", workbook.getSheetName(0));
            assertEquals(0, workbook.getActiveSheetIndex());
            var deck = workbook.getSheetAt(1);
            assertHeaders(deck, "quantity", "link", "foilType", "fromEuro", "sumFromEuro", "trendEuro", "sumTrendEuro");
            assertEquals("A1:G4", deck.getTables().getFirst().getCTTable().getRef());
            assertEquals(
                    "A1:G4",
                    deck.getTables().getFirst().getCTTable().getAutoFilter().getRef());
            assertEquals("A1:G4", deck.getCTWorksheet().getAutoFilter().getRef());
            assertEquals(1, deck.getPaneInformation().getHorizontalSplitPosition());
            assertEquals(
                    "https://example.test/A",
                    deck.getRow(1).getCell(1).getHyperlink().getAddress());
            assertEquals("NO", deck.getRow(1).getCell(2).getStringCellValue());
            assertNumber(deck, 1, 4, 3.75);
            assertNumber(deck, 1, 6, 7.5);
            assertEquals(CellType.BLANK, deck.getRow(2).getCell(3).getCellType());
            assertEquals(CellType.STRING, deck.getRow(2).getCell(4).getCachedFormulaResultType());
            assertEquals("", deck.getRow(2).getCell(4).getStringCellValue());
            assertNumber(deck, 5, 0, 6);
            assertNumber(deck, 5, 4, 3.75);
            assertNumber(deck, 5, 6, 7.75);
            for (int column : new int[] {1, 2, 3, 5}) {
                assertEquals(CellType.BLANK, deck.getRow(5).getCell(column).getCellType());
            }
            var summary = workbook.getSheetAt(0);
            assertHeaders(summary, "deckName", "sumFromEuro", "trendFromEuro", "version");
            assertEquals("A1:D3", summary.getTables().getFirst().getCTTable().getRef());
            assertEquals("First deck", summary.getRow(1).getCell(0).getStringCellValue());
            assertEquals("v2", summary.getRow(1).getCell(3).getStringCellValue());
            assertEquals("'First deck'!E6", summary.getRow(1).getCell(1).getCellFormula());
            assertNumber(summary, 1, 1, 3.75);
            assertNumber(summary, 1, 2, 7.75);
            assertNumber(summary, 4, 1, 23.75);
            assertNumber(summary, 4, 2, 31.75);
            assertEquals(CellType.BLANK, summary.getRow(4).getCell(0).getCellType());
            assertEquals(CellType.BLANK, summary.getRow(4).getCell(3).getCellType());
            assertEquals("#,##0.00", summary.getRow(4).getCell(1).getCellStyle().getDataFormatString());
        }
    }

    @Test
    void emptyLibraryStillHasSummaryTableAndZeroGrandTotals() throws Exception {
        try (var workbook = read(List.of())) {
            assertEquals(1, workbook.getNumberOfSheets());
            var sheet = workbook.getSheetAt(0);
            assertEquals("A1:D2", sheet.getTables().getFirst().getCTTable().getRef());
            assertNumber(sheet, 3, 1, 0);
            assertNumber(sheet, 3, 2, 0);
        }
    }

    @Test
    void emptyDeckStillHasOwnSheetZeroTotalsAndSummaryEntry() throws Exception {
        try (var workbook = read(List.of(new DeckExcelData("Empty deck", "", List.of())))) {
            var deck = workbook.getSheetAt(1);
            assertEquals("A1:G2", deck.getTables().getFirst().getCTTable().getRef());
            assertNumber(deck, 3, 0, 0);
            assertNumber(deck, 3, 4, 0);
            assertNumber(deck, 3, 6, 0);
            var summary = workbook.getSheetAt(0);
            assertEquals("Empty deck", summary.getRow(1).getCell(0).getStringCellValue());
            assertEquals("", summary.getRow(1).getCell(3).getStringCellValue());
            assertNumber(summary, 1, 1, 0);
        }
    }

    @Test
    void createsUniqueSafeSheetNamesAndEscapesCrossSheetReferences() throws Exception {
        var names = List.of(
                "Summary", "SUMMARY", "Deck/a", "Deck:a", "x".repeat(40), "x".repeat(39) + "y", "O'Brien", "   ");
        var decks = names.stream()
                .map(name -> new DeckExcelData(name, "=1+1", List.of(card(1, "=untrusted-text", "0", null))))
                .toList();
        try (var workbook = read(decks)) {
            var unique = new HashSet<String>();
            for (int index = 0; index < workbook.getNumberOfSheets(); index++) {
                String name = workbook.getSheetName(index);
                assertTrue(name.length() <= 31);
                assertTrue(unique.add(name.toLowerCase(Locale.ROOT)));
                assertFalse(name.matches(".*[\\[\\]:*?/\\\\].*"));
            }
            var summary = workbook.getSheetAt(0);
            assertEquals("'O''Brien'!E4", summary.getRow(7).getCell(1).getCellFormula());
            assertNumber(summary, 7, 1, 0);
            assertEquals(CellType.STRING, summary.getRow(1).getCell(3).getCellType());
            assertEquals("=1+1", summary.getRow(1).getCell(3).getStringCellValue());
            assertEquals(
                    CellType.STRING, workbook.getSheetAt(1).getRow(1).getCell(1).getCellType());
            assertNull(workbook.getSheetAt(1).getRow(1).getCell(1).getHyperlink());
        }
    }

    @Test
    void createsEachDeckThroughItsOwnSheetServiceAndPropagatesFailures() {
        var deckSheets = mock(DeckExcelSheetService.class);
        var summarySheets = mock(DeckExcelSummarySheetService.class);
        var service = new DeckExcelExportService(deckSheets, summarySheets, tables);
        var first = new DeckExcelData("First", "v1", List.of());
        var second = new DeckExcelData("Second", "v2", List.of());
        var result = new DeckExcelSheetService.SheetSummary("First", "v1", "First", 4);
        when(deckSheets.create(any(), eq(first))).thenReturn(result);
        var failure = new IllegalStateException("Sheet failure");
        when(deckSheets.create(any(), eq(second))).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> service.export(List.of(first, second))));
        verify(deckSheets).create(any(), eq(first));
        verify(deckSheets).create(any(), eq(second));
        verifyNoInteractions(summarySheets);
    }

    @Test
    void appliesEuroStylesToBothSummaryPriceColumns() throws Exception {
        try (var workbook = read(List.of(new DeckExcelData("Deck", "v1", List.of())))) {
            for (int column : new int[] {1, 2}) {
                assertEquals(
                        "#,##0.00",
                        workbook.getSheetAt(0)
                                .getRow(1)
                                .getCell(column)
                                .getCellStyle()
                                .getDataFormatString());
            }
        }
    }

    @Test
    void incrementsSheetSuffixForRepeatedShortAndTruncatedNames() throws Exception {
        var names = List.of("Deck", "Deck", "Deck", "x".repeat(40), "x".repeat(40), "x".repeat(40));
        try (var workbook = read(
                names.stream().map(n -> new DeckExcelData(n, "v1", List.of())).toList())) {
            assertEquals("Deck (2)", workbook.getSheetName(2));
            assertEquals("Deck (3)", workbook.getSheetName(3));
            assertEquals("x".repeat(27) + " (2)", workbook.getSheetName(5));
            assertEquals("x".repeat(27) + " (3)", workbook.getSheetName(6));
        }
    }

    @Test
    void activatesSummaryEvenWhenDeckPopulationChangesActiveSheet() throws Exception {
        var deckSheets = spy(new DeckExcelSheetService(tables));
        doAnswer(invocation -> {
                    var summary = (DeckExcelSheetService.SheetSummary) invocation.callRealMethod();
                    XSSFWorkbook workbook = invocation.getArgument(0);
                    workbook.setActiveSheet(workbook.getNumberOfSheets() - 1);
                    return summary;
                })
                .when(deckSheets)
                .create(any(), any());
        var service = new DeckExcelExportService(deckSheets, new DeckExcelSummarySheetService(tables), tables);
        try (var workbook = new XSSFWorkbook(
                new ByteArrayInputStream(service.export(List.of(new DeckExcelData("Deck", "v1", List.of())))))) {
            assertEquals(0, workbook.getActiveSheetIndex());
        }
    }

    @Test
    void wrapsWorkbookWriteFailureAndPreservesCause() {
        var failure = new java.io.IOException("Write failed");
        try (var workbooks = mockConstruction(XSSFWorkbook.class, (workbook, context) -> {
            when(workbook.createSheet("Summary")).thenReturn(mock(XSSFSheet.class));
            when(workbook.getCreationHelper())
                    .thenReturn(mock(org.apache.poi.xssf.usermodel.XSSFCreationHelper.class, RETURNS_DEEP_STUBS));
            doThrow(failure).when(workbook).write(any(java.io.OutputStream.class));
        })) {
            var service = new DeckExcelExportService(
                    mock(DeckExcelSheetService.class), mock(DeckExcelSummarySheetService.class), tables);
            var exception = assertThrows(
                    trd.home.tcg.exception.DeckExcelExportException.class, () -> service.export(List.of()));
            assertSame(failure, exception.getCause());
            assertEquals("Unable to export active decks to Excel", exception.getMessage());
            verify(workbooks.constructed().getFirst()).close();
        } catch (java.io.IOException exception) {
            throw new AssertionError(exception);
        }
    }

    private XSSFWorkbook read(List<DeckExcelData> decks) throws Exception {
        return new XSSFWorkbook(new ByteArrayInputStream(exporter.export(decks)));
    }

    private static DeckExcelData.Card card(int quantity, String link, String from, String trend) {
        return new DeckExcelData.Card(
                quantity,
                link,
                CardFoilType.NO,
                from == null ? null : new BigDecimal(from),
                trend == null ? null : new BigDecimal(trend));
    }

    private static void assertHeaders(XSSFSheet sheet, String... names) {
        for (int column = 0; column < names.length; column++) {
            assertEquals(names[column], sheet.getRow(0).getCell(column).getStringCellValue());
            assertEquals(
                    names[column],
                    sheet.getTables()
                            .getFirst()
                            .getCTTable()
                            .getTableColumns()
                            .getTableColumnArray(column)
                            .getName());
        }
    }

    private static void assertNumber(XSSFSheet sheet, int row, int column, double expected) {
        var cell = sheet.getRow(row).getCell(column);
        assertEquals(CellType.FORMULA, cell.getCellType());
        assertEquals(CellType.NUMERIC, cell.getCachedFormulaResultType());
        assertEquals(expected, cell.getNumericCellValue(), 0.00000001);
    }
}
