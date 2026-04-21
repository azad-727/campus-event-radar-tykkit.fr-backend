package com.tykkit.fr.main.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "booking")
public class Booking {
    @Id
    private String id;

    private String eventTitle;
    private String eventId;
    private String clubName;
    private String eventDate;
    private String eventTime;
    private String eventLineup;
    private String location;

    private String attendeeName;
    private String studentIdNumber;
    private String  userEmail;

    private String ticketType;
    private String seatSection;
    private String status;

    private LocalDateTime bookingTime;





}
