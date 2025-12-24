package com.learn.taskmanager.controller;

import com.learn.taskmanager.dto.TaskDTO;
import com.learn.taskmanager.model.Task;
import com.learn.taskmanager.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // CREATE: POST http://localhost:8080/api/tasks
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody TaskDTO task, Authentication authentication) {
        return ResponseEntity.ok(taskService.createTask(authentication.getName(), task));
    }

    // READ ALL: GET http://localhost:8080/api/tasks
    @GetMapping
    public ResponseEntity<List<Task>> getMyTasks(Authentication authentication) {
        return ResponseEntity.ok(taskService.getAllTasksByUsername(authentication.getName()));
    }

    // UPDATE: PUT http://localhost:8080/api/tasks/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task taskDetails, Authentication authentication) {
        return ResponseEntity.ok(taskService.updateTask(id, taskDetails, authentication.getName()));
    }

    // DELETE: DELETE http://localhost:8080/api/tasks/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id, Authentication authentication) {
        taskService.deleteTask(id, authentication.getName());
        return ResponseEntity.ok("Task deleted successfully!");
    }

    // SEARCH: GET http://localhost:8080/api/tasks/search?status=PENDING
    @GetMapping("/search")
    public List<Task> searchMyTasks(@RequestParam(required = false) String status,
                                    @RequestParam(required = false) String title,
                                    Authentication authentication) {
        return taskService.searchTasksForUser(authentication.getName(), status, title);
    }
}