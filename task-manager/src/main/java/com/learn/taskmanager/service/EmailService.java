package com.learn.taskmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendTaskReminder(String toEmail, String taskTitle, String messageContent) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Task Reminder: " + taskTitle);
        message.setText(messageContent);
        message.setFrom("no-reply@taskmanager.com");

        mailSender.send(message);
    }
}