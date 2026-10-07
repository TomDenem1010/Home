package trd.home.tcg.service.excel;

import java.util.Locale;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class ExcelTableService {

    private static final String EURO_FORMAT = "#,##0.00";

    public void prepare(XSSFSheet sheet, String[] headers, int dataRows) {
        createHeader(sheet, headers);
        for (int row = 1; row <= Math.max(1, dataRows); row++) {
            createRow(sheet, row, headers.length);
        }
        sheet.createFreezePane(0, 1);
    }

    public void finish(XSSFSheet sheet, String tableName, int dataRows, int columnCount) {
        int lastDataRow = Math.max(1, dataRows);
        var area = new AreaReference(
                new CellReference(0, 0), new CellReference(lastDataRow, columnCount - 1), SpreadsheetVersion.EXCEL2007);
        var table = sheet.createTable(area);
        table.setName(tableName);
        table.setDisplayName(tableName);
        table.getCTTable().addNewAutoFilter().setRef(area.formatAsString());
        styleTable(table);
        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, lastDataRow, 0, columnCount - 1));
    }

    public void fitColumns(XSSFSheet sheet) {
        var formatter = new DataFormatter(Locale.ROOT);
        formatter.setUseCachedValuesForFormulaCells(true);
        for (int column = 0; column < sheet.getRow(0).getLastCellNum(); column++) {
            int longest = longestContent(sheet, column, formatter);
            sheet.setColumnWidth(column, columnWidth(sheet, column, longest));
        }
    }

    public XSSFRow totals(XSSFSheet sheet, int dataRows, int columns) {
        var style = totalsStyle(sheet.getWorkbook());
        var row = createRow(sheet, Math.max(1, dataRows) + 2, columns);
        for (int column = 0; column < columns; column++) {
            row.getCell(column).setCellStyle(style);
        }
        return row;
    }

    public XSSFCellStyle priceStyle(XSSFWorkbook workbook) {
        var style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat(EURO_FORMAT));
        return style;
    }

    public void setSum(XSSFRow row, int column, int lastExcelRow) {
        String letter = CellReference.convertNumToColString(column);
        row.getCell(column).setCellFormula("SUM(" + letter + "2:" + letter + lastExcelRow + ")");
    }

    public void setEuroSum(XSSFRow row, int column, int lastExcelRow) {
        setSum(row, column, lastExcelRow);
        var cell = row.getCell(column);
        var workbook = row.getSheet().getWorkbook();
        var style = workbook.createCellStyle();
        style.cloneStyleFrom(cell.getCellStyle());
        style.setDataFormat(workbook.createDataFormat().getFormat(EURO_FORMAT));
        cell.setCellStyle(style);
    }

    private static void createHeader(XSSFSheet sheet, String[] headers) {
        var style = headerStyle(sheet.getWorkbook());
        var row = createRow(sheet, 0, headers.length);
        row.setHeightInPoints(24);
        for (int column = 0; column < headers.length; column++) {
            var cell = row.getCell(column);
            cell.setCellValue(headers[column]);
            cell.setCellStyle(style);
        }
    }

    private static XSSFRow createRow(XSSFSheet sheet, int rowIndex, int columns) {
        var row = sheet.createRow(rowIndex);
        for (int column = 0; column < columns; column++) {
            row.createCell(column);
        }
        return row;
    }

    private static XSSFCellStyle headerStyle(XSSFWorkbook workbook) {
        var font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        var style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static XSSFCellStyle totalsStyle(XSSFWorkbook workbook) {
        var font = workbook.createFont();
        font.setBold(true);
        var style = workbook.createCellStyle();
        style.setFont(font);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    private static void styleTable(XSSFTable table) {
        var style = table.getCTTable().addNewTableStyleInfo();
        style.setName("TableStyleMedium2");
        style.setShowRowStripes(true);
    }

    private static int longestContent(XSSFSheet sheet, int column, DataFormatter formatter) {
        int longest = 0;
        for (var row : sheet) {
            for (String line : formatter.formatCellValue(row.getCell(column)).split("\\R")) {
                longest = Math.max(longest, line.codePointCount(0, line.length()));
            }
        }
        return longest;
    }

    private static int columnWidth(XSSFSheet sheet, int column, int longest) {
        // Uppercase foil names need more room than the default character-width estimate.
        int characters = "foilType".equals(sheet.getRow(0).getCell(column).getStringCellValue())
                ? (int) Math.ceil(longest * 1.25) + 3
                : longest + 2;
        return Math.min(255, characters) * 256;
    }
}
