package com.learn.taskmanager.repository;

import com.learn.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Custom query method: Find a user by their username
    Optional<User> findByUsername(String username);

    // Custom query method: Check if an email already exists
    Boolean existsByEmail(String email);
}