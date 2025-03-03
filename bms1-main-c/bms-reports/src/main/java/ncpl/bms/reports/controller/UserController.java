package ncpl.bms.reports.controller;
import lombok.extern.slf4j.Slf4j;
import ncpl.bms.reports.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ncpl.bms.reports.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ncpl.bms.reports.model.dto.UserDTO;


@RestController
@RequestMapping("v1")
@CrossOrigin(origins = "http://localhost:4200")
//@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/add-user")
    public ResponseEntity<Map<String, String>> addUser(@RequestBody UserDTO userDTO) {
        boolean isUserAdded = userService.addUser(userDTO);

        Map<String, String> response = new HashMap<>();
        if (isUserAdded) {
            response.put("message", "User added successfully");
            return ResponseEntity.ok().body(response); // ✅ Always return JSON
        } else {
            response.put("message", "Failed to add user");
            return ResponseEntity.status(400).body(response);
        }
    }
//
//

    @GetMapping("/get-users")
    public ResponseEntity<List<UserDTO>> getUsers() {
        List<UserDTO> users = userService.getUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/update-user/{id}")
    public ResponseEntity<Map<String, String>> updateUser(@PathVariable int id, @RequestBody UserDTO userDTO) {
        boolean isUpdated = userService.updateUser(id, userDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", isUpdated ? "User updated successfully" : "Failed to update user");
        return isUpdated ? ResponseEntity.ok(response) : ResponseEntity.status(400).body(response);
    }

    // ✅ Delete User
    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable int id) {
        boolean isDeleted = userService.deleteUser(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", isDeleted ? "User deleted successfully" : "Failed to delete user");
        return isDeleted ? ResponseEntity.ok(response) : ResponseEntity.status(400).body(response);
    }

    @PutMapping("/update-password/{id}")
    public ResponseEntity<Map<String, String>> updatePassword(@PathVariable int id, @RequestBody Map<String, String> passwordMap) {
        String newPassword = passwordMap.get("password");
        boolean isUpdated = userService.updatePassword(id, newPassword);

        Map<String, String> response = new HashMap<>();
        response.put("message", isUpdated ? "Password updated successfully" : "Failed to update password");
        return isUpdated ? ResponseEntity.ok(response) : ResponseEntity.status(400).body(response);
    }



}
