package com.learn.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public class TaskDTO {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;


    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PENDING|IN_PROGRESS|COMPLETED", message = "Invalid status")
    private String status;

    // The ID of the category the user wants to link this task to
    private Long categoryId;


    private String priority;

    // --- Constructors ---
    public TaskDTO() {}

    public TaskDTO(String title, String description, String status, Long categoryId) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.categoryId = categoryId;
    }

    // --- Getters and Setters ---
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return null;
    }
}