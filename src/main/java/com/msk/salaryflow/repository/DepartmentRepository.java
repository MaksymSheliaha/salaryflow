package com.msk.salaryflow.repository;

import com.msk.salaryflow.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    @Query(value = "SELECT * FROM department d WHERE " +
            "(LOWER(d.name) || ' ' || LOWER(d.location)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))",
            nativeQuery = true)
    Page<Department> searchDepartments(@Param("searchTerm") String searchTerm, Pageable pageable);
}
