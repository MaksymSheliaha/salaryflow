package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.Department;
import com.msk.salaryflow.entity.DepartmentInfo;
import com.msk.salaryflow.model.DepartmentSearchRequest;
import com.msk.salaryflow.model.RestResponsePage;
import com.msk.salaryflow.repository.DepartmentInfoRepository;
import com.msk.salaryflow.repository.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
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
    void update_ShouldReturnDepartment() {
        Department dept = new Department();
        dept.setName("IT");

        when(departmentRepository.save(dept)).thenReturn(dept);

        Department updated = departmentService.update(dept);

        assertNotNull(updated);
        assertEquals("IT", updated.getName());
        verify(departmentRepository).save(dept);
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

    @Test
    void findAll_WithNullSearchTerm_ShouldDelegateToRepositoryFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Department> page = new PageImpl<>(List.of(new Department()));

        when(departmentRepository.findAll(pageable)).thenReturn(page);

        Page<Department> result = departmentService.findAll(null, pageable);

        assertEquals(1, result.getTotalElements());
        verify(departmentRepository).findAll(pageable);
        verify(departmentRepository, never()).searchDepartments(anyString(), any());
    }

    @Test
    void findAll_WithEmptySearchTerm_ShouldDelegateToRepositoryFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Department> page = new PageImpl<>(List.of(new Department()));

        when(departmentRepository.findAll(pageable)).thenReturn(page);

        Page<Department> result = departmentService.findAll("   ", pageable);

        assertEquals(1, result.getTotalElements());
        verify(departmentRepository).findAll(pageable);
        verify(departmentRepository, never()).searchDepartments(anyString(), any());
    }

    @Test
    void findAll_WithSearchTerm_ShouldUseSearchDepartments() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Department> page = new PageImpl<>(List.of(new Department()));

        when(departmentRepository.searchDepartments("HR", pageable)).thenReturn(page);

        Page<Department> result = departmentService.findAll(" HR ", pageable);

        assertEquals(1, result.getTotalElements());
        verify(departmentRepository).searchDepartments("HR", pageable);
        verify(departmentRepository, never()).findAll(pageable);
    }

    @Test
    void findAll_RequestWithoutEmployeeInfo_ShouldMapDepartmentsToInfo() {
        Department dept = new Department();
        dept.setId(UUID.randomUUID());
        dept.setName("HR");
        dept.setLocation("Kyiv");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Department> page = new PageImpl<>(List.of(dept), pageable, 1);

        when(departmentRepository.findAll(pageable)).thenReturn(page);

        DepartmentSearchRequest request = new DepartmentSearchRequest(pageable, null, false);

        RestResponsePage<DepartmentInfo> result = departmentService.findAll(request);

        assertEquals(1, result.getTotalElements());
        DepartmentInfo info = result.getContent().get(0);
        assertEquals(dept.getId(), info.getId());
        assertEquals("HR", info.getName());
        assertEquals("Kyiv", info.getLocation());
    }

    @Test
    void findAll_RequestWithEmployeeInfoAndEmptySearchTerm_ShouldUseDepartmentInfoFindAll() {
        DepartmentInfo info = new DepartmentInfo();
        info.setId(UUID.randomUUID());

        Pageable pageable = PageRequest.of(0, 10);
        Page<DepartmentInfo> page = new PageImpl<>(List.of(info), pageable, 1);

        when(departmentInfoRepository.findAll(pageable)).thenReturn(page);

        DepartmentSearchRequest request = new DepartmentSearchRequest(pageable, "   ", true);


        RestResponsePage<DepartmentInfo> result = departmentService.findAll(request);

        assertEquals(1, result.getTotalElements());
        verify(departmentInfoRepository).findAll(pageable);
    }

    @Test
    void findAll_RequestWithEmployeeInfoAndSearchTerm_ShouldUseDepartmentInfoSearch() {
        DepartmentInfo info = new DepartmentInfo();
        info.setId(UUID.randomUUID());

        Pageable pageable = PageRequest.of(0, 10);
        Page<DepartmentInfo> page = new PageImpl<>(List.of(info), pageable, 1);

        when(departmentInfoRepository.searchDepartments("HR", pageable)).thenReturn(page);

        DepartmentSearchRequest request = new DepartmentSearchRequest(pageable, "HR", true);


        RestResponsePage<DepartmentInfo> result = departmentService.findAll(request);

        assertEquals(1, result.getTotalElements());
        verify(departmentInfoRepository).searchDepartments("HR", pageable);
    }

    @Test
    void deleteById_ShouldReturnDeletedDepartment_WhenExists() {
        UUID id = UUID.randomUUID();
        Department dept = new Department();
        dept.setId(id);

        when(departmentRepository.findById(id)).thenReturn(Optional.of(dept));

        Department deleted = departmentService.deleteById(id);

        assertEquals(dept, deleted);
        verify(departmentRepository).findById(id);
        verify(departmentRepository).deleteById(id);
    }

    @Test
    void deleteById_ShouldReturnNull_WhenNotExists() {
        UUID id = UUID.randomUUID();
        when(departmentRepository.findById(id)).thenReturn(Optional.empty());

        Department deleted = departmentService.deleteById(id);

        assertNull(deleted);
        verify(departmentRepository).findById(id);
        verify(departmentRepository).deleteById(id);
    }

    @Test
    void findInfoById_ShouldReturnDepartmentInfoOrNull() {
        UUID id = UUID.randomUUID();
        DepartmentInfo info = new DepartmentInfo();
        info.setId(id);

        when(departmentInfoRepository.findById(id)).thenReturn(Optional.of(info));

        assertEquals(info, departmentService.findInfoById(id));

        when(departmentInfoRepository.findById(id)).thenReturn(Optional.empty());

        assertNull(departmentService.findInfoById(id));
    }

    @Test
    void findAll_Pageable_ShouldDelegateToRepository() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Department> page = new PageImpl<>(List.of(new Department()), pageable, 1);

        when(departmentRepository.findAll(pageable)).thenReturn(page);

        Page<Department> result = departmentService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(departmentRepository).findAll(pageable);
    }
}

