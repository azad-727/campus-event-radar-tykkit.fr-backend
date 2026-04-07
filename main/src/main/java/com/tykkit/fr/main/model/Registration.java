package com.tykkit.fr.main.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
//import org.springframework.stereotype.Indexed;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;

@Data
@NoArgsConstructor
@Document(collection = "registration")
public class Registration {
    @Id
    private String id;

    @Indexed
    private String eventId;
    @Indexed
    private String studentId;

    private String status;
    private Instant timestamp;
    private boolean attendance;
}
