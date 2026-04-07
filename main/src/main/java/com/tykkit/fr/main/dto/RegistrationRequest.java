package com.tykkit.fr.main.dto;

import lombok.Data;

@Data
public class RegistrationRequest {
    private String studentId;
    private String eventId;

    private String idempotencyKey;

}
