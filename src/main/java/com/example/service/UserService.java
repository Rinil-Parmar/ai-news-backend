package com.example.service;

import com.example.model.User;
import com.example.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository repository) {
        this.repository = repository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // Register user
    public User register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        return repository.save(user);
    }

    // Login
    public Optional<User> login(String email, String password) {
        Optional<User> userOpt = repository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    // Get by email
    public Optional<User> getByEmail(String email) {
        return repository.findByEmail(email);
    }

    // Get by ID
    public Optional<User> getById(String id) {
        return repository.findById(id);
    }

    // Update preferences
    public User updatePreferences(String userId, List<String> preferences) {
        Optional<User> userOpt = repository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPreferences(preferences);
            user.setUpdatedAt(new Date());
            return repository.save(user);
        }
        return null;
    }
}