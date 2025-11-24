package com.msk.salaryflow.repository;

import com.msk.salaryflow.entity.Employee;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    @Query("SELECT e.id FROM Employee e WHERE " +
            "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<UUID> findIdsBySearchTerm(@Param("term") String term);
}