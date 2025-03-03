package ncpl.bms.reports.controller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import ncpl.bms.reports.service.ManualBillingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("v1")
@CrossOrigin(origins = "http://localhost:4200")
//@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@Slf4j
public class ManualBillingController {

    @Autowired
    private ManualBillingService manualBillingService;

    @GetMapping("/export-manual-bill")
    public ResponseEntity<byte[]> exportBill(@RequestParam("tenantId") Long tenantId, @RequestParam("fromDate") String fromDate, @RequestParam("toDate") String toDate) {

        byte[] pdfContent = manualBillingService.generateManualBillPdf(tenantId, fromDate, toDate);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=bill.pdf")
                .body(pdfContent);
    }
}