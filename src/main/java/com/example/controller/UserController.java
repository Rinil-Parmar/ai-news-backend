package com.example.controller;

import com.example.model.ApiResponse;
import com.example.model.User;
import com.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // ================= Register =================
    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody User user) {
        if (user.getEmail() == null || user.getPassword() == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", "Email and password required", null));
        }
        Optional<User> existing = service.getByEmail(user.getEmail());
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", "User already exists", null));
        }
        User saved = service.register(user);
        saved.setPassword(null); // do not expose password
        return ResponseEntity.ok(new ApiResponse<>("success", "User registered", saved));
    }

    // ================= Login =================
    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<User>> login(@RequestBody User loginReq) {
        if (loginReq.getEmail() == null || loginReq.getPassword() == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", "Email and password required", null));
        }
        Optional<User> userOpt = service.login(loginReq.getEmail(), loginReq.getPassword());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(null);
            return ResponseEntity.ok(new ApiResponse<>("success", "Login successful", user));
        }
        return ResponseEntity.status(401).body(new ApiResponse<>("error", "Invalid credentials", null));
    }

    // ================= Get Current User =================
    @PostMapping("/users/me")
    public ResponseEntity<ApiResponse<User>> getCurrent(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", "userId required", null));
        }
        Optional<User> userOpt = service.getById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(null);
            return ResponseEntity.ok(new ApiResponse<>("success", "User info retrieved", user));
        }
        return ResponseEntity.badRequest().body(new ApiResponse<>("error", "User not found", null));
    }

    // ================= Update Preferences =================
    @PutMapping("/users/me/preferences")
    public ResponseEntity<ApiResponse<User>> updatePreferences(@RequestBody Map<String, Object> body) {
        String userId = (String) body.get("userId");
        @SuppressWarnings("unchecked")
        List<String> preferences = (List<String>) body.get("preferences");

        if (userId == null || preferences == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", "userId and preferences required", null));
        }

        User updated = service.updatePreferences(userId, preferences);
        if (updated != null) {
            updated.setPassword(null);
            return ResponseEntity.ok(new ApiResponse<>("success", "Preferences updated", updated));
        }
        return ResponseEntity.badRequest().body(new ApiResponse<>("error", "User not found", null));
    }
}