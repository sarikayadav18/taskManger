package com.learn.taskmanager.repository;

import com.learn.taskmanager.dto.PriorityStatsDTO;
import com.learn.taskmanager.dto.TaskStatsDTO;
import com.learn.taskmanager.dto.WeeklyActivityDTO;
import com.learn.taskmanager.model.Priority;
import com.learn.taskmanager.model.Status;
import com.learn.taskmanager.model.Task;
import com.learn.taskmanager.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // --- BASIC QUERIES ---
    Page<Task> findByUser(User user, Pageable pageable);

    Page<Task> findByUserAndStatus(User user, String status, Pageable pageable);

    Page<Task> findByUserAndTitleContainingIgnoreCase(User user, String title, Pageable pageable);

    Page<Task> findByUserAndCategoryId(User user, Long categoryId, Pageable pageable);

    Page<Task> findByUserAndPriority(User user, Priority priority, Pageable pageable);

    List<Task> findByStatus(Status status);

    List<Task> findByDueDateAndStatusNot(LocalDate dueDate, String status);

    // --- AUTOMATION QUERIES ---
    @Modifying
    @Query("UPDATE Task t SET t.status = 'OVERDUE' WHERE t.dueDate < :today AND t.status != 'COMPLETED'")
    int markExpiredTasksAsOverdue(@Param("today") LocalDate today);

    // --- DASHBOARD & ANALYTICS QUERIES ---

    /**
     * Uses LEFT JOIN so that tasks without a category are still counted.
     * If category is null, c.name will be null in the resulting DTO.
     */
    @Query("SELECT new com.learn.taskmanager.dto.TaskStatsDTO(c.name, t.status, COUNT(t)) " +
            "FROM Task t LEFT JOIN t.category c " +
            "WHERE t.user.username = :username " +
            "GROUP BY c.name, t.status")
    List<TaskStatsDTO> getTaskStatsByUsername(@Param("username") String username);

    @Query("SELECT new com.learn.taskmanager.dto.PriorityStatsDTO(t.priority, COUNT(t)) " +
            "FROM Task t WHERE t.user.username = :username " +
            "GROUP BY t.priority")
    List<PriorityStatsDTO> getPriorityStatsByUsername(@Param("username") String username);

    /**
     * Fetches completion counts per day for the last 7 days.
     */
    @Query("SELECT new com.learn.taskmanager.dto.WeeklyActivityDTO(t.completedAt, COUNT(t)) " +
            "FROM Task t " +
            "WHERE t.user.username = :username " +
            "AND t.status = 'COMPLETED' " +
            "AND t.completedAt >= :startDate " +
            "AND t.completedAt IS NOT NULL " +
            "GROUP BY t.completedAt " +
            "ORDER BY t.completedAt ASC")
    List<WeeklyActivityDTO> getWeeklyActivity(@Param("username") String username, @Param("startDate") LocalDate startDate);
    @Query("SELECT t FROM Task t WHERE t.user = :user AND (" +
            "LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(COALESCE(t.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Task> searchByKeyword(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);


        // This will generate the SQL: SELECT * FROM tasks WHERE status = ?
        List<Task> findByStatus(String status);
    // Finds tasks belonging to a specific username
    List<Task> findByUserUsername(String username);

    // If you want to combine filtering from before:
    List<Task> findByUserUsernameAndStatus(String username, String status);


}