package com.learn.taskmanager.repository;

import com.learn.taskmanager.model.Task;
import com.learn.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Find all tasks belonging to a specific user
    List<Task> findByUser(User user);

    // Find tasks by status for a specific user (e.g., all "PENDING" tasks)
    List<Task> findByUserAndStatus(User user, String status);
}