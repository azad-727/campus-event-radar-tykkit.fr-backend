package com.tykkit.fr.main.service;

import com.tykkit.fr.main.dto.RegistrationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

        @Autowired
        private StringRedisTemplate redisTemplate;

        private static final String SEAT_KEY_PREFIX = "seats:";
        private static final String QUEUE_KEY_PREFIX ="queue:";
        private static final String IDEMPOTENCY_PREFIX = "req:";

        public String processRegistration(RegistrationRequest request){
            String eventId = request.getEventId();
            String seatKey = SEAT_KEY_PREFIX + eventId;
            String queueKey=QUEUE_KEY_PREFIX + eventId;
            String idemKey=IDEMPOTENCY_PREFIX + request.getIdempotencyKey();

            //--- Idempotency check to prevent from burst fake clicking.
            Boolean isNewRequest = redisTemplate.opsForValue().setIfAbsent(idemKey,"processing",java.time.Duration.ofSeconds(10));
            if (Boolean.FALSE.equals(isNewRequest)){
                return "DUPLICATE_REQUEST";
            }
            //--- redis seats decrement control to prevent any mismatch
            Long seatsLeft = redisTemplate.opsForValue().decrement(seatKey);

            //--- if seats are already overfull then put into waiting queue
            if(seatsLeft !=null && seatsLeft>=0){
                String queuePayload = String.format("{\"studentId\":\"%s\",\"eventId\":\"%s\"}",request.getStudentId(),eventId);
                redisTemplate.opsForList().rightPush(queueKey,queuePayload);
                return "ACCEPTED_IN_QUEUE";
            } else{
                redisTemplate.opsForValue().increment(seatKey);
                return "EVENT_FULL";
            }
        }
}
