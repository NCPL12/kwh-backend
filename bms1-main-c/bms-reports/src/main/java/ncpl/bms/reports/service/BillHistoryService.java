package ncpl.bms.reports.service;

import ncpl.bms.reports.model.dto.BillingHistoryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BillHistoryService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public BillingHistoryDTO addBillHistory(BillingHistoryDTO bill) {
        String sql = "INSERT INTO billing_history " +
                "(bill_name, generated_date, tenant_id, type, periods, pdf_content) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql,
                    bill.getBill_name(),
                    bill.getGenerated_date(),
                    bill.getTenant_id(),
                    bill.getType(),
                    bill.getPeriods(),
                    bill.getPdf_content());
        } catch (Exception e) {
            throw new RuntimeException("Error storing PDF as VARBINARY: " + e.getMessage(), e);
        }
        return bill;
    }

    public byte[] getBillHistoryPdfById(int id) {
        String sql = "SELECT pdf_content FROM billing_history WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> rs.getBytes("pdf_content"), id);
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("PDF not found for Bill ID: " + id);
        }
    }

    public BillingHistoryDTO getBillHistoryById(int id) {
        String sql = "SELECT id, bill_name, generated_date, tenant_id, type, periods, pdf_content FROM billing_history WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                BillingHistoryDTO bill = new BillingHistoryDTO();
                bill.setId(rs.getInt("id"));
                bill.setBill_name(rs.getString("bill_name"));
                bill.setGenerated_date(rs.getTimestamp("generated_date").toLocalDateTime());
                bill.setTenant_id(rs.getInt("tenant_id"));
                bill.setType(rs.getString("type"));
                bill.setPeriods(rs.getString("periods"));
                bill.setPdf_content(rs.getBytes("pdf_content"));
                return bill;
            }, id);
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("Bill history not found with ID: " + id);
        }
    }

    public List<BillingHistoryDTO> getAllBillHistories() {
        String sql = "SELECT id, bill_name, generated_date, tenant_id, type, periods FROM billing_history";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            BillingHistoryDTO bill = new BillingHistoryDTO();
            bill.setId(rs.getInt("id"));
            bill.setBill_name(rs.getString("bill_name"));
            bill.setGenerated_date(rs.getTimestamp("generated_date").toLocalDateTime());
            bill.setTenant_id(rs.getInt("tenant_id"));
            bill.setType(rs.getString("type"));
            bill.setPeriods(rs.getString("periods"));
            return bill;
        });
    }
}
