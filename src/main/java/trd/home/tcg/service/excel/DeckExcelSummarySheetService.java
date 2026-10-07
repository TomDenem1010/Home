package trd.home.tcg.service.excel;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeckExcelSummarySheetService {

    private static final String[] HEADERS = {"deckName", "sumFromEuro", "trendFromEuro", "version"};

    private final ExcelTableService tables;

    public void populate(XSSFSheet sheet, List<DeckExcelSheetService.SheetSummary> decks) {
        tables.prepare(sheet, HEADERS, decks.size());
        populateDecks(sheet, decks);
        createTotals(sheet, decks.size());
        tables.finish(sheet, "DeckSummary", decks.size(), HEADERS.length);
    }

    private void populateDecks(XSSFSheet sheet, List<DeckExcelSheetService.SheetSummary> decks) {
        var priceStyle = tables.priceStyle(sheet.getWorkbook());
        for (int index = 0; index < decks.size(); index++) {
            populateDeck(sheet.getRow(index + 1), decks.get(index), priceStyle);
        }
    }

    private static void populateDeck(XSSFRow row, DeckExcelSheetService.SheetSummary deck, XSSFCellStyle priceStyle) {
        row.getCell(0).setCellValue(deck.deckName());
        String reference = "'" + deck.sheetName().replace("'", "''") + "'!";
        row.getCell(1).setCellFormula(reference + "E" + deck.totalRow());
        row.getCell(2).setCellFormula(reference + "G" + deck.totalRow());
        row.getCell(1).setCellStyle(priceStyle);
        row.getCell(2).setCellStyle(priceStyle);
        row.getCell(3).setCellValue(deck.version());
    }

    private void createTotals(XSSFSheet sheet, int dataRows) {
        var totals = tables.totals(sheet, dataRows, HEADERS.length);
        int lastExcelRow = Math.max(1, dataRows) + 1;
        tables.setEuroSum(totals, 1, lastExcelRow);
        tables.setEuroSum(totals, 2, lastExcelRow);
    }
}
