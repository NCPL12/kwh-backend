package ncpl.bms.reports.controller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ncpl.bms.reports.service.LoginService;

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
public class loginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginDetails) {
        String username = loginDetails.get("username");
        String password = loginDetails.get("password");

        Optional<Map<String, String>> userRole = loginService.verifyUser(username, password);

        if (userRole.isPresent()) {
            return ResponseEntity.ok(userRole.get());
        } else {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }




}
