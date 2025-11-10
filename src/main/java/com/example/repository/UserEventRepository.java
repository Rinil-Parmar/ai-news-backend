package com.example.repository;

import com.example.model.UserEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserEventRepository extends MongoRepository<UserEvent, String> {
    List<UserEvent> findByUserId(String userId);
}