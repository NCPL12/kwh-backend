package ncpl.bms.reports.service;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.*;
import java.io.IOException;
import java.util.List;
import ncpl.bms.reports.model.dto.MonthlyKwhReportDTO;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.*;
import com.itextpdf.layout.element.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@Component
@Slf4j
public class MonthlyKWHGenerationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MonthlyKWHExcelService monthlyKWHExcelService;

    @Autowired
    private PageNumberEventHandler pageNumberEventHandler;





    public List<MonthlyKwhReportDTO> generateMonthlyKwhReport(List<String> tableNames, String fromMonthYear, String toMonthYear) {
        if (!fromMonthYear.matches("\\d{4}-\\d{2}") || !toMonthYear.matches("\\d{4}-\\d{2}")) {
            throw new IllegalArgumentException("Month and Year must be in YYYY-MM format");
        }

        String fromTimestamp = fromMonthYear + "-01 00:00:00";
        String toTimestamp = calculateLastDayOfMonth(toMonthYear) + " 23:59:59";

        List<MonthlyKwhReportDTO> combinedReports = new ArrayList<>();

        for (String tableName : tableNames) {
            String sql = String.format(
                    "SELECT t1.month, " +
                            "       t1.start_kwh, " +
                            "       t2.kwh AS next_month_kwh " +
                            "FROM ( " +
                            "    SELECT FORMAT(timestamp, 'yyyy-MM') AS month, " + // Format timestamp to YYYY-MM
                            "           MIN(kwh) AS start_kwh " +
                            "    FROM %s " +
                            "    WHERE timestamp BETWEEN ? AND ? " +
                            "    GROUP BY FORMAT(timestamp, 'yyyy-MM') " +
                            ") t1 " +
                            "LEFT JOIN %s t2 " +
                            "ON CAST(t2.timestamp AS DATE) = DATEADD(MONTH, 1, DATEFROMPARTS(LEFT(t1.month, 4), RIGHT(t1.month, 2), 1)) " +
                            "AND CONVERT(TIME, t2.timestamp) = '00:00:00' " +
                            "ORDER BY t1.month",
                    tableName, tableName
            );

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, fromTimestamp, toTimestamp);

            for (Map<String, Object> row : rows) {
                String month = row.get("month").toString();
                Double startKwh = row.get("start_kwh") != null ? Double.parseDouble(row.get("start_kwh").toString()) : null;
                Double nextMonthKwh = row.get("next_month_kwh") != null ? Double.parseDouble(row.get("next_month_kwh").toString()) : null;

                if (startKwh != null && nextMonthKwh != null) {
                    double monthlyKwh = nextMonthKwh - startKwh;
                    BigDecimal roundedMonthlyKwh = BigDecimal.valueOf(monthlyKwh).setScale(2, RoundingMode.HALF_UP);

                    combinedReports.add(new MonthlyKwhReportDTO(month, roundedMonthlyKwh.doubleValue(), tableName));
                }
            }
        }

        return combinedReports;
    }


    private String calculateLastDayOfMonth(String monthYear) {
        try {
            String[] parts = monthYear.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);

            // Determine the number of days in the month
            int lastDay;
            switch (month) {
                case 4: case 6: case 9: case 11:
                    lastDay = 30;
                    break;
                case 2:
                    // Check for leap year
                    boolean isLeapYear = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
                    lastDay = isLeapYear ? 29 : 28;
                    break;
                default:
                    lastDay = 31;
                    break;
            }

            // Return the last day of the month in YYYY-MM-DD format
            return String.format("%d-%02d-%02d", year, month, lastDay);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid month-year format. Expected YYYY-MM.", e);
        }
    }

    public void generateAndExportMonthlyKwhReport(List<String> tableNames, String fromMonthYear, String toMonthYear,String tenantName) {
        Map<String, List<MonthlyKwhReportDTO>> reportData = new HashMap<>();

        for (String tableName : tableNames) {
            List<MonthlyKwhReportDTO> report = generateMonthlyKwhReport(Collections.singletonList(tableName), fromMonthYear, toMonthYear);
            reportData.put(tableName, report);
        }

        String outputFileName = "monthly_kwh_report.pdf";
        generateMonthlyKwhReportPdf(outputFileName, reportData, fromMonthYear, toMonthYear, tenantName);
    }

    public void generateMonthlyKwhReportPdf(String fileName, Map<String, List<MonthlyKwhReportDTO>> reportData, String fromMonthYear, String toMonthYear, String tenantName) {
        try {
            PdfWriter writer = new PdfWriter(fileName);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, pageNumberEventHandler);

            Paragraph title = new Paragraph("Monthly kWh Report - " + tenantName)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(18);
            document.add(title);

            Paragraph subheading = new Paragraph("From: " + fromMonthYear + "  To: " + toMonthYear)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12)
                    .setMarginBottom(20);
            document.add(subheading);

            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM");
            SimpleDateFormat outputFormat = new SimpleDateFormat("MM-yyyy");

            for (Map.Entry<String, List<MonthlyKwhReportDTO>> entry : reportData.entrySet()) {
                String tableName = entry.getKey();
                List<MonthlyKwhReportDTO> report = entry.getValue();

                Paragraph tableHeader = new Paragraph("Meter: " + tableName)
                        .setBold()
                        .setFontSize(14)
                        .setMarginTop(20);
                document.add(tableHeader);

                Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                        .setWidth(UnitValue.createPercentValue(100))
                        .setMarginTop(10);

                table.addHeaderCell(new Cell().add(new Paragraph("Month").setBold().setFontSize(12).setTextAlignment(TextAlignment.CENTER)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                table.addHeaderCell(new Cell().add(new Paragraph("Monthly kWh").setBold().setFontSize(12).setTextAlignment(TextAlignment.CENTER)).setBackgroundColor(ColorConstants.LIGHT_GRAY));

                for (MonthlyKwhReportDTO dto : report) {
                    String formattedMonth = dto.getMonth();
                    try {
                        formattedMonth = outputFormat.format(inputFormat.parse(dto.getMonth()));
                    } catch (ParseException e) {
                        // Handle the exception (e.g., log it or set a fallback value)
                        formattedMonth = dto.getMonth(); // Use original if parsing fails
                    }
                    table.addCell(new Cell().add(new Paragraph(formattedMonth).setTextAlignment(TextAlignment.CENTER)));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.2f", dto.getMonthlyKwh())).setTextAlignment(TextAlignment.RIGHT)));
                }

                document.add(table);
            }

            document.close();
        } catch (IOException e) {
            throw new RuntimeException("Error while generating PDF", e);
        }
    }

    public void exportMonthlyKwhReportExcel(List<String> tableNames, String fromMonthYear, String toMonthYear , String tenantName) {
        // Map to store reports by table
        Map<String, List<MonthlyKwhReportDTO>> reportData = new HashMap<>();

        // Generate reports for each table
        for (String tableName : tableNames) {
            List<MonthlyKwhReportDTO> report = generateMonthlyKwhReport(Collections.singletonList(tableName), fromMonthYear, toMonthYear);
            reportData.put(tableName, report);
        }

        // Specify output Excel file path
        String outputFileName = "monthly_kwh_report.xlsx";

        // Generate the Excel file with all table data
        monthlyKWHExcelService.generateMonthlyKwhReportExcel(outputFileName, reportData, fromMonthYear, toMonthYear, tenantName);
    }


}