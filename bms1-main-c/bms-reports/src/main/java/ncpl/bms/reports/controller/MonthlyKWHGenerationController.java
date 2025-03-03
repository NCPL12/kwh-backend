package ncpl.bms.reports.controller;
import lombok.extern.slf4j.Slf4j;
import ncpl.bms.reports.service.MonthlyKWHGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ncpl.bms.reports.model.dto.MonthlyKwhReportDTO;

@RestController
@RequestMapping("v1")
@CrossOrigin(origins = "http://localhost:4200")
//@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@Slf4j
public class MonthlyKWHGenerationController {

    @Autowired
    private MonthlyKWHGenerationService monthlyKWHGenerationService;

    @GetMapping("/monthly-kwh-data")
    public List<MonthlyKwhReportDTO> generateMonthlyKwhData(
            @RequestParam List<String> tableNames,
            @RequestParam String fromMonthYear,
            @RequestParam String toMonthYear) {
        return monthlyKWHGenerationService.generateMonthlyKwhReport(tableNames, fromMonthYear, toMonthYear);
    }

    @GetMapping("/monthly-kwh-report-pdf-file")
    public ResponseEntity<byte[]> getMonthlyKwhReportPdf(
            @RequestParam List<String> tableNames,
            @RequestParam String fromMonthYear,
            @RequestParam String toMonthYear,
            @RequestParam String tenantName) {

        // Generate and export the PDF
        String pdfFileName = "monthly_kwh_report.pdf";
        monthlyKWHGenerationService.generateAndExportMonthlyKwhReport(tableNames, fromMonthYear, toMonthYear, tenantName);

        try {
            // Read the generated PDF into a byte array
            File pdfFile = new File(pdfFileName);
            FileInputStream fileInputStream = new FileInputStream(pdfFile);
            byte[] pdfBytes = fileInputStream.readAllBytes();
            fileInputStream.close();

            // Prepare HTTP response headers for PDF download
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=monthly_kwh_report.pdf");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");

            // Return the PDF as a response
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException("Error while reading the PDF file", e);
        }
    }

    @GetMapping("/monthly-kwh-report-excel-file")
    public ResponseEntity<byte[]> getMonthlyKwhReportExcel(
            @RequestParam List<String> tableNames,
            @RequestParam String fromMonthYear,
            @RequestParam String toMonthYear,
            @RequestParam String tenantName) {

        // Generate and export the Excel file
        String excelFileName = "monthly_kwh_report.xlsx";
        monthlyKWHGenerationService.exportMonthlyKwhReportExcel(tableNames, fromMonthYear, toMonthYear , tenantName);

        try {
            // Read the generated Excel file into a byte array
            File excelFile = new File(excelFileName);
            FileInputStream fileInputStream = new FileInputStream(excelFile);
            byte[] excelBytes = fileInputStream.readAllBytes();
            fileInputStream.close();

            // Prepare HTTP response headers for Excel download
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=monthly_kwh_report.xlsx");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            // Return the Excel file as a response
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException("Error while reading the Excel file", e);
        }
    }


}
