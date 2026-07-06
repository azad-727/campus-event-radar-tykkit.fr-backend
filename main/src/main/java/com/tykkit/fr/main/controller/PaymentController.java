package com.tykkit.fr.main.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @PostMapping("/checkout")
    public ResponseEntity<?> processPayment(@RequestBody Map<String, Object> request) {
        // Simulate payment gateway delay
        try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        
        return ResponseEntity.ok(Map.of(
            "status", "SUCCESS",
            "transactionId", "TXN-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            "message", "Payment processed successfully."
        ));
    }
}
