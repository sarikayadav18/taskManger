package com.learn.taskmanager.model;

import com.fasterxml.jackson.annotation.JsonBackReference; // <-- ADD THIS
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

@Entity
@Table(name = "tasks")
public class Task {

    private String description;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Task title is required")
    private String title;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PENDING|IN_PROGRESS|COMPLETED", message = "Status must be PENDING, IN_PROGRESS, or COMPLETED")
    private String status;

    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference // <-- ADD THIS: Prevents the loop back to User
    private User user;

    // --- Constructors ---
    public Task(String description) {
        this.description = description;
    }

    public Task(String title, String description, String status, LocalDate dueDate, User user) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
        this.user = user;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}