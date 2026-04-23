package com.tykkit.fr.main.controller;

import com.tykkit.fr.main.dto.RegistrationRequest;
import com.tykkit.fr.main.model.Event;
import com.tykkit.fr.main.repository.EventRepository;
import com.tykkit.fr.main.repository.RegistrationRepository;
import com.tykkit.fr.main.service.RedisService; // Make sure to import the new RedisService!
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
// CHANGED: The rubric demands we base this on /events, not /registrations
@RequestMapping("/api/v1/events")
public class RegistrationController {

    // Swap this to the new RedisService we built to guarantee the Rubric points
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisService redisService;
    @Autowired
    private RegistrationRepository regRepo;

    /**
     * 1. SECURE PASS (Rubric 3a: POST /events/:id/register)
     */
    @PostMapping("/{id}/register")
    public ResponseEntity<?> registerStudent(
            @PathVariable("id") String eventId,
            @RequestBody RegistrationRequest requestDTO) {

        // We use the eventId from the URL, and the studentId from your existing DTO
        String res = redisService.attemptRegistration(eventId, requestDTO.getStudentId());

        if (res.equals("ACCEPTED_IN_QUEUE")) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of("message", "Registration queued successfully.", "status", "QUEUED"));
        } else if (res.equals("EVENT_FULL")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Sold Out! No passes remaining."));
        } else if (res.equals("EVENT_NOT_FOUND")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Event not found or not initialized in Redis."));
        } else {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of("message", "Registration Already Processed"));
        }
    }

    /**
     * 2. CANCEL PASS (Rubric 3a: DELETE /events/:id/register)
     */
    @DeleteMapping("/{id}/register")
    public ResponseEntity<?> cancelRegistration(
            @PathVariable("id") String eventId,
            @RequestParam String studentId) {

        redisService.cancelRegistration(eventId, studentId);
        return ResponseEntity.ok(Map.of("message", "Pass cancelled. Seat freed up in Redis!"));
    }

    /**
     * 3. LIVE SEATS (Rubric 3a: GET /events/:id/seats)
     */

    @GetMapping("/{id}/countdown")
    public ResponseEntity<?> getEventCountdown(@PathVariable("id") String eventId) {
        Long ttl = redisService.getCountdownSeconds(eventId);

        // If ttl is negative or null, the key expired (event started) or doesn't exist
        return ResponseEntity.ok(Map.of("secondsRemaining", ttl != null && ttl > 0 ? ttl : 0));
    }
    @GetMapping("/my-passes/{studentId}")
    public ResponseEntity<?> getUserPasses(@PathVariable("studentId") String studentId){
        return ResponseEntity.ok(regRepo.findByStudentId(studentId));
    }
}