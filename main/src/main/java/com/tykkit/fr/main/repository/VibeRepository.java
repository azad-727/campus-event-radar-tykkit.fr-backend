package com.tykkit.fr.main.repository;

import com.tykkit.fr.main.model.Vibe;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VibeRepository extends MongoRepository<Vibe,String> {

}
