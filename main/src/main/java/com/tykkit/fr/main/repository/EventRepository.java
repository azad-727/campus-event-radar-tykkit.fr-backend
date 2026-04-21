package com.tykkit.fr.main.repository;

import com.tykkit.fr.main.model.Event;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends MongoRepository<Event,String> {
    List<Event> findByLocationNear(Point location, Distance maxDistance);
    @Query("{ 'attributes':{$elemMatch:{'k':'venueId','v',?0}}}")
    List<Event>findByVenueId(String venueId);
}
