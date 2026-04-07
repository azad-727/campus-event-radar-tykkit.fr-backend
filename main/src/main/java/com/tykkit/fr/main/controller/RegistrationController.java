package com.tykkit.fr.main.controller;

import com.tykkit.fr.main.dto.RegistrationRequest;
import com.tykkit.fr.main.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/registrations")
public class RegistrationController {
    @Autowired
    private RegistrationService registrationService;


    @PostMapping
    public ResponseEntity<String> registerStudent(@RequestBody RegistrationRequest requestDTO){
        String res=registrationService.processRegistration(requestDTO);
        if(res.equals("ACCEPTED_IN_QUEUE")){
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Registration queued successfully.");
        }
        else if(res.equals("EVENT_FULL")){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Event is Full");
        }
        else{
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Registration Already Processed");
        }

    }
}
