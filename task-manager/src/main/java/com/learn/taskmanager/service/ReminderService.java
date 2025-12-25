package com.learn.taskmanager.service;

import com.learn.taskmanager.model.Notification;
import com.learn.taskmanager.model.Priority; // Make sure to import this
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

    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void checkUpcomingDeadlines() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // Note: If you changed Status to an Enum, ensure the second parameter matches your Enum type
        List<Task> upcomingTasks = taskRepository.findByDueDateAndStatusNot(tomorrow, "COMPLETED");

        System.out.println("Scheduler found " + upcomingTasks.size() + " tasks due tomorrow.");

        for (Task task : upcomingTasks) {
            // Determine prefix based on Priority Enum
            String prefix = (task.getPriority() == Priority.URGENT || task.getPriority() == Priority.HIGH)
                    ? "🚨 URGENT REMINDER: "
                    : "Reminder: ";

            String msg = prefix + "Your task '" + task.getTitle() + "' is due tomorrow!";

            // Prevents creating duplicate notifications for the same message/user
            boolean alreadyExists = notificationRepository.existsByMessageAndUser(msg, task.getUser());

            if (!alreadyExists) {
                Notification note = new Notification(msg, task.getUser());
                notificationRepository.save(note);
                System.out.println("Saved: " + msg);
            } else {
                System.out.println("Notification already exists for: " + task.getTitle());
            }
        }
    }
}