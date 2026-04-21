package com.tykkit.fr.main.controller;

import com.tykkit.fr.main.model.Event;
import com.tykkit.fr.main.repository.EventRepository;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@CrossOrigin(origins = "http://localhost:5173")
public class EventController {
    @Autowired
    EventRepository eventRepository;

    public static class EventRequest {
        public String eventId;
        public String title;
        public String type;
        public int maxSeats;
        public int registeredCount;
        public double longitude; // Flat numbers instead of complex Geo JSON
        public double latitude;
        public List<Event.EventAttribute> attributes;
    }
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents(){
        return ResponseEntity.ok(eventRepository.findAll());
    }
    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody EventRequest request) {
        Event newEvent = new Event();
        newEvent.setEventId(request.eventId);
        newEvent.setTitle(request.title);
        newEvent.setType(request.type);
        newEvent.setMaxSeats(request.maxSeats);
        newEvent.setRegisteredCount(request.registeredCount);
        newEvent.setAttributes(request.attributes);

        // Safely construct the GeoJsonPoint in Java where it won't crash
        GeoJsonPoint locationPoint = new GeoJsonPoint(request.longitude, request.latitude);
        newEvent.setLocation(locationPoint);

        // Save to MongoDB
        Event savedEvent = eventRepository.save(newEvent);

        return ResponseEntity.ok(savedEvent);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<Event>> getNearByEvents(@RequestParam double lat,@RequestParam double lng,@RequestParam double radiusKm){
        Point userLocation=new Point(lng,lat);
        Distance distance=new Distance(radiusKm,org.springframework.data.geo.Metrics.KILOMETERS);

        List<Event> nearbyEvents=eventRepository.findByLocationNear(userLocation,distance);
        return ResponseEntity.ok(nearbyEvents);
    }
}
