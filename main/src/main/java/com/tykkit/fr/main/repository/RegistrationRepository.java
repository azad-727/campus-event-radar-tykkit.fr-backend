package com.tykkit.fr.main.repository;


import com.tykkit.fr.main.model.Registration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationRepository extends MongoRepository<Registration,String> {
}
