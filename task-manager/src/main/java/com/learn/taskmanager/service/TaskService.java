package com.learn.taskmanager.service;

import com.learn.taskmanager.model.Task;
import com.learn.taskmanager.model.User;
import com.learn.taskmanager.repository.TaskRepository;
import com.learn.taskmanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // Constructor Injection (Spring automatically provides these)
    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // 1. Create a new task
    public Task createTask(Long userId, Task task) {
        // Business Logic: Ensure the user exists before assigning a task
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        task.setUser(user);
        return taskRepository.save(task);
    }

    // 2. Get all tasks for a specific user
    public List<Task> getAllTasksByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return taskRepository.findByUser(user);
    }

    // 3. Update an existing task
    public Task updateTask(Long taskId, Task taskDetails) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Update fields
        existingTask.setTitle(taskDetails.getTitle());
        existingTask.setDescription(taskDetails.getDescription());
        existingTask.setStatus(taskDetails.getStatus());
        existingTask.setDueDate(taskDetails.getDueDate());

        return taskRepository.save(existingTask);
    }

    // 4. Delete a task
    public void deleteTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new RuntimeException("Cannot delete: Task not found");
        }
        taskRepository.deleteById(taskId);
    }
}