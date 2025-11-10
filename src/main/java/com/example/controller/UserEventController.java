package com.example.controller;

import com.example.model.ApiResponse;
import com.example.model.UserEvent;
import com.example.service.UserEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class UserEventController {

    private final UserEventService service;

    public UserEventController(UserEventService service) {
        this.service = service;
    }

    // SAVE EVENT
    @PostMapping
    public ResponseEntity<ApiResponse<UserEvent>> saveEvent(@RequestBody UserEvent event) {
        UserEvent saved = service.saveEvent(event);
        return ResponseEntity.ok(new ApiResponse<>("success", "Event saved", saved));
    }

    // GET USER EVENTS
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<UserEvent>>> getUserEvents(@PathVariable String userId) {
        List<UserEvent> events = service.getEventsByUser(userId);
        return ResponseEntity.ok(new ApiResponse<>("success", "User events retrieved", events));
    }
}