package com.example.service;

import com.example.model.UserEvent;
import com.example.repository.UserEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserEventService {

    private final UserEventRepository repository;

    public UserEventService(UserEventRepository repository) {
        this.repository = repository;
    }

    public UserEvent saveEvent(UserEvent event) {
        return repository.save(event);
    }

    public List<UserEvent> getEventsByUser(String userId) {
        return repository.findByUserId(userId);
    }
}