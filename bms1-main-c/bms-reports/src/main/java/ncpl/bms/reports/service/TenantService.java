package ncpl.bms.reports.service;
import org.springframework.stereotype.Service;
import ncpl.bms.reports.model.dto.TenantDTO;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import ncpl.bms.reports.model.dto.TenantDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;



@Service
public class TenantService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void deleteTenant(Integer id) {
        String sql = "UPDATE tenant SET is_deleted = 1 WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public TenantDTO addTenant(TenantDTO tenantDTO) {
        String sql = "INSERT INTO tenant (name, address, person_of_contact, mobile_number, unit_address, email, area_occupied) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                tenantDTO.getName(),
                tenantDTO.getAddress(),
                tenantDTO.getPersonOfContact(),
                tenantDTO.getMobileNumber(),
                tenantDTO.getUnitAddress(),
                tenantDTO.getEmail(),
                tenantDTO.getAreaOccupied()
        );
        return tenantDTO;
    }

    public TenantDTO updateTenant(Integer id, TenantDTO tenantDTO) {
        String sql = "UPDATE tenant SET name = ?, address = ?, person_of_contact = ?, mobile_number = ?, unit_address = ?, email = ?, area_occupied = ? WHERE id = ?";
        jdbcTemplate.update(
                sql,
                tenantDTO.getName(),
                tenantDTO.getAddress(),
                tenantDTO.getPersonOfContact(),
                tenantDTO.getMobileNumber(),
                tenantDTO.getUnitAddress(),
                tenantDTO.getEmail(),
                tenantDTO.getAreaOccupied(),
                id
        );
        tenantDTO.setId(id);
        return tenantDTO;
    }

    public List<TenantDTO> getAllTenants() {
        String sql = "SELECT id, name, address, person_of_contact, mobile_number, unit_address,area_occupied, email, is_deleted FROM tenant";
        return jdbcTemplate.query(sql, new RowMapper<TenantDTO>() {
            @Override
            public TenantDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new TenantDTO(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("person_of_contact"),
                        rs.getString("mobile_number")  ,
                        rs.getString("unit_address")  ,
                        rs.getInt("area_occupied"),
                        rs.getString("email"),
                        rs.getBoolean("is_deleted")
                );
            }
        });
    }
}
