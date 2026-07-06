package com.tykkit.fr.main.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class UserDTO {
    private String id;
    private String fullName;
    private String email;
    private double globalTrustScore;
    private Instant createdAt;
    private String academicRollNo;
    private String rollType;
}
