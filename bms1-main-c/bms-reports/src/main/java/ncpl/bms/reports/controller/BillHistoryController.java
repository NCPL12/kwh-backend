package ncpl.bms.reports.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.io.exceptions.IOException;
import ncpl.bms.reports.model.dto.BillingHistoryDTO;
import ncpl.bms.reports.service.BillHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.ByteArrayResource;
import java.util.List;

@RestController
@RequestMapping("v1")
@CrossOrigin(origins = "http://localhost:4200")
public class BillHistoryController {

    @Autowired
    private BillHistoryService billHistoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/all-bill-histories")
    public ResponseEntity<List<BillingHistoryDTO>> getAllBillHistories() {
        try {
            List<BillingHistoryDTO> billHistories = billHistoryService.getAllBillHistories();
            return ResponseEntity.ok(billHistories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/bill-history-by-id")
    public ResponseEntity<BillingHistoryDTO> getBillHistoryById(@RequestParam int id) {
        try {
            return ResponseEntity.ok(billHistoryService.getBillHistoryById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/bill-history-pdf/{id}")
    public ResponseEntity<ByteArrayResource> getBillHistoryPdf(@PathVariable int id) {
        try {
            byte[] pdfBytes = billHistoryService.getBillHistoryPdfById(id);
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=bill-history-" + id + ".pdf");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(pdfBytes.length)
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping(value = "/add-bill-history", consumes = "multipart/form-data")
    public ResponseEntity<?> addBillHistory(
            @RequestPart("billHistory") String billHistoryJson,
            @RequestPart("file") MultipartFile file) {
        try {
            BillingHistoryDTO billHistory = objectMapper.readValue(billHistoryJson, BillingHistoryDTO.class);

            if (file != null && !file.isEmpty()) {
                billHistory.setPdf_content(file.getBytes());
            }

            BillingHistoryDTO savedBillHistory = billHistoryService.addBillHistory(billHistory);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedBillHistory);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
