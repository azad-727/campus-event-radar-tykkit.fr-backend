package com.tykkit.fr.main.controller;

import com.tykkit.fr.main.model.User;
import com.tykkit.fr.main.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> request) {
        String studentId = request.get("studentId");
        User user = userRepository.findByAcademicRollNo(studentId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }
        
        if (request.containsKey("name") && !request.get("name").trim().isEmpty()) {
            user.setFullName(request.get("name").trim());
        }
        
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully!", "name", user.getFullName()));
    }
}
