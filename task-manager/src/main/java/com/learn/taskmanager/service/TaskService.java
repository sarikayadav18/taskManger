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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    public Page<Task> searchTasksForUser(String username, String status, String title, Long categoryId, String priority, String keyword, int page, int size, String sortBy, String direction) {
        User user = getUserByUsername(username);
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // --- 1. KEYWORD SEARCH (Must be the very first check) ---
        // Added .trim() to handle accidental spaces in the URL
        if (keyword != null && !keyword.trim().isEmpty()) {
            System.out.println("DEBUG: Executing Keyword Search for: " + keyword); // Check console for this!
            return taskRepository.searchByKeyword(user, keyword.trim(), pageable);
        }

        // --- 2. OTHER FILTERS (Only run if keyword is null) ---
        if (priority != null && !priority.isEmpty()) {
            Priority priorityEnum = Priority.valueOf(priority.toUpperCase());
            return taskRepository.findByUserAndPriority(user, priorityEnum, pageable);
        }

        if (categoryId != null) {
            return taskRepository.findByUserAndCategoryId(user, categoryId, pageable);
        }

        if (status != null && !status.isEmpty()) {
            return taskRepository.findByUserAndStatus(user, status, pageable);
        }

        if (title != null && !title.isEmpty()) {
            return taskRepository.findByUserAndTitleContainingIgnoreCase(user, title, pageable);
        }

        // --- 3. DEFAULT (If no filters are provided) ---
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

    public ByteArrayInputStream exportTasksToExcel(List<Task> tasks) throws IOException {
        String[] columns = {"ID", "Title", "Description", "Status", "Due Date"};

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Tasks");
            // 1. Header Style (Optional but makes headers bold)
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            // 1. Create Header Row
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
            }

            // 2. Fill Data Rows
            int rowIdx = 1;
            for (Task task : tasks) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(task.getId());
                row.createCell(1).setCellValue(task.getTitle() != null ? task.getTitle() : "");
                row.createCell(2).setCellValue(task.getDescription() != null ? task.getDescription() : "");
                // Status Null Check
                String statusValue = (task.getStatus() != null) ? task.getStatus().toString() : "PENDING";
                row.createCell(3).setCellValue(statusValue);

                // DUE DATE NULL CHECK (This is what caused your 500 error)
                if (task.getDueDate() != null) {
                    row.createCell(4).setCellValue(task.getDueDate().toString());
                } else {
                    row.createCell(4).setCellValue("No Date");
                }
                System.out.println("DEBUG: Writing task ID " + task.getId() + " to Excel row " + (rowIdx - 1));
            }
            // IMPORTANT: Auto-size columns so data isn't cut off
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }


            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
}