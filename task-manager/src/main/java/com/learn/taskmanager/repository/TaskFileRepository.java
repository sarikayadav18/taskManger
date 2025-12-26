package com.learn.taskmanager.repository;

import com.learn.taskmanager.model.TaskFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskFileRepository extends JpaRepository<TaskFile, Long> {
    // This allows us to fetch a list of all attachments for a specific task
    List<TaskFile> findByTaskId(Long taskId);
}