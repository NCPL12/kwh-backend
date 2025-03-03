package ncpl.bms.reports.service;

import ncpl.bms.reports.model.dto.ScheduledBillDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ScheduledBillService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BillingService billingService;

    @Autowired
    private EmailService emailService;

    private String getTenantEmail(int tenantId) {
        String sql = "SELECT email FROM kwh_data_ai_generated.dbo.tenant WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, tenantId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void generateAndProcessMonthlyBills() {
        boolean isScheduled = isBillingScheduled();
        boolean isSendMail = isMailScheduled();

        if (!isScheduled && !isSendMail) {
            System.out.println("❌ Neither billing nor email is scheduled. Skipping bill generation.");
            return;
        }

        System.out.println("✅ Processing billing based on configuration...");

        String getTenantsSql = "SELECT DISTINCT id FROM kwh_data_ai_generated.dbo.tenant WHERE is_deleted = 0 AND id != 1;";
        List<Integer> tenantIds = jdbcTemplate.query(getTenantsSql, (rs, rowNum) -> rs.getInt("id"));

        LocalDate previousMonth = LocalDate.now().minusMonths(1);
        String billPeriod = previousMonth.format(DateTimeFormatter.ofPattern("MMMM-yyyy"));

        for (Integer tenantId : tenantIds) {
            try {
                byte[] pdfContent = billingService.generateBillPdf(Long.valueOf(tenantId),
                        String.valueOf(previousMonth.getMonthValue()),
                        String.valueOf(previousMonth.getYear()));

                // Execute both storing and emailing independently if both are enabled
                if (isScheduled) {
                    storeBillInDatabase(tenantId, billPeriod, pdfContent);
                }

                if (isSendMail) {
                    sendBillByEmail(tenantId, billPeriod, pdfContent);
                }

            } catch (Exception e) {
                System.err.println("❌ Error generating bill for tenant " + tenantId + ": " + e.getMessage());
            }
        }
    }

    private void storeBillInDatabase(Integer tenantId, String billPeriod, byte[] pdfContent) {
        String insertSql = "INSERT INTO kwh_data_ai_generated.dbo.scheduled_billing " +
                "(bill_name, tenant_id, type, periods, auto_generate, pdf_content, generated_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(insertSql,
                "Monthly Bill - " + billPeriod,
                tenantId,
                "MONTHLY",
                billPeriod,
                true,
                pdfContent,
                LocalDateTime.now());

        System.out.println("✅ Bill stored for tenant: " + tenantId);
    }

    private void sendBillByEmail(Integer tenantId, String billPeriod, byte[] pdfContent) {
        String tenantEmail = getTenantEmail(tenantId);
        if (tenantEmail != null && !tenantEmail.isEmpty()) {
            emailService.sendBillEmail(
                    tenantEmail,
                    "Your Monthly Bill - " + billPeriod,
                    "Dear Tenant,\n\nPlease find attached your monthly bill for " + billPeriod + ".\n\nBest Regards,\nBilling Team",
                    pdfContent,
                    "Monthly_Bill_" + billPeriod + ".pdf"
            );
            System.out.println("✅ Email sent to tenant: " + tenantId);
        } else {
            System.out.println("⚠️ No email found for tenant " + tenantId);
        }
    }

    private boolean isBillingScheduled() {
        String sql = "SELECT schedule FROM building_details WHERE id = 1";
        try {
            Integer scheduleStatus = jdbcTemplate.queryForObject(sql, Integer.class);
            return scheduleStatus != null && scheduleStatus == 1;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    private boolean isMailScheduled() {
        String sql = "SELECT send_mail FROM building_details WHERE id = 1";
        try {
            Integer mailStatus = jdbcTemplate.queryForObject(sql, Integer.class);
            return mailStatus != null && mailStatus == 1;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Scheduled(cron = "0 * * * * ?") // Run at 7 AM on the 1st of every month(0 7 1 * *)
    public void scheduleMonthlyBillGeneration() {
        generateAndProcessMonthlyBills();
    }

    public List<ScheduledBillDTO> getAllScheduledBills() {
        String sql = "SELECT id, bill_name, generated_date, tenant_id, type, periods, pdf_content, auto_generate FROM kwh_data_ai_generated.dbo.scheduled_billing";
        return jdbcTemplate.query(sql, billRowMapper);
    }

    public ScheduledBillDTO getScheduledBillById(int id) {
        try {
            String sql = "SELECT id, bill_name, generated_date, tenant_id, type, periods, pdf_content, auto_generate FROM kwh_data_ai_generated.dbo.scheduled_billing WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, billRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public byte[] getScheduledBillPdfById(int id) {
        try {
            String sql = "SELECT pdf_content FROM kwh_data_ai_generated.dbo.scheduled_billing WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> rs.getBytes("pdf_content"), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private final RowMapper<ScheduledBillDTO> billRowMapper = (rs, rowNum) -> {
        ScheduledBillDTO bill = new ScheduledBillDTO();
        bill.setId(rs.getInt("id"));
        bill.setBill_name(rs.getString("bill_name"));
        bill.setGenerated_date(rs.getTimestamp("generated_date").toLocalDateTime());
        bill.setTenant_id(rs.getInt("tenant_id"));
        bill.setType(rs.getString("type"));
        bill.setPeriods(rs.getString("periods"));
        bill.setPdf_content(rs.getBytes("pdf_content"));
        return bill;
    };
}
