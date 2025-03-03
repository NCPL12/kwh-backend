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
public class LoginService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Optional<Map<String, String>> verifyUser(String username, String password) {
        String sql = "SELECT username, role, password FROM [user] WHERE username = ?";

        try {
            Map<String, String> userDetails = jdbcTemplate.queryForObject(
                    sql,
                    new Object[]{username},
                    (ResultSet rs, int rowNum) -> {
                        Map<String, String> map = new HashMap<>();
                        map.put("username", rs.getString("username"));
                        map.put("role", rs.getString("role"));
                        map.put("password", rs.getString("password")); // Stored password
                        return map;
                    }
            );

            if (userDetails != null) {
                String storedHash = userDetails.get("password");
                String hashedPassword = hashPassword(password);

                // log.info("Stored password: " + storedHash);
              //  log.info("Hashed input password: " + hashedPassword);

                if (hashedPassword.equals(storedHash)) {
                    userDetails.remove("password"); // Don't send password back
                    return Optional.of(userDetails);
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            e.printStackTrace(); // Log actual SQL errors
            return Optional.empty();
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
