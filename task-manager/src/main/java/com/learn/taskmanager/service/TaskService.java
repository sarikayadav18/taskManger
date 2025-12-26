package com.learn.taskmanager.service;

import com.learn.taskmanager.dto.*;
import com.learn.taskmanager.exception.ResourceNotFoundException;
import com.learn.taskmanager.model.Category;
import com.learn.taskmanager.model.Priority;
import com.learn.taskmanager.model.Task;
import com.learn.taskmanager.model.User;
import com.learn.taskmanager.repository.CategoryRepository;
import com.learn.taskmanager.repository.TaskRepository;
import com.learn.taskmanager.repository.UserRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository,
                       CategoryRepository categoryRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    public Task createTask(String username, TaskDTO taskDto) {
        User user = getUserByUsername(username);
        Task taskEntity = new Task();
        taskEntity.setUser(user);
        taskEntity.setTitle(taskDto.getTitle());
        taskEntity.setDescription(taskDto.getDescription());
        taskEntity.setStatus(taskDto.getStatus());
        taskEntity.setDueDate(taskDto.getDueDate());

        if (taskDto.getPriority() != null) {
            taskEntity.setPriority(Priority.valueOf(taskDto.getPriority().toUpperCase()));
        }

        // Auto-stamp if created as COMPLETED
        if ("COMPLETED".equalsIgnoreCase(taskDto.getStatus())) {
            taskEntity.setCompletedAt(LocalDate.now());
        }

        if (taskDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(taskDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

            if (!category.getUser().getUsername().equals(username)) {
                throw new SecurityException("You do not own this category");
            }
            taskEntity.setCategory(category);
        }

        return taskRepository.save(taskEntity);
    }

    public Page<Task> getAllTasksByUsername(String username, int page, int size, String sortBy, String direction) {
        User user = getUserByUsername(username);
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return taskRepository.findByUser(user, pageable);
    }

    public Task updateTask(Long taskId, Task details, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (!task.getUser().getUsername().equals(username)) {
            throw new SecurityException("Unauthorized: You cannot update this task");
        }

        // --- WEEKLY ACTIVITY LOGIC: Date Stamping ---
        // If the task status is being changed to COMPLETED, set the timestamp
        if ("COMPLETED".equalsIgnoreCase(details.getStatus()) && !"COMPLETED".equalsIgnoreCase(task.getStatus())) {
            task.setCompletedAt(LocalDate.now());
        }
        // If moved out of COMPLETED, remove the timestamp
        else if (!"COMPLETED".equalsIgnoreCase(details.getStatus())) {
            task.setCompletedAt(null);
        }

        task.setTitle(details.getTitle());
        task.setDescription(details.getDescription());
        task.setStatus(details.getStatus());
        task.setDueDate(details.getDueDate());

        if (details.getPriority() != null) {
            task.setPriority(details.getPriority());
        }

        return taskRepository.save(task);
    }

    public void deleteTask(Long taskId, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (!task.getUser().getUsername().equals(username)) {
            throw new SecurityException("Unauthorized: You cannot delete this task");
        }
        taskRepository.delete(task);
    }

    public Page<Task> searchTasksForUser(String username, String status, String title, Long categoryId, String priority, int page, int size, String sortBy, String direction) {
        User user = getUserByUsername(username);
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (priority != null) {
            Priority priorityEnum = Priority.valueOf(priority.toUpperCase());
            return taskRepository.findByUserAndPriority(user, priorityEnum, pageable);
        }

        if (categoryId != null) {
            return taskRepository.findByUserAndCategoryId(user, categoryId, pageable);
        }

        if (status != null) return taskRepository.findByUserAndStatus(user, status, pageable);
        if (title != null) return taskRepository.findByUserAndTitleContainingIgnoreCase(user, title, pageable);

        return taskRepository.findByUser(user, pageable);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    // --- DASHBOARD & ANALYTICS ---

    public DashboardDTO getDashboardData(String username) {
        List<TaskStatsDTO> statusStats = taskRepository.getTaskStatsByUsername(username);
        List<PriorityStatsDTO> priorityStats = taskRepository.getPriorityStatsByUsername(username);

        // Fetch Weekly Activity for the last 7 days
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        List<WeeklyActivityDTO> weeklyActivity = taskRepository.getWeeklyActivity(username, sevenDaysAgo);

        long totalTasks = 0;
        long completedTasks = 0;

        for (TaskStatsDTO stat : statusStats) {
            totalTasks += stat.getCount();
            if ("COMPLETED".equalsIgnoreCase(stat.getStatus())) {
                completedTasks = stat.getCount();
            }
        }

        double rate = (totalTasks > 0) ? ((double) completedTasks / totalTasks) * 100 : 0.0;
        double roundedRate = Math.round(rate * 100.0) / 100.0;

        DashboardDTO dashboard = new DashboardDTO();
        dashboard.setStatusStats(statusStats);
        dashboard.setPriorityStats(priorityStats);
        dashboard.setCompletionRate(roundedRate);
        dashboard.setWeeklyActivity(weeklyActivity); // New data included

        return dashboard;
    }

    public @Nullable List<TaskStatsDTO> getTaskStats(String name) {
        return List.of();
    }
}