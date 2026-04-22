package com.tykkit.fr.main.repository;


import com.tykkit.fr.main.model.Registration;
import org.jspecify.annotations.Nullable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationRepository extends MongoRepository<Registration,String> {
    List<Registration> findByStudentId(String studentId);
}
