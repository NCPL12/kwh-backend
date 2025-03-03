package ncpl.bms.reports.service;

import ncpl.bms.reports.model.dto.TenantDTO;
import ncpl.bms.reports.model.dto.MonthlyKwhReportDTO;
import ncpl.bms.reports.model.dto.TenantEnergyMeterDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TenantAndEnergyMeterService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MonthlyKWHGenerationService monthlyKWHGenerationService;

    @Autowired
    private DailyKWHGenerationService dailyKWHGenerationService;

    /**
     * Fetches tenant details by ID.
     */
    public TenantDTO getTenantDetailsById(int tenantId) {
        try {
            String sql = "SELECT name, address FROM tenant WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, new Object[]{tenantId}, (rs, rowNum) -> {
                TenantDTO tenant = new TenantDTO();
                tenant.setName(rs.getString("name"));
                tenant.setAddress(rs.getString("address"));
                return tenant;
            });
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("Tenant not found with ID: " + tenantId);
        }
    }

    /**
     * Retrieves all active energy meters.
     */
    public List<TenantEnergyMeterDTO> getAllActiveEnergyMeters() {
        String sql = "SELECT id, name, tenant_id FROM tenant_to_energy_meter_relation";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            TenantEnergyMeterDTO dto = new TenantEnergyMeterDTO();
            dto.setId(rs.getInt("id"));
            dto.setName(rs.getString("name"));
            dto.setTenantId(rs.getObject("tenant_id", Integer.class));
            return dto;
        });
    }

    /**
     * Adds a new energy meter and returns the inserted record.
     */
    public TenantEnergyMeterDTO addEnergyMeter(TenantEnergyMeterDTO energyMeter) {
        String sql = "INSERT INTO tenant_to_energy_meter_relation (name, tenant_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, energyMeter.getName(), energyMeter.getTenantId());

        // Fetch the last inserted row using TOP 1 instead of LIMIT
        String fetchSql = "SELECT TOP 1 id, name, tenant_id FROM tenant_to_energy_meter_relation ORDER BY id DESC";
        return jdbcTemplate.queryForObject(fetchSql, (rs, rowNum) -> {
            TenantEnergyMeterDTO dto = new TenantEnergyMeterDTO();
            dto.setId(rs.getInt("id"));
            dto.setName(rs.getString("name"));
            dto.setTenantId(rs.getObject("tenant_id", Integer.class));
            return dto;
        });
    }

    /**
     * Updates an existing energy meter.
     */
    public void updateEnergyMeter(TenantEnergyMeterDTO energyMeter) {
        String sql = "UPDATE tenant_to_energy_meter_relation SET name = ?, tenant_id = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, energyMeter.getName(), energyMeter.getTenantId(), energyMeter.getId());
        if (rowsAffected == 0) {
            throw new RuntimeException("Energy meter not found with ID: " + energyMeter.getId());
        }
    }

    /**
     * Deletes an energy meter by ID.
     */
    public void deleteEnergyMeterById(int id) {
        String sql = "DELETE FROM tenant_to_energy_meter_relation WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        if (rowsAffected == 0) {
            throw new RuntimeException("Energy meter not found with ID: " + id);
        }
    }

    /**
     * Fetches available energy meter names from the database.
     */
    public List<String> getAvailableEnergyMeterNames() {
        String sql = "SELECT table_name FROM information_schema.tables WHERE table_name LIKE '%_dg_kwh' OR table_name LIKE '%_eb_kwh'";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    /**
     * Retrieves energy meters assigned to a tenant and calculates energy usage.
     */
    public Map<String, Double> getEnergyMetersForTenant(int tenantId, String month, String year) {
        String sql = "SELECT name FROM tenant_to_energy_meter_relation WHERE tenant_id = ?";
        List<String> energyMeters = jdbcTemplate.query(sql, new Object[]{tenantId}, (rs, rowNum) -> rs.getString("name"));

        // Separate energy meters into EB and DG categories
        List<String> ebMeters = energyMeters.stream()
                .filter(name -> name.endsWith("_eb_kwh"))
                .collect(Collectors.toList());

        List<String> dgMeters = energyMeters.stream()
                .filter(name -> name.endsWith("_dg_kwh"))
                .collect(Collectors.toList());

        // Convert month and year to YYYY-MM format
        String monthYear = year + "-" + String.format("%02d", Integer.parseInt(month));

        // Fetch data for EB meters
        double totalEbKwh = 0;
        if (!ebMeters.isEmpty()) {
            List<MonthlyKwhReportDTO> ebData = monthlyKWHGenerationService.generateMonthlyKwhReport(ebMeters, monthYear, monthYear);
            totalEbKwh = ebData.stream().mapToDouble(MonthlyKwhReportDTO::getMonthlyKwh).sum();
        }

        // Fetch data for DG meters
        double totalDgKwh = 0;
        if (!dgMeters.isEmpty()) {
            List<MonthlyKwhReportDTO> dgData = monthlyKWHGenerationService.generateMonthlyKwhReport(dgMeters, monthYear, monthYear);
            totalDgKwh = dgData.stream().mapToDouble(MonthlyKwhReportDTO::getMonthlyKwh).sum();
        }

        // Return the results
        Map<String, Double> energyUsage = new HashMap<>();
        energyUsage.put("totalEbKwh", totalEbKwh);
        energyUsage.put("totalDgKwh", totalDgKwh);
        return energyUsage;
    }

    public Map<String, Double> getEnergyMetersForManualBill(int tenantId, String fromDate, String toDate) {
        // Query to fetch energy meters assigned to the tenant
        String sql = "SELECT name FROM  tenant_to_energy_meter_relation WHERE tenant_id = ?";
        List<String> energyMeters = jdbcTemplate.query(sql, new Object[]{tenantId}, (rs, rowNum) -> rs.getString("name"));

        // Separate energy meters into EB and DG categories
        List<String> ebMeters = energyMeters.stream()
                .filter(name -> name.endsWith("_eb_kwh"))
                .toList();

        List<String> dgMeters = energyMeters.stream()
                .filter(name -> name.endsWith("_dg_kwh"))
                .toList();

        // Fetch data for EB meters
        double totalEbKwh = 0;
        if (!ebMeters.isEmpty()) {
            double ebData = dailyKWHGenerationService.getTotalDailyKwhSum(ebMeters, fromDate, toDate);
            totalEbKwh = ebData;
        }

        // Fetch data for DG meters
        double totalDgKwh = 0;
        if (!dgMeters.isEmpty()) {
            double dgData = dailyKWHGenerationService.getTotalDailyKwhSum(dgMeters, fromDate, toDate);
            totalDgKwh = dgData;
        }

        Map<String, Double> energyUsage = new HashMap<>();
        energyUsage.put("totalEbKwh", totalEbKwh);
        energyUsage.put("totalDgKwh", totalDgKwh);

        return energyUsage;
    }


}
