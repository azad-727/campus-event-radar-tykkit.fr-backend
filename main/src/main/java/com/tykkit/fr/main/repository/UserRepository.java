package com.tykkit.fr.main.repository;

import com.tykkit.fr.main.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User,String> {

    Optional<User> findByEmail(String email);
    Optional<User> findByAcademicRollNo(String rollNo);
    Boolean existsByEmail(String email);

}
