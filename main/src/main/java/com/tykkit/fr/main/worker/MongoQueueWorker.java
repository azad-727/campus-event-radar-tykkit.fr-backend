package com.tykkit.fr.main.worker;

import com.tykkit.fr.main.repository.RegistrationRepository;
import com.tykkit.fr.main.model.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;

@Component

public class MongoQueueWorker {

        @Autowired
        private StringRedisTemplate redisTemplate;
        @Autowired
        private RegistrationRepository registrationRepository;
        @Autowired
        private ObjectMapper objectMapper;
        @Autowired
        private SimpMessagingTemplate messagingTemplate;
        private static final String QUEUE_KEY="queue:EVT-101";

        @Scheduled(fixedDelay = 1000 )
        public void processQueue(){
            String payload=redisTemplate.opsForList().leftPop(QUEUE_KEY);
            if(payload != null){
                try{
                    JsonNode jsonNode=objectMapper.readTree(payload);
                    String dynamicStudentId=jsonNode.get("studentId").asText();
                    String dynamicEventId=jsonNode.get("eventId").asText();

                    Registration newReg=new Registration();

                    newReg.setEventId(dynamicEventId);
                    newReg.setStudentId(dynamicStudentId);
                    newReg.setStatus("CONFIRMED");
                    newReg.setTimestamp(Instant.now());

                    registrationRepository.save(newReg);
                    System.out.println("Successfully saved student from queue to MongoDB!");
                    String message="UPDATE_EVENT:"+dynamicEventId;
                    String channel="/topics/events";
                    messagingTemplate.convertAndSend(channel,message);
                }catch(Exception e){
                    System.err.println("Error parsing JSON from Redis queue:"+e.getMessage());
                }
            }
        }
}
