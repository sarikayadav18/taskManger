package com.learn.taskmanager.repository;

import com.learn.taskmanager.dto.TaskStatsDTO;
import com.learn.taskmanager.model.Priority;
import com.learn.taskmanager.model.Task;
import com.learn.taskmanager.model.User;
import org.springframework.data.domain.Page; // Added
import org.springframework.data.domain.Pageable; // Added
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Updated: Returns a Page and accepts Pageable
    Page<Task> findByUser(User user, Pageable pageable);

    // Updated: Supports paginated search by status
    Page<Task> findByUserAndStatus(User user, String status, Pageable pageable);

    // Updated: Supports paginated search by title
    Page<Task> findByUserAndTitleContainingIgnoreCase(User user, String title, Pageable pageable);
    // Inside TaskRepository.java
    Page<Task> findByUserAndCategoryId(User user, Long categoryId, Pageable pageable);

    // Inside TaskRepository.java
    @Query("SELECT new com.learn.taskmanager.dto.TaskStatsDTO(c.name, t.status, COUNT(t)) " +
            "FROM Task t JOIN t.category c " +
            "WHERE t.user.username = :username " +
            "GROUP BY c.name, t.status")
    List<TaskStatsDTO> getTaskStatsByUsername(@Param("username") String username);
    List<Task> findByDueDateAndStatusNot(LocalDate dueDate, String status);

    // Inside TaskRepository interface
    Page<Task> findByUserAndPriority(User user, Priority priority, Pageable pageable);
}