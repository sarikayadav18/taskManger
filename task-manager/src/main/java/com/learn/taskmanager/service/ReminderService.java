package com.learn.taskmanager.service;

import com.learn.taskmanager.model.Notification;
import com.learn.taskmanager.model.Priority;
import com.learn.taskmanager.model.Status;
import com.learn.taskmanager.model.Task;
import com.learn.taskmanager.repository.NotificationRepository;
import com.learn.taskmanager.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReminderService {

    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService; // Added EmailService

    // Fixed constructor (added missing comma)
    public ReminderService(TaskRepository taskRepository,
                           NotificationRepository notificationRepository,
                           EmailService emailService) {
        this.taskRepository = taskRepository;
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void checkUpcomingDeadlines() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Task> upcomingTasks = taskRepository.findByDueDateAndStatusNot(tomorrow, "COMPLETED");

        System.out.println("Scheduler found " + upcomingTasks.size() + " tasks due tomorrow.");

        for (Task task : upcomingTasks) {
            String prefix = (task.getPriority() == Priority.URGENT || task.getPriority() == Priority.HIGH)
                    ? "🚨 URGENT REMINDER: "
                    : "Reminder: ";

            String msg = prefix + "Your task '" + task.getTitle() + "' is due tomorrow!";

            boolean alreadyExists = notificationRepository.existsByMessageAndUser(msg, task.getUser());

            if (!alreadyExists) {
                // 1. Save In-App Notification
                notificationRepository.save(new Notification(msg, task.getUser()));
                System.out.println("Saved notification: " + msg);

                // 2. Send Email Notification
                if (task.getUser().getEmail() != null) {
                    emailService.sendTaskReminder(
                            task.getUser().getEmail(),
                            task.getTitle(),
                            msg + "\n\nLog in to your dashboard to complete your task."
                    );
                }
            } else {
                System.out.println("Notification already exists for: " + task.getTitle());
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * ?") // Runs at 12:00 AM
    @Transactional
    public void processOverdueTasks() {
        LocalDate today = LocalDate.now();
        int updatedCount = taskRepository.markExpiredTasksAsOverdue(today);

        if (updatedCount > 0) {
            System.out.println("Midnight Cleanup: " + updatedCount + " tasks marked as OVERDUE.");
        }

        List<Task> overdueTasks = taskRepository.findByStatus(Status.OVERDUE);

        for (Task task : overdueTasks) {
            String msg = "⚠️ ATTENTION: Your task '" + task.getTitle() + "' is now OVERDUE!";

            if (!notificationRepository.existsByMessageAndUser(msg, task.getUser())) {
                // 1. Save In-App Notification
                notificationRepository.save(new Notification(msg, task.getUser()));

                // 2. Send Overdue Email
                if (task.getUser().getEmail() != null) {
                    emailService.sendTaskReminder(
                            task.getUser().getEmail(),
                            "OVERDUE: " + task.getTitle(),
                            msg + "\n\nThis task has passed its deadline. Please update its status as soon as possible."
                    );
                }
            }
        }
    }
}