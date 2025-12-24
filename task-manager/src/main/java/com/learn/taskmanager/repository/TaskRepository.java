package com.learn.taskmanager.repository;

import com.learn.taskmanager.model.Task;
import com.learn.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUser(User user);
    List<Task> findByUserAndStatus(User user, String status);
    List<Task> findByUserAndTitleContainingIgnoreCase(User user, String title);
}