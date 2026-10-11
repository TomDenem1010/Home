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
    void readsExporterOutputIncludingSheetNameCollisionsPricesAndEmptyDecks() {
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
    void acceptsEmptyExport() {
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
        var tables = new ExcelTableService();
        return new DeckExcelExportService(
                        new DeckExcelSheetService(tables), new DeckExcelSummarySheetService(tables), tables)
                .export(decks);
    }

    private static ByteArrayInputStream input(XSSFWorkbook workbook) throws Exception {
        var output = new ByteArrayOutputStream();
        workbook.write(output);
        return new ByteArrayInputStream(output.toByteArray());
    }
}
