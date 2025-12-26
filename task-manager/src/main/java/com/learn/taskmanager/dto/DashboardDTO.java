package com.learn.taskmanager.dto;

import java.util.List;

public class DashboardDTO {
    private List<TaskStatsDTO> statusStats;
    private List<PriorityStatsDTO> priorityStats;
    private double completionRate;



    private List<WeeklyActivityDTO> weeklyActivity;
    public List<WeeklyActivityDTO> getWeeklyActivity() {
        return weeklyActivity;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }

    public DashboardDTO(List<PriorityStatsDTO> priorityStats, List<TaskStatsDTO> statusStats) {
        this.priorityStats = priorityStats;
        this.statusStats = statusStats;
    }

    public DashboardDTO() {

    }

    public List<PriorityStatsDTO> getPriorityStats() {
        return priorityStats;
    }

    public void setPriorityStats(List<PriorityStatsDTO> priorityStats) {
        this.priorityStats = priorityStats;
    }

    public List<TaskStatsDTO> getStatusStats() {
        return statusStats;
    }

    public void setStatusStats(List<TaskStatsDTO> statusStats) {
        this.statusStats = statusStats;
    }

    public void setWeeklyActivity(List<WeeklyActivityDTO> weeklyActivity) {
        this.weeklyActivity = weeklyActivity;

    }


    // Constructor, Getters, Setters
}
