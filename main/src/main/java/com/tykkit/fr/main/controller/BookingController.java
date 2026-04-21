package com.tykkit.fr.main.controller;

import com.tykkit.fr.main.model.Booking;
import com.tykkit.fr.main.model.Event;
import com.tykkit.fr.main.repository.BookingRepository;
import com.tykkit.fr.main.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@CrossOrigin(origins = "http://localhost:5173")
public class BookingController {

    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    EventRepository eventRepository;

    @lombok.Data
    public static class BookingRequest{

        private String eventId;
        private String userEmail;
        private String ticketType;
        private String attendeeName;
        private String studentIDNumber;
    }

    @PostMapping
    public ResponseEntity<Booking> securePass(@RequestBody BookingRequest request){

        Event event=eventRepository.findById(request.eventId)
                .orElseThrow(()-> new RuntimeException("Event not found!"));

        Booking newBooking=new Booking();

        newBooking.setAttendeeName(request.attendeeName);
        newBooking.setStudentIdNumber(request.studentIDNumber);
        newBooking.setUserEmail(request.userEmail);
        newBooking.setTicketType(request.ticketType);

        newBooking.setEventId(request.eventId);
        newBooking.setEventTitle(event.getTitle());

        newBooking.setEventDate(getAttribute(event,"date"));
        newBooking.setEventTime(getAttribute(event,"time"));
        newBooking.setLocation(getAttribute(event, "locationName"));
        newBooking.setClubName(getAttribute(event,"clubName","MIT Student  Council"));

        newBooking.setSeatSection("General Admission");
        newBooking.setStatus("Confirmed");
        newBooking.setBookingTime(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(newBooking);

        return ResponseEntity.ok(savedBooking);
    }
    @GetMapping("/user/{userName}")
    public ResponseEntity<List<Booking>> getUserBookings(@PathVariable String userName){
        return ResponseEntity.ok(bookingRepository.findByUserEmail(userName));
    }

    private String getAttribute(Event event,String key){
        return getAttribute(event,key,"TBD");
    }
    private String getAttribute(Event event,String key,String defaultValue){
        if(event.getAttributes()==null) return defaultValue;
        return event.getAttributes().stream()
                .filter(attr-> attr.getK().equals(key))
                .map(Event.EventAttribute::getV)
                .findFirst()
                .orElse(defaultValue);
    }

}
