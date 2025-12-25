package com.learn.taskmanager.controller;

import com.learn.taskmanager.model.Notification;
import com.learn.taskmanager.model.User;
import com.learn.taskmanager.repository.NotificationRepository;
import com.learn.taskmanager.repository.UserRepository;
import com.learn.taskmanager.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationController(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Notification> getMyNotifications(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // 1. Mark a specific notification as read
    @PatchMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notification.setRead(true);
        notificationRepository.save(notification);

        return ResponseEntity.ok("Notification marked as read");
    }

    // 2. Mark ALL notifications as read for the current user
    @PatchMapping("/read-all")
    public ResponseEntity<String> markAllAsRead(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalse(user);

        for (Notification note : unreadNotifications) {
            note.setRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);

        return ResponseEntity.ok("All notifications marked as read");
    }
}