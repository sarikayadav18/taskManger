package com.learn.taskmanager.dto;

public class TaskStatsDTO {
    private String categoryName;
    private String status;
    private Long count;

    public TaskStatsDTO(String categoryName, String status, Long count) {
        this.categoryName = categoryName;
        this.status = status;
        this.count = count;
    }

    // Getters
    public String getCategoryName() { return categoryName; }
    public String getStatus() { return status; }
    public Long getCount() { return count; }
}