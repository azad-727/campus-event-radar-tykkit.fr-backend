package com.tykkit.fr.main.repository;

import com.tykkit.fr.main.model.UserMembership;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMembershipRepository extends MongoRepository<UserMembership, String> {
    List<UserMembership> findByUserId(String userId);
    Optional<UserMembership> findByUserIdAndInstituteId(String userId, String instituteId);
}
