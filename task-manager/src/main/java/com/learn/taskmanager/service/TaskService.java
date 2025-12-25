package com.learn.taskmanager.service;

import com.learn.taskmanager.dto.TaskDTO;
import com.learn.taskmanager.exception.ResourceNotFoundException;
import com.learn.taskmanager.model.Task;
import com.learn.taskmanager.model.User;
import com.learn.taskmanager.repository.TaskRepository;
import com.learn.taskmanager.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public Task createTask(String username, TaskDTO taskDto) {
        User user = getUserByUsername(username);
        Task taskEntity = new Task();
        taskEntity.setUser(user);
        taskEntity.setDescription(taskDto.getDescription());
        taskEntity.setStatus(taskDto.getStatus());
        taskEntity.setTitle(taskDto.getTitle());
        return taskRepository.save(taskEntity);
    }

    // UPDATED: Now returns a Page and accepts pagination/sorting parameters
    public Page<Task> getAllTasksByUsername(String username, int page, int size, String sortBy, String direction) {
        User user = getUserByUsername(username);

        // Logic: Create a Sort object based on direction string
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        // Logic: Create Pageable object (Spring Page indexes start at 0)
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

    // UPDATED: Search now supports pagination
    public Page<Task> searchTasksForUser(String username, String status, String title, int page, int size, String sortBy, String direction) {
        User user = getUserByUsername(username);
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (status != null) return taskRepository.findByUserAndStatus(user, status, pageable);
        if (title != null) return taskRepository.findByUserAndTitleContainingIgnoreCase(user, title, pageable);

        return taskRepository.findByUser(user, pageable);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}