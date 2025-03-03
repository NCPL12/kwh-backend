package ncpl.bms.reports.service;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import ncpl.bms.reports.model.dto.DailyKwhReportDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.util.Map;

import org.springframework.stereotype.Service;


@Service
public class DailyKWHExcelService {

    @Autowired
    private JdbcTemplate jdbcTemplate;



    public void generateDailyKwhReportExcel(String fileName, Map<String, List<DailyKwhReportDTO>> reportData, String fromDate, String toDate, String tenantName) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Daily kWh Report");

            int rowNum = 0; // Track the current row number

            // Iterate over each table and generate its data
            for (Map.Entry<String, List<DailyKwhReportDTO>> entry : reportData.entrySet()) {
                String tableName = entry.getKey();
                List<DailyKwhReportDTO> report = entry.getValue();

                // Create the title row
                Row titleRow = sheet.createRow(rowNum++);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("Daily KWh Report of "+ tenantName +" and energy meter : "+ tableName);
                titleCell.setCellStyle(createTitleCellStyle(workbook));
                sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1)); // Merge cells for title

                // Create the subtitle row
                Row subtitleRow = sheet.createRow(rowNum++);
                Cell subtitleCell = subtitleRow.createCell(0);
                subtitleCell.setCellValue("From: " + fromDate + "  To: " + toDate);
                subtitleCell.setCellStyle(createSubtitleCellStyle(workbook));
                sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1)); // Merge cells for subtitle

                // Create the header row
                Row headerRow = sheet.createRow(rowNum++);
                String[] headers = {"Date", "Daily kWh"};
                CellStyle headerStyle = createHeaderCellStyle(workbook);

                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Fill in the data rows
                CellStyle dataStyle = createDataCellStyle(workbook);
                CellStyle dateStyle = createDateCellStyle(workbook); // Create date style

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // Input date format, if needed
                for (DailyKwhReportDTO dto : report) {
                    Row row = sheet.createRow(rowNum++);

                    // Parse date from string if needed and apply the date cell format
                    Date date = sdf.parse(dto.getDate()); // Parse the date
                    Cell dateCell = row.createCell(0);
                    dateCell.setCellValue(date);
                    dateCell.setCellStyle(dateStyle); // Apply the date style

                    Cell kwhCell = row.createCell(1);
                    kwhCell.setCellValue(dto.getDailyKwh());
                    kwhCell.setCellStyle(dataStyle);
                }

                // Add some space between tables
                rowNum++;
            }

            // Autosize columns for better readability
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            // Write the Excel file to disk
            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                workbook.write(fos);
            }

            System.out.println("Excel file generated successfully: " + fileName);
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Error while generating Excel file", e);
        }
    }


    // Helper methods to create cell styles
    private CellStyle createTitleCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createSubtitleCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createDataCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDateCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);

        // Set the date format to dd-MM-yyyy
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy")); // Updated format

        return style;
    }

}
