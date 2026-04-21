package com.tykkit.fr.main.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String SEAT_KEY = "seats";
    private static final String QUEUE_KEY = "regqueue:";
    private static final String COUNTDOWN_KEY = "countdown:";
    private static final String CHANNEL_NEW = "events:new";
    private static final String CHANNEL_ALMOST_FULL = "events:almostfull";
    private static final String CHANNEL_SEAT_OPEN = "events:seatopen";

    public void initializeEvent(String eventId,int maxSeats,long secondUntilEvent){

        redisTemplate.opsForValue().set(SEAT_KEY + eventId, String.valueOf(maxSeats));

        redisTemplate.opsForValue().set(COUNTDOWN_KEY+eventId,"LIVE", Duration.ofSeconds(secondUntilEvent));

        redisTemplate.convertAndSend(CHANNEL_NEW,"New Event Created: "+eventId);
    }
    public String attemptRegistration(String eventId,String studentId){
        String seatKey=SEAT_KEY+eventId;
        String queueKey=QUEUE_KEY+eventId;

        Long remainingSeats = redisTemplate.opsForValue().decrement(seatKey);

        if (remainingSeats == null) {
            return "EVENT_NOT_FOUND";
        }

        // Check if we dropped below 0 (Rubric 2a: reject + INCR back)
        if (remainingSeats < 0) {
            redisTemplate.opsForValue().increment(seatKey); // Give the phantom seat back
            return "EVENT_FULL";
        }

        // Check if almost full (Rubric 2c: < 5 seats)
        if (remainingSeats < 5 && remainingSeats > 0) {
            redisTemplate.convertAndSend(CHANNEL_ALMOST_FULL, "Hurry! Event " + eventId + " is almost full!");
        }
        redisTemplate.opsForList().rightPush(queueKey, studentId);

        return "ACCEPTED_IN_QUEUE";
    }
    public void cancelRegistration(String eventId,String studentID) {
        redisTemplate.opsForValue().increment(SEAT_KEY + eventId);
        redisTemplate.convertAndSend(CHANNEL_SEAT_OPEN, "A seat just opened for event " + eventId + "!");
    }
    public String getLiveSeats(String eventId){
        return redisTemplate.opsForValue().get(SEAT_KEY+eventId);
    }
    public Long getCountdownSeconds(String eventId) {
        return redisTemplate.getExpire(COUNTDOWN_KEY + eventId);
    }
}
