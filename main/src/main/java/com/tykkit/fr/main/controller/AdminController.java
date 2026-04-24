package com.tykkit.fr.main.controller;

import com.tykkit.fr.main.model.User; // Ensure this matches your User/Student model name
import com.tykkit.fr.main.model.Registration;
import com.tykkit.fr.main.repository.UserRepository; // Or StudentRepository, depending on your setup
import com.tykkit.fr.main.repository.RegistrationRepository;
import com.tykkit.fr.main.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*")  // Crucial: Allows React to talk to this endpoint
public class AdminController {

    // --- REPOSITORIES ---
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegistrationRepository regRepo;

    @Autowired
    private EventRepository eventRepo;

    @Autowired
    private StringRedisTemplate redisTemplate;
    @GetMapping("/insights")
    public ResponseEntity<Map<String, Object>> getDashboardInsights() {
        Map<String, Object> insights = new HashMap<>();

        // Count documents directly from MongoDB
        insights.put("totalUsers", userRepository.count());
        insights.put("totalEvents", eventRepo.count());
        insights.put("totalTicketsIssued", regRepo.count());

        String liveCount = redisTemplate.opsForValue().get("live_website_visitors");
        insights.put("liveVisitors", liveCount != null ? Integer.parseInt(liveCount) : 0);

        return ResponseEntity.ok(insights);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        // Returns the entire list of users from MongoDB
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/users/{studentId}/history")
    public ResponseEntity<List<Registration>> getUserHistory(@PathVariable String studentId) {
        // Uses the method we created earlier to find tickets by student ID
        return ResponseEntity.ok(regRepo.findByStudentId(studentId));
    }
    @PutMapping("/scan")
    public ResponseEntity<Map<String, String>> scanTicket(@RequestBody Map<String, String> payload) {

        String rawQrData = payload.get("ticketHash");
        if (rawQrData == null || rawQrData.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("status", "DENIED", "message", "AWAITING INPUT"));
        }
        String eventId = "";
        String studentId = "";
        if (rawQrData.startsWith("tykkit-auth://event/")) {
            try {
                // Strip the prefix
                String cleanString = rawQrData.substring("tykkit-auth://event/".length());
                // Split by "/user/"
                String[] parts = cleanString.split("/user/");

                eventId = parts[0];
                studentId = parts[1];
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("status", "DENIED", "message", "MALFORMED QR CODE"));
            }
        } else {
            // If they type garbage into the manual scanner box
            return ResponseEntity.badRequest().body(Map.of("status", "DENIED", "message", "INVALID TICKET FORMAT"));
        }

        Registration ticket = regRepo.findFirstByEventIdAndStudentId(eventId,studentId);

        if (ticket == null) {
            return ResponseEntity.status(404).body(Map.of("status", "DENIED", "message", "TICKET NOT FOUND"));
        }

        // 3. PREVENT SCREENSHOT SHARING / DOUBLE SCANNING
        if ("ATTENDED".equals(ticket.getStatus())) {
            return ResponseEntity.status(400).body(Map.of("status", "DENIED", "message", "ALREADY SCANNED"));
        }

        // 4. GRANT ACCESS
        ticket.setStatus("ATTENDED");
        regRepo.save(ticket);

        return ResponseEntity.ok(Map.of("status", "GRANTED", "message", "ACCESS GRANTED"));
    }
}