package com.learn.taskmanager.dto;

import java.time.LocalDate;

public class WeeklyActivityDTO {
    private LocalDate date;
    private Long count;

    public WeeklyActivityDTO(LocalDate date, Long count) {
        this.date = date;
        this.count = count;
    }
    // Getters

    public Long getCount() {
        return count;
    }

    public LocalDate getDate() {
        return date;
    }
}
