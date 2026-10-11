package trd.home.tcg.service.excel;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.dto.DeckExcelData;
import trd.home.tcg.exception.DeckImportException;

@Service
public class DeckXlsxReader {
    private static final Pattern SHEET_REFERENCE = Pattern.compile("^'((?:[^']|'')+)'!E[0-9]+$");

    public List<DeckExcelData> read(InputStream input) {
        try (var workbook = new XSSFWorkbook(input)) {
            var summary = workbook.getSheetAt(0);
            requireHeaders(summary, "deckName", "sumFromEuro", "trendFromEuro", "version");
            var decks = new ArrayList<DeckExcelData>();
            var names = new HashSet<String>();
            for (int index = 1; index <= summary.getTables().getFirst().getEndRowIndex(); index++) {
                var row = summary.getRow(index);
                if (blank(row, 0)) {
                    continue;
                }
                String name = text(row, 0);
                String version = text(row, 3);
                versionNumber(version);
                if (!names.add(name)) {
                    throw new DeckImportException("Duplicate deck in Excel summary: " + name);
                }
                var reference = SHEET_REFERENCE.matcher(row.getCell(1).getCellFormula());
                if (!reference.matches()) {
                    throw new DeckImportException("Missing deck sheet reference: " + name);
                }
                var sheet = workbook.getSheet(reference.group(1).replace("''", "'"));
                if (sheet == null || sheet == summary) {
                    throw new DeckImportException("Deck sheet not found: " + name);
                }
                decks.add(new DeckExcelData(name, version, readCards(sheet)));
            }
            return List.copyOf(decks);
        } catch (DeckImportException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new DeckImportException("Unable to read exported deck XLSX workbook", exception);
        }
    }

    public static BigInteger versionNumber(String version) {
        if (version == null || !version.matches("[vV]?[0-9]+")) {
            throw new DeckImportException("Invalid deck version: " + version);
        }
        return new BigInteger(version.replaceFirst("^[vV]", ""));
    }

    private List<DeckExcelData.Card> readCards(XSSFSheet sheet) {
        requireHeaders(sheet, "quantity", "link", "foilType", "fromEuro", "sumFromEuro", "trendEuro", "sumTrendEuro");
        var cards = new ArrayList<DeckExcelData.Card>();
        var identities = new HashSet<List<String>>();
        for (int index = 1; index <= sheet.getTables().getFirst().getEndRowIndex(); index++) {
            var row = sheet.getRow(index);
            if (blank(row, 1)) {
                if (!blank(row, 0) || !blank(row, 2)) {
                    throw new DeckImportException("Missing card link in sheet: " + sheet.getSheetName());
                }
                continue;
            }
            int quantity =
                    BigDecimal.valueOf(row.getCell(0).getNumericCellValue()).intValueExact();
            if (quantity <= 0) {
                throw new DeckImportException("Card quantity must be positive: " + sheet.getSheetName());
            }
            String link = text(row, 1);
            var foil = CardFoilType.valueOf(text(row, 2));
            if (!identities.add(List.of(link, foil.name()))) {
                throw new DeckImportException("Duplicate card in sheet: " + sheet.getSheetName());
            }
            cards.add(new DeckExcelData.Card(quantity, link, foil, price(row.getCell(3)), price(row.getCell(5))));
        }
        return List.copyOf(cards);
    }

    private static void requireHeaders(XSSFSheet sheet, String... headers) {
        if (sheet.getTables().size() != 1) {
            throw new DeckImportException("Expected one export table in sheet: " + sheet.getSheetName());
        }
        for (int index = 0; index < headers.length; index++) {
            if (!headers[index].equals(text(sheet.getRow(0), index))) {
                throw new DeckImportException("Unexpected export header in sheet: " + sheet.getSheetName());
            }
        }
    }

    private static boolean blank(Row row, int column) {
        return row == null || row.getCell(column) == null || row.getCell(column).getCellType() == CellType.BLANK;
    }

    private static String text(Row row, int column) {
        String value = row.getCell(column).getStringCellValue();
        if (value.isBlank()) {
            throw new DeckImportException("Empty text in exported workbook");
        }
        return value;
    }

    private static BigDecimal price(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        var value = BigDecimal.valueOf(cell.getNumericCellValue());
        if (value.signum() < 0
                || value.stripTrailingZeros().scale() > 4
                || value.compareTo(new BigDecimal("1000000")) >= 0) {
            throw new DeckImportException("Invalid card price in exported workbook");
        }
        return value.stripTrailingZeros();
    }
}
