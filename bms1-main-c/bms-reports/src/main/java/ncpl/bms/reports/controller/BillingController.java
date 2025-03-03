package ncpl.bms.reports.controller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import ncpl.bms.reports.service.BillingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("v1")
@CrossOrigin(origins = "http://localhost:4200")
//@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@Slf4j
public class BillingController {
    @Autowired
    private BillingService billingService;

    @GetMapping("/export-bill")
    public ResponseEntity<byte[]> exportBill(@RequestParam("tenantId") Long tenantId,
                                             @RequestParam("month") String month,
                                             @RequestParam("year") String year) {


        byte[] pdfContent = billingService.generateBillPdf(tenantId, month, year);

        // Return the PDF as a response
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=bill.pdf")
                .body(pdfContent);
    }
}
