package ncpl.bms.reports.controller;
import lombok.extern.slf4j.Slf4j;
import ncpl.bms.reports.service.DailyKWHGenerationService;
import ncpl.bms.reports.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ncpl.bms.reports.model.dto.TenantDTO;
import ncpl.bms.reports.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("v1")
@CrossOrigin(origins = "http://localhost:4200")
//@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@Slf4j
public class TenantController {


    @Autowired
    private TenantService tenantService;

    @PostMapping("/add-tenant")
    public TenantDTO addTenant(@RequestBody TenantDTO tenantDTO) {
        return tenantService.addTenant(tenantDTO);
    }

    @PutMapping("/update-tenant/{id}")
    public TenantDTO updateTenant(@PathVariable Integer id, @RequestBody TenantDTO tenantDTO) {
        return tenantService.updateTenant(id, tenantDTO);
    }

    @DeleteMapping("/delete-tenant/{id}")
    public ResponseEntity<Map<String, String>> deleteTenant(@PathVariable Integer id) {
        tenantService.deleteTenant(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Tenant deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-all-tenants")
    public List<TenantDTO> getAllTenants() {
        return tenantService.getAllTenants();
    }
}
