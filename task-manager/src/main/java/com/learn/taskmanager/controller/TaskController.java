package com.learn.taskmanager.controller;

import com.learn.taskmanager.dto.DashboardDTO;
import com.learn.taskmanager.dto.TaskDTO;
import com.learn.taskmanager.dto.TaskStatsDTO;
import com.learn.taskmanager.model.Task;
import com.learn.taskmanager.service.EmailService;
import com.learn.taskmanager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.List;


@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private String keyword;
    @Autowired
    private EmailService emailService;


    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // CREATE: Now accepts categoryId inside the TaskDTO
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody TaskDTO task, Authentication authentication) {
        return ResponseEntity.ok(taskService.createTask(authentication.getName(), task));
    }

    // READ ALL: Paginated & Sorted
    @GetMapping
    public ResponseEntity<Page<Task>> getMyTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            Authentication authentication) {

        return ResponseEntity.ok(taskService.getAllTasksByUsername(
                authentication.getName(), page, size, sortBy, direction));
    }

    @GetMapping("/stats")
    public ResponseEntity<List<TaskStatsDTO>> getStats(Authentication authentication) {
        return ResponseEntity.ok(taskService.getTaskStats(authentication.getName()));
    }

    // UPDATE: Now returns the updated Task object
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task taskDetails, Authentication authentication) {
        return ResponseEntity.ok(taskService.updateTask(id, taskDetails, authentication.getName()));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id, Authentication authentication) {
        taskService.deleteTask(id, authentication.getName());
        return ResponseEntity.ok("Task deleted successfully!");
    }

    // SEARCH: Added categoryId filter support
    // URL Example: http://localhost:8080/api/tasks/search?categoryId=1&status=PENDING
    @GetMapping("/search")
    public ResponseEntity<Page<Task>> searchMyTasks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            // <--- ADDED THIS
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            Authentication authentication) {
        this.keyword = keyword;

        return ResponseEntity.ok(taskService.searchTasksForUser(
                authentication.getName(), status, title, categoryId,priority,keyword, page, size, sortBy, direction));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboard(Authentication auth) {
        return ResponseEntity.ok(taskService.getDashboardData(auth.getName()));


    }

    @GetMapping("/test-email")
    public String testEmail(Principal principal) {
        // Replace with your actual email to test
        emailService.sendTaskReminder("your_real_email@example.com", "Test Task", "The email system is working!");
        return "Email sent! Check your inbox.";
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportToExcel() throws IOException {
        List<Task> tasks = taskService.getAllTasks();
        // ADD THESE LOGS:
        System.out.println("DEBUG: Export requested");
        System.out.println("DEBUG: Tasks list size is: " + tasks.size());

        if (tasks.isEmpty()) {
            System.out.println("DEBUG: No tasks found! Check your Repository query.");
        }
        // Fetch all tasks
        ByteArrayInputStream in = taskService.exportTasksToExcel(tasks);
        System.out.println("Tasks found for export: " + tasks.size());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=tasks.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}