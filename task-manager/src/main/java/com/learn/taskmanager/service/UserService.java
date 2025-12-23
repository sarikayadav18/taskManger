package com.learn.taskmanager.service;

import com.learn.taskmanager.model.User;
import com.learn.taskmanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    // Constructor Injection: Telling Spring to provide the UserRepository
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 1. Register a new user
    public User registerUser(User user) {
        // Business Logic: Check if the username is already taken
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken!");
        }

        // Business Logic: Check if the email is already in use
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email is already registered!");
        }

        // In a real app, you would encrypt the password here before saving
        return userRepository.save(user);
    }

    // 2. Find a user by ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // 3. Get all users (useful for testing)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 4. Delete a user
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }
}