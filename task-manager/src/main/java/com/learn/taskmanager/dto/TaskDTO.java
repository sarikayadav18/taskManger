package com.learn.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class TaskDTO {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PENDING|IN_PROGRESS|COMPLETED", message = "Invalid status")
    private String status;

    // --- Constructors ---
    public TaskDTO() {}

    public TaskDTO(String title, String description, String status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    // --- Getters and Setters ---
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}