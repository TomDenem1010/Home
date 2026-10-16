package trd.home.tcg.service.excel;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
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

    private static void prepare(
            org.apache.poi.xssf.usermodel.XSSFSheet sheet, String[] headers, int rows, String tableName) {
        for (int row = 0; row <= Math.max(1, rows); row++) {
            var cells = sheet.createRow(row);
            for (int column = 0; column < headers.length; column++) cells.createCell(column);
        }
        for (int column = 0; column < headers.length; column++)
            sheet.getRow(0).getCell(column).setCellValue(headers[column]);
        var area = new org.apache.poi.ss.util.AreaReference(
                new org.apache.poi.ss.util.CellReference(0, 0),
                new org.apache.poi.ss.util.CellReference(Math.max(1, rows), headers.length - 1),
                org.apache.poi.ss.SpreadsheetVersion.EXCEL2007);
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
