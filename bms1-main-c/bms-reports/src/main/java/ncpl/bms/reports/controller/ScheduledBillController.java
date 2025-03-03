package ncpl.bms.reports.controller;

import ncpl.bms.reports.service.ScheduledBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ncpl.bms.reports.model.dto.ScheduledBillDTO;
import org.springframework.http.HttpHeaders;

import java.util.List;

@RestController
@RequestMapping("v1/scheduled-bills")
@CrossOrigin(origins = "http://localhost:4200")
public class ScheduledBillController {

    @Autowired
    private ScheduledBillService scheduledBillService;

    // ✅ Trigger manual bill generation
    @PostMapping("/generate")
    public ResponseEntity<String> generateBillsManually() {
        try {
            System.out.println("🔄 Manually triggering bill generation...");
            scheduledBillService.generateAndProcessMonthlyBills(); // 🔄 Using the correct method
            System.out.println("✅ Manual bill generation triggered.");
            return ResponseEntity.ok("Bill generation triggered successfully.");
        } catch (Exception e) {
            System.err.println("❌ Error in manual bill generation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // ✅ Get all scheduled bills (Auto-generated)
    @GetMapping("/all-auto")
    public ResponseEntity<List<ScheduledBillDTO>> getAllScheduledBills() {
        List<ScheduledBillDTO> bills = scheduledBillService.getAllScheduledBills();
        return ResponseEntity.ok(bills);
    }

    // ✅ View scheduled bill PDF in browser
    @GetMapping("/{id}/pdf-view")
    public ResponseEntity<ByteArrayResource> viewScheduledBillPdf(@PathVariable int id) {
        try {
            byte[] pdfBytes = scheduledBillService.getScheduledBillPdfById(id);
            if (pdfBytes == null || pdfBytes.length == 0) {
                System.out.println("⚠️ No PDF found for bill ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            ByteArrayResource resource = new ByteArrayResource(pdfBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=scheduled-bill-" + id + ".pdf");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");

            System.out.println("✅ PDF viewed for bill ID: " + id);
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(pdfBytes.length)
                    .body(resource);
        } catch (RuntimeException e) {
            System.err.println("❌ Error viewing PDF for bill ID: " + id + " | " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // ✅ Download scheduled bill PDF
    @GetMapping("/{id}/pdf-download")
    public ResponseEntity<byte[]> downloadScheduledBillPdf(@PathVariable int id) {
        try {
            byte[] pdfContent = scheduledBillService.getScheduledBillPdfById(id);
            if (pdfContent == null || pdfContent.length == 0) {
                System.out.println("⚠️ No PDF found for bill ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=scheduled-bill-" + id + ".pdf");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");

            System.out.println("✅ PDF downloaded for bill ID: " + id);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfContent);
        } catch (RuntimeException e) {
            System.err.println("❌ Error downloading PDF for bill ID: " + id + " | " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
