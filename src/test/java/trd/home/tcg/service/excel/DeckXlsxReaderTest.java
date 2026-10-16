package trd.home.tcg.service.excel;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.dto.DeckExcelData;
import trd.home.tcg.exception.DeckImportException;

class DeckXlsxReaderTest {
    private final DeckXlsxReader reader = new DeckXlsxReader();

    @Test
    void readsIndependentInMemoryWorkbookWithPricesAndEmptyDecks() {
        var priced = new DeckExcelData.Card(
                2, "https://example.test/Card", CardFoilType.NO, new BigDecimal("1.2345"), new BigDecimal("2.5"));
        var unpriced = new DeckExcelData.Card(1, "https://example.test/Other", CardFoilType.NO, null, BigDecimal.ZERO);
        var decks = List.of(
                new DeckExcelData("Summary", "v12", List.of(priced, unpriced)),
                new DeckExcelData("Long deck name that is more than thirty one characters A", "V2", List.of(priced)),
                new DeckExcelData("Long deck name that is more than thirty one characters B", "v3", List.of()),
                new DeckExcelData("Deck's/name", "v1", List.of(priced)));
        assertEquals(decks, reader.read(new ByteArrayInputStream(export(decks))));
    }

    @Test
    void acceptsEmptySummary() {
        assertEquals(List.of(), reader.read(new ByteArrayInputStream(export(List.of()))));
    }

