package com.msk.salaryflow.repository;

import com.msk.salaryflow.entity.Department;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // Піднімає JPA і базу H2
class DepartmentRepositoryTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void searchDepartments_ShouldFindByName() {
        // Arrange (Зберігаємо дані в тестову базу)
        Department d1 = new Department();
        d1.setName("IT Department");
        d1.setLocation("Room 101");
        departmentRepository.save(d1);

        Department d2 = new Department();
        d2.setName("HR Department");
        d2.setLocation("Room 102");
        departmentRepository.save(d2);

        // Act (Виконуємо наш кастомний пошук)
        Page<Department> result = departmentRepository.searchDepartments("IT", PageRequest.of(0, 10));

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("IT Department", result.getContent().get(0).getName());
    }
}