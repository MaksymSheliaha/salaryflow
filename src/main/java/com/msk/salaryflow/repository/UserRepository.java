package com.msk.salaryflow.repository;

import com.msk.salaryflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN u.roles r WHERE " +
            "(:role IS NULL OR r.name = :role) AND " +
            "(:enabled IS NULL OR u.enabled = :enabled) AND " +
            "(:searchTerm IS NULL OR LOWER(CAST(u.username AS string)) LIKE :searchTerm)")
    Page<User> filterUsers(@Param("searchTerm") String searchTerm,
                           @Param("role") String role,
                           @Param("enabled") Boolean enabled,
                           Pageable pageable);
}