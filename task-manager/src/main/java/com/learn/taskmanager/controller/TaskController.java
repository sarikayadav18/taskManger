package com.learn.taskmanager.controller;

import com.learn.taskmanager.model.Task;
import com.learn.taskmanager.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // Create a task for a user: POST http://localhost:8080/api/tasks/user/1
    @PostMapping("/user/{userId}")
    public ResponseEntity<Task> createTask(@PathVariable Long userId, @RequestBody Task task) {
        return ResponseEntity.ok(taskService.createTask(userId, task));
    }

    // Get all tasks for a user: GET http://localhost:8080/api/tasks/user/1
    @GetMapping("/user/{userId}")
    public List<Task> getTasksByUserId(@PathVariable Long userId) {
        return taskService.getAllTasksByUser(userId);
    }


    @GetMapping("/search")
    public List<Task> searchTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String title) {

        if (status != null) {
            return taskService.getTasksByStatus(status);
        } else if (title != null) {
            return taskService.searchTasksByTitle(title);
        }

        return taskService.getAllTasks(); // Returns all if no params are sent
    }

    // Update a task: PUT http://localhost:8080/api/tasks/5
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task taskDetails) {
        return ResponseEntity.ok(taskService.updateTask(id, taskDetails));
    }

    // Delete a task: DELETE http://localhost:8080/api/tasks/5
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok("Task deleted successfully!");
    }
}