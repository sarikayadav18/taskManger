package com.learn.taskmanager.service;

import com.learn.taskmanager.model.Task;
import com.learn.taskmanager.model.TaskFile;
import com.learn.taskmanager.repository.TaskFileRepository;
import com.learn.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class FileService {

    private final TaskFileRepository fileRepository;
    private final TaskRepository taskRepository;

    public FileService(TaskFileRepository fileRepository, TaskRepository taskRepository) {
        this.fileRepository = fileRepository;
        this.taskRepository = taskRepository;
    }

    public TaskFile storeFile(Long taskId, MultipartFile file) throws IOException {
        // 1. Validate if the Task exists
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        // 2. Extract file details
        String fileName = file.getOriginalFilename();
        String fileType = file.getContentType();
        byte[] data = file.getBytes(); // This is the @Lob data

        // 3. Create and Save the TaskFile entity
        TaskFile taskFile = new TaskFile(fileName, fileType, data, task);

        return fileRepository.save(taskFile);
    }

    public TaskFile getFile(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + fileId));
    }

    public List<TaskFile> getFilesByTask(Long taskId) {
        return fileRepository.findByTaskId(taskId);
    }
}