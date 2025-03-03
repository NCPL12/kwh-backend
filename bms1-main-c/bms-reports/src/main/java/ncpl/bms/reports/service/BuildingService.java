package ncpl.bms.reports.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ncpl.bms.reports.model.dto.BuildingDTO;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class BuildingService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public BuildingDTO getBuildingById(Integer id) {
        String sql = "SELECT * FROM building_details WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, this::mapRowToBuildingDTO);
    }

    public BuildingDTO updateBuilding(Integer id, BuildingDTO buildingDTO) {
        String sql = "UPDATE building_details SET building_name = ?, address = ?, total_area = ?, email = ?, " +
                "person_of_contact = ?, phone_number = ?, account_name = ?, account_number = ?, ifsc_code = ?, " +
                "schedule = ?, send_mail = ? WHERE id = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                buildingDTO.getBuildingName(),
                buildingDTO.getAddress(),
                buildingDTO.getTotalArea(),
                buildingDTO.getEmail(),
                buildingDTO.getPersonOfContact(),
                buildingDTO.getPhoneNumber(),
                buildingDTO.getAccountName(),
                buildingDTO.getAccountNumber(),
                buildingDTO.getIfscCode(),
                buildingDTO.getSchedule(),
                buildingDTO.getSendMail(),
                id
        );

        // Return updated object if update was successful
        return rowsAffected > 0 ? getBuildingById(id) : null;
    }

    private BuildingDTO mapRowToBuildingDTO(ResultSet rs, int rowNum) throws SQLException {
        BuildingDTO building = new BuildingDTO();
        building.setId(rs.getInt("id"));
        building.setBuildingName(rs.getString("building_name"));
        building.setAddress(rs.getString("address"));
        building.setTotalArea(rs.getDouble("total_area"));
        building.setEmail(rs.getString("email"));
        building.setPersonOfContact(rs.getString("person_of_contact"));
        building.setPhoneNumber(rs.getString("phone_number"));
        building.setAccountName(rs.getString("account_name"));
        building.setAccountNumber(rs.getString("account_number"));
        building.setIfscCode(rs.getString("ifsc_code"));
        building.setSchedule(rs.getBoolean("schedule"));
        building.setSendMail(rs.getBoolean("send_mail"));
        return building;
    }
}
