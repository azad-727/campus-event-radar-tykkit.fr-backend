package com.tykkit.fr.main.worker;

import com.tykkit.fr.main.model.Event;
import com.tykkit.fr.main.repository.EventRepository;
import com.tykkit.fr.main.repository.RegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class EventCleanupWorker {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Scheduled(fixedDelay = 60000)
    public void cleanupExpiredEvents() {
        List<Event> allEvents = eventRepository.findAll();
        Instant now = Instant.now();

        for (Event event : allEvents) {
            if (event.getEventDate() != null && event.getEventDate().isBefore(now) && !"COMPLETED".equals(event.getStatus())) {
                System.out.println("Marking event as COMPLETED: " + event.getId());
                
                // 1. Mark as completed
                event.setStatus("COMPLETED");
                eventRepository.save(event);

                // 2. Cleanup Redis keys
                redisTemplate.delete("event:" + event.getId() + ":seats");
                redisTemplate.delete("countdown:" + event.getId());
                redisTemplate.delete("regqueue:" + event.getId());
                
                // Registrations remain as they are, but frontend will see event is COMPLETED
            }
        }
    }
}
