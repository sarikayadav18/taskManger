package com.learn.taskmanager.service;

import com.learn.taskmanager.dto.TaskDTO;
import com.learn.taskmanager.dto.TaskStatsDTO;
import com.learn.taskmanager.exception.ResourceNotFoundException;
import com.learn.taskmanager.model.Category;
import com.learn.taskmanager.model.Priority;
import com.learn.taskmanager.model.Task;
import com.learn.taskmanager.model.User;
import com.learn.taskmanager.repository.CategoryRepository; // 1. IMPORT ADDED
import com.learn.taskmanager.repository.TaskRepository;
import com.learn.taskmanager.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository; // 2. FIELD DECLARED

    // 3. CONSTRUCTOR UPDATED TO INJECT CategoryRepository
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
        taskEntity.setDueDate(taskDto.getDueDate()); // Don't forget the date!

        // --- PRIORITY LOGIC ---
        if (taskDto.getPriority() != null) {
            taskEntity.setPriority(Priority.valueOf(taskDto.getPriority().toUpperCase()));
        }

        // --- CATEGORY LOGIC ---
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

        task.setTitle(details.getTitle());
        task.setDescription(details.getDescription());
        task.setStatus(details.getStatus());
        task.setDueDate(details.getDueDate());

        // Update Priority if the 'details' object (Task entity) has it
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

        // Filter by Category if provided
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

    public List<TaskStatsDTO> getTaskStats(String username) {
        return taskRepository.getTaskStatsByUsername(username);
    }
}