package com.tykkit.fr.main.repository;

import com.tykkit.fr.main.model.Booking;
import com.tykkit.fr.main.worker.MongoQueueWorker;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookingRepository extends MongoRepository<Booking,String> {

        List<Booking> findByUserEmail(String userName);
}
