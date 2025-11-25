package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.Department;
import com.msk.salaryflow.repository.DepartmentInfoRepository;
import com.msk.salaryflow.repository.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DepartmentInfoRepository departmentInfoRepository;

    @InjectMocks
    private DepartmentService departmentService;

    @Test
    void save_ShouldReturnDepartment() {
        Department dept = new Department();
        dept.setName("HR");

        when(departmentRepository.save(any(Department.class))).thenReturn(dept);

        Department saved = departmentService.save(dept);

        assertNotNull(saved);
        assertEquals("HR", saved.getName());
        verify(departmentRepository, times(1)).save(dept);
    }

    @Test
    void findById_ShouldReturnDepartment() {
        UUID id = UUID.randomUUID();
        Department dept = new Department();
        dept.setId(id);

        when(departmentRepository.findById(id)).thenReturn(Optional.of(dept));

        Department found = departmentService.findById(id);

        assertNotNull(found);
        assertEquals(id, found.getId());
    }
}