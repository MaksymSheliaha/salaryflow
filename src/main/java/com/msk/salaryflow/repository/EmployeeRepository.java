package com.msk.salaryflow.repository;

import com.msk.salaryflow.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // <-- Важливо!
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {

}