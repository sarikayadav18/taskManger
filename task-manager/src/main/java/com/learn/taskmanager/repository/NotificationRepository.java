package com.learn.taskmanager.repository;

import com.learn.taskmanager.model.Notification;
import com.learn.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Finds all notifications for a specific user, newest first
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findByUserAndIsReadFalse(User user);

    boolean existsByMessageAndUser(String msg, User user);

}