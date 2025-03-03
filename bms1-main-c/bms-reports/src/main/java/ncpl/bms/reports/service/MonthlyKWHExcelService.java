package ncpl.bms.reports.service;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.io.IOException;
import java.util.List;
import ncpl.bms.reports.model.dto.MonthlyKwhReportDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.util.Map;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.springframework.stereotype.Service;

@Service
public class MonthlyKWHExcelService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

//    public void generateMonthlyKwhReportExcel(String fileName, Map<String, List<MonthlyKwhReportDTO>> reportData, String fromMonthYear, String toMonthYear) {
//        try (Workbook workbook = new XSSFWorkbook()) {
//            Sheet sheet = workbook.createSheet("Monthly kWh Report");
//
//            int rowNum = 0;
//
//            // Iterate over each table and generate its data
//            for (Map.Entry<String, List<MonthlyKwhReportDTO>> entry : reportData.entrySet()) {
//                String tableName = entry.getKey();
//                List<MonthlyKwhReportDTO> report = entry.getValue();
//
//                // Create title row
//                Row titleRow = sheet.createRow(rowNum++);
//                Cell titleCell = titleRow.createCell(0);
//                titleCell.setCellValue("Monthly kWh Report of " + tableName);
//                titleCell.setCellStyle(createTitleCellStyle(workbook));
//                sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));
//
//                // Create subtitle row
//                Row subtitleRow = sheet.createRow(rowNum++);
//                Cell subtitleCell = subtitleRow.createCell(0);
//                subtitleCell.setCellValue("From: " + fromMonthYear + "  To: " + toMonthYear);
//                subtitleCell.setCellStyle(createSubtitleCellStyle(workbook));
//                sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));
//
//                // Create header row
//                Row headerRow = sheet.createRow(rowNum++);
//                String[] headers = {"Month", "Monthly kWh"};
//                CellStyle headerStyle = createHeaderCellStyle(workbook);
//
//                for (int i = 0; i < headers.length; i++) {
//                    Cell cell = headerRow.createCell(i);
//                    cell.setCellValue(headers[i]);
//                    cell.setCellStyle(headerStyle);
//                }
//
//                // Fill in the data rows
//                CellStyle dataStyle = createDataCellStyle(workbook);
//                for (MonthlyKwhReportDTO dto : report) {
//                    Row row = sheet.createRow(rowNum++);
//                    Cell monthCell = row.createCell(0);
//                    monthCell.setCellValue(dto.getMonth());
//                    monthCell.setCellStyle(dataStyle);
//
//                    Cell kwhCell = row.createCell(1);
//                    kwhCell.setCellValue(dto.getMonthlyKwh());
//                    kwhCell.setCellStyle(dataStyle);
//                }
//
//                // Add space between tables
//                rowNum++;
//            }
//
//            // Autosize columns
//            sheet.autoSizeColumn(0);
//            sheet.autoSizeColumn(1);
//
//            // Write the Excel file to disk
//            try (FileOutputStream fos = new FileOutputStream(fileName)) {
//                workbook.write(fos);
//            }
//
//            System.out.println("Excel file generated successfully: " + fileName);
//        } catch (IOException e) {
//            throw new RuntimeException("Error while generating Excel file", e);
//        }
//    }
//
//
//

    public void generateMonthlyKwhReportExcel(String fileName, Map<String, List<MonthlyKwhReportDTO>> reportData, String fromMonthYear, String toMonthYear, String tenantName) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Monthly kWh Report");

            int rowNum = 0;

            // Date format setup
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM");
            SimpleDateFormat outputFormat = new SimpleDateFormat("MM-yyyy");

            // Iterate over each table and generate its data
            for (Map.Entry<String, List<MonthlyKwhReportDTO>> entry : reportData.entrySet()) {
                String tableName = entry.getKey();
                List<MonthlyKwhReportDTO> report = entry.getValue();

                // Create title row
                Row titleRow = sheet.createRow(rowNum++);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("Monthly kWh Report of " + tableName + " - " + tenantName);
                titleCell.setCellStyle(createTitleCellStyle(workbook));
                sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

                // Create subtitle row
                Row subtitleRow = sheet.createRow(rowNum++);
                Cell subtitleCell = subtitleRow.createCell(0);
                subtitleCell.setCellValue("From: " + fromMonthYear + "  To: " + toMonthYear);
                subtitleCell.setCellStyle(createSubtitleCellStyle(workbook));
                sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

                // Create header row
                Row headerRow = sheet.createRow(rowNum++);
                String[] headers = {"Month", "Monthly kWh"};
                CellStyle headerStyle = createHeaderCellStyle(workbook);

                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Fill in the data rows
                CellStyle dataStyle = createDataCellStyle(workbook);
                for (MonthlyKwhReportDTO dto : report) {
                    Row row = sheet.createRow(rowNum++);
                    String formattedMonth = dto.getMonth();
                    try {
                        formattedMonth = outputFormat.format(inputFormat.parse(dto.getMonth()));
                    } catch (ParseException e) {
                        // Handle the exception (e.g., log it or use the original format as a fallback)
                        formattedMonth = dto.getMonth();
                    }

                    Cell monthCell = row.createCell(0);
                    monthCell.setCellValue(formattedMonth);
                    monthCell.setCellStyle(dataStyle);

                    Cell kwhCell = row.createCell(1);
                    kwhCell.setCellValue(dto.getMonthlyKwh());
                    kwhCell.setCellStyle(dataStyle);
                }

                // Add space between tables
                rowNum++;
            }

            // Autosize columns
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            // Write the Excel file to disk
            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                workbook.write(fos);
            }

            System.out.println("Excel file generated successfully: " + fileName);
        } catch (IOException e) {
            throw new RuntimeException("Error while generating Excel file", e);
        }
    }


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
}
