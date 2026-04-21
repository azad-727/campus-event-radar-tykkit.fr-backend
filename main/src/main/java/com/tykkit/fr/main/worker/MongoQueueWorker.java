package com.tykkit.fr.main.worker;

import com.tykkit.fr.main.model.Event; // Make sure to import your Event model
import com.tykkit.fr.main.repository.RegistrationRepository;
import com.tykkit.fr.main.model.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.Set;

@Component
public class MongoQueueWorker {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RegistrationRepository registrationRepository;

    // NEW: We need MongoTemplate to run the specific $inc command for the rubric
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedDelay = 1000)
    public void processQueue() {
        // 1. DYNAMIC QUEUES (Rubric 2b): Find all keys starting with 'regqueue:'
        Set<String> queueKeys = redisTemplate.keys("regqueue:*");
        if (queueKeys == null || queueKeys.isEmpty()) return;

        for (String queueKey : queueKeys) {
            // LPOP for FIFO fairness
            String payload = redisTemplate.opsForList().leftPop(queueKey);

            if (payload != null) {
                try {
                    JsonNode jsonNode = objectMapper.readTree(payload);
                    String dynamicStudentId = jsonNode.get("studentId").textValue();
                    String dynamicEventId = jsonNode.get("eventId").textValue();

                    // 2. Save the Registration Record
                    Registration newReg = new Registration();
                    newReg.setEventId(dynamicEventId);
                    newReg.setStudentId(dynamicStudentId);
                    newReg.setStatus("CONFIRMED");
                    newReg.setTimestamp(Instant.now());
                    registrationRepository.save(newReg);

                    // 3. COMPUTED PATTERN (Rubric 1c): Use $inc to update the Event's registered_count
                    Query query = new Query(Criteria.where("id").is(dynamicEventId));
                    Update update = new Update().inc("registeredCount", 1);
                    mongoTemplate.updateFirst(query, update, Event.class);

                    System.out.println("Successfully saved student to MongoDB & incremented count for: " + dynamicEventId);

                    // 4. Fire WebSocket Notification
                    String message = "UPDATE_EVENT:" + dynamicEventId;
                    String channel = "/topic/events";
                    messagingTemplate.convertAndSend(channel, message);

                } catch (Exception e) {
                    System.err.println("Error parsing JSON from Redis queue: " + e.getMessage());
                }
            }
        }
    }
}