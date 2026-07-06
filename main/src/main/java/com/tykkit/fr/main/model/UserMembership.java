package com.tykkit.fr.main.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "user_memberships")
@CompoundIndex(def = "{'userId':1,'instituteId':1}",unique = true)
public class UserMembership {

    @Id
    private String id;

    private String userId;
    private String instituteId;

    private Role role;
    private String academicRollNo;

    private Instant joinedAt;

    public enum Role{
        STUDENT,ADMIN,ALUMNI,BANNED
    }

}
