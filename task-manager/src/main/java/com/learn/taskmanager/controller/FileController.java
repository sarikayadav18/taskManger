package com.learn.taskmanager.controller;

import com.learn.taskmanager.model.TaskFile;
import com.learn.taskmanager.service.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    // 1. Upload a file to a specific Task
    @PostMapping("/{taskId}/files")
    public ResponseEntity<String> uploadFile(@PathVariable Long taskId,
                                             @RequestParam("file") MultipartFile file) {
        try {
            fileService.storeFile(taskId, file);
            return ResponseEntity.ok("File uploaded successfully: " + file.getOriginalFilename());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Could not upload file: " + e.getMessage());
        }
    }

    // 2. List all files for a specific Task (metadata only)
    @GetMapping("/{taskId}/files")
    public ResponseEntity<List<TaskFile>> listFiles(@PathVariable Long taskId) {
        List<TaskFile> files = fileService.getFilesByTask(taskId);
        return ResponseEntity.ok(files);
    }

    // 3. Download/View a specific file
    @GetMapping("/files/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId) {
        TaskFile taskFile = fileService.getFile(fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(taskFile.getFileType()))
                // This header makes the browser download the file instead of just showing code
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + taskFile.getFileName() + "\"")
                .body(taskFile.getData());
    }
}
