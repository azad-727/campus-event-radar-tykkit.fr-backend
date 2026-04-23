package com.tykkit.fr.main.config;

import com.tykkit.fr.main.model.Event; // Make sure this matches your package
import com.tykkit.fr.main.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RedisSyncRunner implements CommandLineRunner {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisService redisService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🔄 [SYSTEM STARTUP] Synchronizing MongoDB Seats to Redis...");

        // 1. Fetch all events currently sitting in MongoDB
        List<Event> allEvents = mongoTemplate.findAll(Event.class);

        int syncCount = 0;

        for (Event event : allEvents) {
            String eventId = event.getId();

            // 2. Check if Redis already knows about this event so we don't overwrite live data
            if (redisService.getLiveSeats(eventId) == null) {

                // 👉 THE CRITICAL LOGIC FIX: Subtract registered users!
                int actualAvailableSeats = event.getMaxSeats() - event.getRegisteredCount();

                // Let's assume the event happens in 7 days for the countdown timer
                long secondsUntilEvent = 604800L;

                if (event.getEventDate() != null) {
                    // Math: (Event Time) - (Right Now)
                    secondsUntilEvent = java.time.Duration.between(
                            java.time.Instant.now(),
                            event.getEventDate()
                    ).getSeconds();

                    if (secondsUntilEvent < 0) {
                        secondsUntilEvent = 0;
                    }
                } // 👈 THE SYNTAX FIX: This closing bracket was missing!

                // 3. Inject the REAL, calculated MongoDB data into Redis
                redisService.initializeEvent(eventId, actualAvailableSeats, secondsUntilEvent);

                System.out.println("✅ Synced Event: " + eventId + " | Available Seats: " + actualAvailableSeats);
                syncCount++;
            }
        }

        System.out.println("🚀 [SYSTEM READY] Successfully synchronized " + syncCount + " missing events into Redis.");
    }
}