    @Test
    void rejectsMissingDeckSheetAndInvalidVersions() throws Exception {
        var data = List.of(new DeckExcelData("Deck", "v1", List.of()));
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(export(data)))) {
            workbook.removeSheetAt(1);
            assertThrows(DeckImportException.class, () -> reader.read(input(workbook)));
        }
        assertThrows(
                DeckImportException.class,
                () -> reader.read(
                        new ByteArrayInputStream(export(List.of(new DeckExcelData("Deck", "bad", List.of()))))));
    }

    @Test
    void rejectsFractionalQuantitiesDuplicateCardsAndNegativePrices() throws Exception {
        var card = new DeckExcelData.Card(2, "link", CardFoilType.NO, BigDecimal.ONE, null);
        var data = List.of(new DeckExcelData("Deck", "v1", List.of(card)));
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(export(data)))) {
            workbook.getSheetAt(1).getRow(1).getCell(0).setCellValue(1.5);
            assertThrows(DeckImportException.class, () -> reader.read(input(workbook)));
        }
        assertThrows(
                DeckImportException.class,
                () -> reader.read(new ByteArrayInputStream(
                        export(List.of(new DeckExcelData("Deck", "v1", List.of(card, card)))))));
        var negative = new DeckExcelData.Card(1, "link", CardFoilType.NO, new BigDecimal("-1"), null);
        assertThrows(
                DeckImportException.class,
                () -> reader.read(
                        new ByteArrayInputStream(export(List.of(new DeckExcelData("Deck", "v1", List.of(negative)))))));
    }

    @Test
    void wrapsMalformedWorkbook() {
        assertThrows(DeckImportException.class, () -> reader.read(new ByteArrayInputStream(new byte[] {1, 2})));
    }

    @ParameterizedTest
    @CsvSource({
        "summary-header,Unexpected export header in sheet: Summary",
        "deck-header,Unexpected export header in sheet: Deck0",
        "missing-table,Expected one export table in sheet: Summary",
        "extra-table,Expected one export table in sheet: Deck0",
        "blank-name,Empty text in exported workbook",
        "duplicate-deck,Duplicate deck in Excel summary: Deck",
        "invalid-reference,Missing deck sheet reference: Deck",
        "missing-link,Missing card link in sheet: Deck0",
        "zero-quantity,Card quantity must be positive: Deck0",
        "negative-quantity,Card quantity must be positive: Deck0",
        "maximum-price,Invalid card price in exported workbook"
    })
    void rejectsInvalidWorkbookWithSpecificReason(String scenario, String message) throws Exception {
        var deck = new DeckExcelData(
                "Deck", "v1", List.of(new DeckExcelData.Card(1, "link", CardFoilType.NO, BigDecimal.ONE, null)));
        var decks = scenario.equals("duplicate-deck") ? List.of(deck, deck) : List.of(deck);
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(export(decks)))) {
            var summary = workbook.getSheetAt(0);
            var sheet = workbook.getSheetAt(1);
            switch (scenario) {
                case "summary-header" -> summary.getRow(0).getCell(0).setCellValue("invalid");
                case "deck-header" -> sheet.getRow(0).getCell(0).setCellValue("invalid");
                case "missing-table" -> summary.removeTable(summary.getTables().getFirst());
                case "extra-table" -> {
                    var extra = sheet.createTable(new AreaReference(
                            new CellReference(3, 0), new CellReference(4, 0), SpreadsheetVersion.EXCEL2007));
                    extra.setName("Extra");
                    extra.setDisplayName("Extra");
                }
                case "blank-name" -> summary.getRow(1).getCell(0).setCellValue(" ");
                case "duplicate-deck" -> {}
                case "invalid-reference" -> summary.getRow(1).getCell(1).setCellFormula("1+1");
                case "missing-link" -> sheet.getRow(1).getCell(1).setBlank();
                case "zero-quantity" -> sheet.getRow(1).getCell(0).setCellValue(0);
                case "negative-quantity" -> sheet.getRow(1).getCell(0).setCellValue(-1);
                case "maximum-price" -> sheet.getRow(1).getCell(3).setCellValue(1000000);
                default -> throw new AssertionError(scenario);
            }
            var error = assertThrows(DeckImportException.class, () -> reader.read(input(workbook)));
            assertEquals(message, error.getMessage());
        }
    }

    private static byte[] export(List<DeckExcelData> decks) {
        try (var workbook = new XSSFWorkbook()) {
            var summary = workbook.createSheet("Summary");
            prepare(
                    summary,
                    new String[] {"deckName", "sumFromEuro", "trendFromEuro", "version"},
                    decks.size(),
                    "DeckSummary");
            for (int index = 0; index < decks.size(); index++) {
                var deck = decks.get(index);
                String sheetName = "Deck" + index;
                var row = summary.getRow(index + 1);
                row.getCell(0).setCellValue(deck.name());
                row.getCell(1).setCellFormula("'" + sheetName + "'!E4");
                row.getCell(3).setCellValue(deck.version());
                var sheet = workbook.createSheet(sheetName);
                prepare(
                        sheet,
                        new String[] {
                            "quantity", "link", "foilType", "fromEuro", "sumFromEuro", "trendEuro", "sumTrendEuro"
                        },
                        deck.cards().size(),
                        "Cards" + index);
                for (int cardIndex = 0; cardIndex < deck.cards().size(); cardIndex++) {
                    var card = deck.cards().get(cardIndex);
                    var cardRow = sheet.getRow(cardIndex + 1);
                    cardRow.getCell(0).setCellValue(card.quantity());
                    cardRow.getCell(1).setCellValue(card.link());
                    cardRow.getCell(2).setCellValue(card.foilType().name());
                    if (card.fromEuro() != null)
                        cardRow.getCell(3).setCellValue(card.fromEuro().doubleValue());
                    if (card.trendEuro() != null)
                        cardRow.getCell(5).setCellValue(card.trendEuro().doubleValue());
                }
            }
            return input(workbook).readAllBytes();
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    private static void prepare(XSSFSheet sheet, String[] headers, int rows, String tableName) {
        for (int row = 0; row <= Math.max(1, rows); row++) {
            var cells = sheet.createRow(row);
            for (int column = 0; column < headers.length; column++) cells.createCell(column);
        }
        for (int column = 0; column < headers.length; column++)
            sheet.getRow(0).getCell(column).setCellValue(headers[column]);
        var area = new AreaReference(
                new CellReference(0, 0),
                new CellReference(Math.max(1, rows), headers.length - 1),
                SpreadsheetVersion.EXCEL2007);
        var table = sheet.createTable(area);
        table.setName(tableName);
        table.setDisplayName(tableName);
    }

    private static ByteArrayInputStream input(XSSFWorkbook workbook) throws Exception {
        var output = new ByteArrayOutputStream();
        workbook.write(output);
        return new ByteArrayInputStream(output.toByteArray());
    }
}
