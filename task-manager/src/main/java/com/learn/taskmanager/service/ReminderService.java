package com.learn.taskmanager.service;

import com.learn.taskmanager.model.Notification;
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

    public ReminderService(TaskRepository taskRepository, NotificationRepository notificationRepository) {
        this.taskRepository = taskRepository;
        this.notificationRepository = notificationRepository;
    }

    // Runs every day at 8:00 AM
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void checkUpcomingDeadlines() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        System.out.println("tomorrow: "+ tomorrow );
        List<Task> upcomingTasks = taskRepository.findByDueDateAndStatusNot(tomorrow, "COMPLETED");

        // DEBUG: This will print in your IntelliJ/Eclipse console
        System.out.println("Scheduler found " + upcomingTasks.size() + " tasks due tomorrow.");

        for (Task task : upcomingTasks) {
            String msg = "Reminder: Your task '" + task.getTitle() + "' is due tomorrow!";

            // Check if we already sent this specific notification today
            boolean alreadyExists = notificationRepository.existsByMessageAndUser(msg, task.getUser());

            if (!alreadyExists) {
                Notification note = new Notification(msg, task.getUser());
                notificationRepository.save(note);
                System.out.println("New notification saved.");
            } else {
                System.out.println("Notification already exists, skipping...");
            }
        }
    }
}