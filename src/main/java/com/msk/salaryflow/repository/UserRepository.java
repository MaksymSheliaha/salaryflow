package com.msk.salaryflow.repository;

import com.msk.salaryflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    // Метод для пошуку користувача за логіном
    Optional<User> findByUsername(String username);
}