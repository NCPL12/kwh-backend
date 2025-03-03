package ncpl.bms.reports.controller;
import lombok.extern.slf4j.Slf4j;
import ncpl.bms.reports.service.DailyKWHGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ncpl.bms.reports.model.dto.DailyKwhReportDTO;

@RestController
@RequestMapping("v1")
@CrossOrigin(origins = "http://localhost:4200")
//@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@Slf4j
public class DailyKWHGenerationController {

    @Autowired
    private DailyKWHGenerationService dailyKWHGenerationService;

    @GetMapping("/daily-kwh-data")
    public List<DailyKwhReportDTO> generateDailyKwhReport(
            @RequestParam List<String> tableNames,
            @RequestParam String fromDate,
            @RequestParam String toDate) {
        return dailyKWHGenerationService.generateDailyKwhReportData(tableNames, fromDate, toDate);
    }

    @GetMapping("/daily-kwh-report-pdf-file")
    public ResponseEntity<byte[]> getDailyKwhReportPdf(
            @RequestParam List<String> tableNames,
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam String tenantName) {

        // Generate and export the PDF
        String pdfFileName = "daily_kwh_report.pdf";
        dailyKWHGenerationService.ExportDailyKwhReportPdf(tableNames, fromDate, toDate, tenantName);

        try {
            // Read the generated PDF into a byte array
            File pdfFile = new File(pdfFileName);
            FileInputStream fileInputStream = new FileInputStream(pdfFile);
            byte[] pdfBytes = fileInputStream.readAllBytes();
            fileInputStream.close();

            // Prepare HTTP response headers for PDF download
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + pdfFileName);
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");

            // Return the PDF as a response
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException("Error while reading the PDF file", e);
        }
    }

    @GetMapping("/daily-kwh-report-excel-file")
    public ResponseEntity<byte[]> getDailyKwhReportExcel(
            @RequestParam List<String> tableNames,
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam String tenantName) {

        // Generate and export the Excel file
        String excelFileName = "daily_kwh_report.xlsx";
        dailyKWHGenerationService.ExportDailyKwhReportExcel(tableNames, fromDate, toDate,  tenantName);

        try {
            // Read the generated Excel file into a byte array
            File excelFile = new File(excelFileName);
            FileInputStream fileInputStream = new FileInputStream(excelFile);
            byte[] excelBytes = fileInputStream.readAllBytes();
            fileInputStream.close();

            // Prepare HTTP response headers for Excel download
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=daily_kwh_report.xlsx");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            // Return the Excel file as a response
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException("Error while reading the Excel file", e);
        }
    }
}