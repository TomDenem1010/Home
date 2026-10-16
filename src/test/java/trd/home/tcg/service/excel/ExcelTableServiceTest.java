package trd.home.tcg.service.excel;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class ExcelTableServiceTest {

    private final ExcelTableService tables = new ExcelTableService();

    @Test
    void fitsColumnsToDisplayedContentWithFoilPaddingAndExcelWidthLimit() throws Exception {
        try (var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Deck");
            tables.prepare(sheet, new String[] {"link", "foilType", "price"}, 1);
            sheet.getRow(1).getCell(0).setCellValue("x".repeat(300));
            String foil = "OIL_SLICK_RAISED_FOIL";
            sheet.getRow(1).getCell(1).setCellValue(foil);
            var price = sheet.getRow(1).getCell(2);
            price.setCellValue(123456.7891);
            price.setCellStyle(tables.priceStyle(workbook));
            tables.fitColumns(sheet);
            assertEquals(255 * 256, sheet.getColumnWidth(0));
            assertEquals(((int) Math.ceil(foil.length() * 1.25) + 3) * 256, sheet.getColumnWidth(1));
            assertEquals(("123,456.79".length() + 2) * 256, sheet.getColumnWidth(2));
            assertEquals("#,##0.00", price.getCellStyle().getDataFormatString());
        }
    }

    @Test
    void createsStyledHeadersExactRowWidthsFiltersAndStripedTable() throws Exception {
        try (var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Deck");
            tables.prepare(sheet, new String[] {"quantity", "price"}, 2);
            tables.finish(sheet, "DeckTable", 2, 2);
            var header = sheet.getRow(0);
            assertEquals(24, header.getHeightInPoints());
            for (int column = 0; column < 2; column++) {
                var style = header.getCell(column).getCellStyle();
                assertTrue(style.getFont().getBold());
                assertEquals(IndexedColors.WHITE.getIndex(), style.getFont().getColor());
                assertEquals(IndexedColors.DARK_BLUE.getIndex(), style.getFillForegroundColor());
                assertEquals(FillPatternType.SOLID_FOREGROUND, style.getFillPattern());
            }
            for (int row = 0; row <= 2; row++) {
                assertEquals(2, sheet.getRow(row).getPhysicalNumberOfCells());
                assertNull(sheet.getRow(row).getCell(2));
            }
            assertNull(sheet.getRow(3));
            var table = sheet.getTables().getFirst();
            assertEquals("DeckTable", table.getDisplayName());
            assertEquals("A1:B3", table.getCTTable().getAutoFilter().getRef());
            assertEquals("A1:B3", sheet.getCTWorksheet().getAutoFilter().getRef());
            assertEquals(
                    "TableStyleMedium2", table.getCTTable().getTableStyleInfo().getName());
            assertTrue(table.getCTTable().getTableStyleInfo().getShowRowStripes());
        }
    }

    @Test
    void stylesEveryTotalCellAndPreservesStyleWhenAddingEuroFormat() throws Exception {
        try (var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Deck");
            tables.prepare(sheet, new String[] {"quantity", "price", "other"}, 2);
            var totals = tables.totals(sheet, 2, 3);
            tables.setSum(totals, 0, 3);
            tables.setEuroSum(totals, 1, 3);
            assertEquals(4, totals.getRowNum());
            assertEquals(3, totals.getPhysicalNumberOfCells());
            assertEquals("SUM(A2:A3)", totals.getCell(0).getCellFormula());
            assertEquals("SUM(B2:B3)", totals.getCell(1).getCellFormula());
            for (int column = 0; column < 3; column++) {
                var style = totals.getCell(column).getCellStyle();
                assertTrue(style.getFont().getBold());
                assertEquals(BorderStyle.THIN, style.getBorderTop());
            }
            assertEquals("#,##0.00", totals.getCell(1).getCellStyle().getDataFormatString());
            assertEquals("General", totals.getCell(0).getCellStyle().getDataFormatString());
        }
    }
}
