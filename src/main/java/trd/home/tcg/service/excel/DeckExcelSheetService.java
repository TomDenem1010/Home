package trd.home.tcg.service.excel;

import java.math.BigDecimal;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import trd.home.tcg.dto.DeckExcelData;

@Service
@RequiredArgsConstructor
public class DeckExcelSheetService {

    private static final String[] HEADERS = {
        "quantity", "link", "foilType", "fromEuro", "sumFromEuro", "trendEuro", "sumTrendEuro"
    };

    private final ExcelTableService tables;

    public SheetSummary create(XSSFWorkbook workbook, DeckExcelData deck) {
        var sheet = workbook.createSheet(uniqueSheetName(workbook, deck.name()));
        tables.prepare(sheet, HEADERS, deck.cards().size());
        populateCards(sheet, deck);
        var totals = createTotals(sheet, deck.cards().size());
        tables.finish(
                sheet, uniqueTableName(workbook, deck.name()), deck.cards().size(), HEADERS.length);
        return new SheetSummary(deck.name(), deck.version(), sheet.getSheetName(), totals.getRowNum() + 1);
    }

    private void populateCards(XSSFSheet sheet, DeckExcelData deck) {
        var priceStyle = tables.priceStyle(sheet.getWorkbook());
        for (int index = 0; index < deck.cards().size(); index++) {
            populateCard(sheet.getRow(index + 1), deck.cards().get(index), priceStyle);
        }
    }

    private static void populateCard(XSSFRow row, DeckExcelData.Card card, XSSFCellStyle priceStyle) {
        row.getCell(0).setCellValue(card.quantity());
        setLink(row.getCell(1), card.link());
        row.getCell(2).setCellValue(card.foilType().name());
        setPrice(row.getCell(3), card.fromEuro());
        setPrice(row.getCell(5), card.trendEuro());
        setQuantityPrice(row, 4, "D");
        setQuantityPrice(row, 6, "F");
        for (int column = 3; column < HEADERS.length; column++) {
            row.getCell(column).setCellStyle(priceStyle);
        }
    }

    private static void setLink(XSSFCell cell, String link) {
        cell.setCellValue(link);
        if (link.startsWith("https://") || link.startsWith("http://")) {
            var hyperlink = cell.getSheet().getWorkbook().getCreationHelper().createHyperlink(HyperlinkType.URL);
            hyperlink.setAddress(link);
            cell.setHyperlink(hyperlink);
        }
    }

    private static void setQuantityPrice(XSSFRow row, int column, String priceColumn) {
        int excelRow = row.getRowNum() + 1;
        String price = priceColumn + excelRow;
        row.getCell(column).setCellFormula("IF(" + price + "=\"\",\"\",A" + excelRow + "*" + price + ")");
    }

    private XSSFRow createTotals(XSSFSheet sheet, int dataRows) {
        var totals = tables.totals(sheet, dataRows, HEADERS.length);
        int lastExcelRow = Math.max(1, dataRows) + 1;
        tables.setSum(totals, 0, lastExcelRow);
        tables.setEuroSum(totals, 4, lastExcelRow);
        tables.setEuroSum(totals, 6, lastExcelRow);
        return totals;
    }

    private static void setPrice(XSSFCell cell, BigDecimal price) {
        if (price != null) {
            cell.setCellValue(price.doubleValue());
        }
    }

    private static String uniqueSheetName(XSSFWorkbook workbook, String deckName) {
        String base = WorkbookUtil.createSafeSheetName(deckName);
        if (base.isBlank()) {
            base = "Deck";
        }
        String candidate = base;
        int suffix = 2;
        while (hasSheet(workbook, candidate)) {
            String ending = " (" + suffix++ + ")";
            candidate = base.substring(0, Math.min(base.length(), 31 - ending.length())) + ending;
        }
        return candidate;
    }

    private static String uniqueTableName(XSSFWorkbook workbook, String deckName) {
        String base = safeTableName(deckName);
        String candidate = base;
        int suffix = 2;
        while (hasTable(workbook, candidate)) {
            String ending = "_" + suffix++;
            candidate = base.substring(0, Math.min(base.length(), 255 - ending.length())) + ending;
        }
        return candidate;
    }

    private static String safeTableName(String deckName) {
        String name = deckName.strip().replaceAll("[^\\p{L}\\p{N}_]", "_");
        if (name.isEmpty()) {
            name = "Deck";
        }
        if (needsTableNamePrefix(name)) {
            name = "_" + name;
        }
        return name.substring(0, Math.min(255, name.length()));
    }

    private static boolean needsTableNamePrefix(String name) {
        return !Character.isLetter(name.codePointAt(0)) && !name.startsWith("_")
                || name.equalsIgnoreCase("R")
                || name.equalsIgnoreCase("C")
                || isCellReference(name);
    }

    private static boolean isCellReference(String name) {
        return name.matches("(?i)R[1-9][0-9]*C[1-9][0-9]*")
                || name.matches("(?i)[A-Z]+[0-9]+")
                        && CellReference.classifyCellReference(name, SpreadsheetVersion.EXCEL2007)
                                == CellReference.NameType.CELL;
    }

    private static boolean hasTable(XSSFWorkbook workbook, String name) {
        if (name.equalsIgnoreCase("DeckSummary")) {
            return true;
        }
        for (int index = 0; index < workbook.getNumberOfSheets(); index++) {
            for (var table : workbook.getSheetAt(index).getTables()) {
                if (table.getName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasSheet(XSSFWorkbook workbook, String name) {
        for (int index = 0; index < workbook.getNumberOfSheets(); index++) {
            if (workbook.getSheetName(index).toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    public record SheetSummary(String deckName, String version, String sheetName, int totalRow) {}
}
