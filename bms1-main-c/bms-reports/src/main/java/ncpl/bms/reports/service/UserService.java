package ncpl.bms.reports.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import ncpl.bms.reports.model.dto.UserDTO;

@Service

public class UserService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean addUser(UserDTO userDTO) {
        String hashedPassword = hashPassword(userDTO.getPassword());
        String sql = "INSERT INTO [user] (username, role, password, full_name, phone_number) VALUES (?, ?, ?, ?, ?);";

        try {
            int rows = jdbcTemplate.update(sql, userDTO.getUsername(), userDTO.getRole(), hashedPassword, userDTO.getFullName(), userDTO.getPhoneNumber());
            return rows > 0;
        } catch (Exception e) {
            return false;
        }
    }
    public List<UserDTO> getUsers() {
        String sql = "SELECT id, username, role, full_name, phone_number, is_deleted FROM [user];"; // Excluding password

        return jdbcTemplate.query(sql, (rs, rowNum) -> new UserDTO(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("role"),
                null, // Exclude password
                rs.getString("full_name"),
                rs.getString("phone_number"),
                rs.getBoolean("is_deleted")
        ));
    }

    public boolean updateUser(int id, UserDTO userDTO) {
        String sql = "UPDATE [user] SET username = ?, role = ?, full_name = ?, phone_number = ? WHERE id = ?";


        try {
            int rows = jdbcTemplate.update(sql, userDTO.getUsername(), userDTO.getRole(), userDTO.getFullName(), userDTO.getPhoneNumber(), id);
            return rows > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ✅ Delete User
    public boolean deleteUser(int id) {
        String sql = "UPDATE [user] SET is_deleted = 1 WHERE id = ?";

        try {
            int rows = jdbcTemplate.update(sql, id);
            return rows > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updatePassword(int id, String newPassword) {
        String hashedPassword = hashPassword(newPassword);
        String sql = "UPDATE [user] SET password = ? WHERE id = ?";

        try {
            int rows = jdbcTemplate.update(sql, hashedPassword, id);
            return rows > 0;
        } catch (Exception e) {
            // Log the error
            return false;
        }
    }





    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedPassword = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedPassword) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
        //return "password";

    }
}
