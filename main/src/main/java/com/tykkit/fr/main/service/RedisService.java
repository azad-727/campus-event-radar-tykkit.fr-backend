package com.tykkit.fr.main.service;


import com.tykkit.fr.main.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.tykkit.fr.main.repository.RegistrationRepository;
import com.tykkit.fr.main.model.Event;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Duration;

@Service
public class RedisService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private RegistrationRepository regRepo;

    private static final String SEAT_KEY = "seats:";
    private static final String QUEUE_KEY = "regqueue:";
    private static final String COUNTDOWN_KEY = "countdown:";
    private static final String CHANNEL_NEW = "events:new";
    private static final String CHANNEL_ALMOST_FULL = "events:almostfull";
    private static final String CHANNEL_SEAT_OPEN = "events:seatopen";

    public void initializeEvent(String eventId, int maxSeats, long secondUntilEvent) {

        // 👉 THE FIX: Force the exact key string so the React Dashboard can find it!
        String seatKey = "event:" + eventId + ":seats";
        redisTemplate.opsForValue().set(seatKey, String.valueOf(maxSeats));

        // The countdown key is fine as is!
        redisTemplate.opsForValue().set(COUNTDOWN_KEY + eventId, "LIVE", Duration.ofSeconds(secondUntilEvent));

        redisTemplate.convertAndSend(CHANNEL_NEW, "New Event Created: " + eventId);
    }
    public String attemptRegistration(String eventId,String studentId){

        String seatKey = "event:" + eventId + ":seats";
        String queueKey = QUEUE_KEY + eventId;

        Object rawSeats = redisTemplate.opsForValue().get(seatKey);

        Long remainingSeats = redisTemplate.opsForValue().decrement(seatKey);

        if (rawSeats == null || rawSeats.toString().equals("0")) {
            System.out.println("⚠️ Cache Miss/Empty for " + eventId + ". Healing from MongoDB...");

            Event event = eventRepository.findById(eventId).orElse(null);

            if (event == null) {
                return "EVENT_NOT_FOUND"; // It really doesn't exist
            }

            int realSeats = event.getMaxSeats() - event.getRegisteredCount();

            if (realSeats <= 0) {
                return "EVENT_FULL"; // MongoDB confirms it is actually sold out
            }

            // HEAL REDIS: Overwrite the bad data with the real number
            redisTemplate.opsForValue().set(seatKey, String.valueOf(realSeats));
        }

        if (remainingSeats != null && remainingSeats >= 0) {

            // Check if almost full (Rubric 2c: < 5 seats)
            if (remainingSeats < 5 && remainingSeats > 0) {
                redisTemplate.convertAndSend(CHANNEL_ALMOST_FULL, "Hurry! Event " + eventId + " is almost full!");
            }

            // Push to RabbitMQ / Queue
            String jsonPayload = String.format("{\"eventId\":\"%s\", \"studentId\":\"%s\"}", eventId, studentId);
            redisTemplate.opsForList().rightPush(queueKey, jsonPayload);

            return "ACCEPTED_IN_QUEUE";
        } else {
            // Failsafe rollback
            redisTemplate.opsForValue().increment(seatKey);
            return "EVENT_FULL";
        }
    }
    public void cancelRegistration(String eventId,String studentID) {
        regRepo.deleteByEventIdAndStudentId(eventId, studentID);

        Query query=new Query(Criteria.where("id").is(eventId));
        Update update=new Update().inc("registeredCount",-1);
        mongoTemplate.updateFirst(query,update,Event.class);

        redisTemplate.opsForValue().increment(SEAT_KEY + eventId);
        redisTemplate.convertAndSend(CHANNEL_SEAT_OPEN, "A seat just opened for event " + eventId + "!");
        System.out.println("Pass Terminated: "+studentID+"| Seat refunded to Redis for event: "+eventId);
    }
    public String getLiveSeats(String eventId){
        return redisTemplate.opsForValue().get(SEAT_KEY+eventId);
    }
    public Long getCountdownSeconds(String eventId) {
        return redisTemplate.getExpire(COUNTDOWN_KEY + eventId);
    }
}
