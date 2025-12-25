package com.learn.taskmanager.controller;

import com.learn.taskmanager.model.Category;
import com.learn.taskmanager.repository.CategoryRepository;
import com.learn.taskmanager.repository.UserRepository;
import com.learn.taskmanager.model.User;
import com.learn.taskmanager.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryController(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    // CREATE Category: POST http://localhost:8080/api/categories
    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        category.setUser(user);
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    // GET ALL My Categories: GET http://localhost:8080/api/categories
    @GetMapping
    public ResponseEntity<List<Category>> getMyCategories(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return ResponseEntity.ok(categoryRepository.findByUser(user));
    }

    // DELETE Category: DELETE http://localhost:8080/api/categories/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id, Authentication authentication) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Security check
        if (!category.getUser().getUsername().equals(authentication.getName())) {
            throw new SecurityException("Unauthorized: You do not own this category");
        }

        categoryRepository.delete(category);
        return ResponseEntity.ok("Category deleted successfully!");
    }
}