package com.msk.salaryflow.repository;

import com.msk.salaryflow.entity.Department;
import com.msk.salaryflow.entity.Employee;
import com.msk.salaryflow.entity.Gender;
import com.msk.salaryflow.entity.Position;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void findByEmail_ShouldReturnEmployee() {
        // Arrange
        Department dept = new Department();
        dept.setName("IT");
        dept.setLocation("Room 1");
        departmentRepository.save(dept);

        Employee emp = new Employee();
        emp.setFirstName("John");
        emp.setLastName("Doe");
        emp.setEmail("john@example.com");
        emp.setSalary(1000.0);
        emp.setGender(Gender.MALE);
        emp.setPosition(Position.EMPLOYEE);
        emp.setDepartment(dept);

        employeeRepository.save(emp);

        // Act
        Optional<Employee> found = employeeRepository.findByEmail("john@example.com");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
    }
}