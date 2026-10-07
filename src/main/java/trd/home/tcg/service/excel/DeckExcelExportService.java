package trd.home.tcg.service.excel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import trd.home.tcg.dto.DeckExcelData;
import trd.home.tcg.exception.DeckExcelExportException;

@Service
@RequiredArgsConstructor
public class DeckExcelExportService {

    private final DeckExcelSheetService deckSheets;
    private final DeckExcelSummarySheetService summarySheets;
    private final ExcelTableService tables;

    public byte[] export(List<DeckExcelData> decks) {
        try (var workbook = new XSSFWorkbook()) {
            populateWorkbook(workbook, decks);
            finalizeWorkbook(workbook);
            return writeWorkbook(workbook);
        } catch (IOException exception) {
            throw new DeckExcelExportException("Unable to export active decks to Excel", exception);
        }
    }

    private void populateWorkbook(XSSFWorkbook workbook, List<DeckExcelData> decks) {
        var summary = workbook.createSheet("Summary");
        var totals =
                decks.stream().map(deck -> deckSheets.create(workbook, deck)).toList();
        summarySheets.populate(summary, totals);
    }

    private void finalizeWorkbook(XSSFWorkbook workbook) {
        workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
        for (int index = 0; index < workbook.getNumberOfSheets(); index++) {
            tables.fitColumns(workbook.getSheetAt(index));
        }
        workbook.setActiveSheet(0);
    }

    private static byte[] writeWorkbook(XSSFWorkbook workbook) throws IOException {
        try (var output = new ByteArrayOutputStream()) {
            workbook.write(output);
            return output.toByteArray();
        }
    }
}
