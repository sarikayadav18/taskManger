package com.learn.taskmanager.service;

import com.learn.taskmanager.dto.TaskDTO;
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

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public Task createTask(String username, TaskDTO task) {
        User user = getUserByUsername(username);
        Task taskEntity = new Task();
        taskEntity.setUser(user);
        taskEntity.setDescription(task.getDescription());
        taskEntity.setStatus(task.getStatus());
        taskEntity.setTitle(task.getTitle());

        //task.setUser(user);
        return taskRepository.save(taskEntity);
    }

    public List<Task> getAllTasksByUsername(String username) {
        return taskRepository.findByUser(getUserByUsername(username));
    }

    public Task updateTask(Long taskId, Task details, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // SECURITY CHECK: Does this task belong to the user?
        if (!task.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized: You cannot update this task");
        }

        task.setTitle(details.getTitle());
        task.setDescription(details.getDescription());
        task.setStatus(details.getStatus());
        task.setDueDate(details.getDueDate());
        return taskRepository.save(task);
    }

    public void deleteTask(Long taskId, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized: You cannot delete this task");
        }
        taskRepository.delete(task);
    }

    public List<Task> searchTasksForUser(String username, String status, String title) {
        User user = getUserByUsername(username);
        if (status != null) return taskRepository.findByUserAndStatus(user, status);
        if (title != null) return taskRepository.findByUserAndTitleContainingIgnoreCase(user, title);
        return taskRepository.findByUser(user);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}