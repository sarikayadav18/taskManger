package com.learn.taskmanager.repository;

import com.learn.taskmanager.model.Category;
import com.learn.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUser(User user);
}