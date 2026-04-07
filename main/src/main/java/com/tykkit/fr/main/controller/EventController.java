package com.tykkit.fr.main.controller;

import com.tykkit.fr.main.model.Event;
import com.tykkit.fr.main.repository.EventRepository;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {
    @Autowired
    EventRepository eventRepository;

    @GetMapping("/nearby")
    public ResponseEntity<List<Event>> getNearByEvents(@RequestParam double lat,@RequestParam double lng,@RequestParam double radiusKm){
        Point userLocation=new Point(lng,lat);
        Distance distance=new Distance(radiusKm,org.springframework.data.geo.Metrics.KILOMETERS);

        List<Event> nearbyEvents=eventRepository.findByLocationNear(userLocation,distance);
        return ResponseEntity.ok(nearbyEvents);
    }
}
