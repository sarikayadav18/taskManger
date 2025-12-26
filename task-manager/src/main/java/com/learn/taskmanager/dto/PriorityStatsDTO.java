package com.learn.taskmanager.dto;

import com.learn.taskmanager.model.Priority;

public class PriorityStatsDTO {
    private Priority priority;
    private Long count;

    public PriorityStatsDTO(Priority priority, Long count) {
        this.priority = priority;
        this.count = count;
    }

    // Getters
    public Priority getPriority() { return priority; }
    public Long getCount() { return count; }
}