package com.learn.taskmanager.repository;

import com.learn.taskmanager.model.Task;
import com.learn.taskmanager.model.User;
import org.springframework.data.domain.Page; // Added
import org.springframework.data.domain.Pageable; // Added
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Updated: Returns a Page and accepts Pageable
    Page<Task> findByUser(User user, Pageable pageable);

    // Updated: Supports paginated search by status
    Page<Task> findByUserAndStatus(User user, String status, Pageable pageable);

    // Updated: Supports paginated search by title
    Page<Task> findByUserAndTitleContainingIgnoreCase(User user, String title, Pageable pageable);
}