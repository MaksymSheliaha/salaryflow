package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.Department;
import com.msk.salaryflow.entity.DepartmentInfo;
import com.msk.salaryflow.model.DepartmentSearchRequest;
import com.msk.salaryflow.model.PageResponse;
import com.msk.salaryflow.repository.DepartmentInfoRepository;
import com.msk.salaryflow.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentInfoRepository departmentInfoRepository;

    @CacheEvict(value = { "department", "department_pages" }, allEntries = true)
    public Department save(Department department) {
        return departmentRepository.save(department);
    }

    public Page<Department> findAll(Pageable pageable) {
        return departmentRepository.findAll(pageable);
    }

    public Page<Department> findAll(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return departmentRepository.findAll(pageable);
        }
        return departmentRepository.searchDepartments(searchTerm.trim(), pageable);
    }

    @Cacheable(value = "departmentsSearch", key = "#request.cacheKey()")
    public PageResponse<DepartmentInfo> findAll(DepartmentSearchRequest request) {
        if (!request.isEmployeeInfo()) {
            return PageResponse.from(findAll(request.getSearchTerm(), request.getPageable()).map(this::map));
        }

        if (request.getSearchTerm() == null || request.getSearchTerm().trim().isEmpty()) {
            return PageResponse.from(departmentInfoRepository.findAll(request.getPageable()));
        }

        return PageResponse.from(departmentInfoRepository.searchDepartments(request.getSearchTerm(), request.getPageable()));
    }

    @CacheEvict(value = { "department", "department_pages" }, allEntries = true)
    public void deleteById(UUID id) {
        departmentRepository.deleteById(id);
    }

    @Cacheable(value = "department", key = "#id")
    public Department findById(UUID id) {
        return departmentRepository.findById(id).orElse(null);
    }

    // Load aggregated department info (including employee-related stats) from department_stats view
    public DepartmentInfo findInfoById(UUID id) {
        return departmentInfoRepository.findById(id).orElse(null);
    }

    private DepartmentInfo map(Department department) {
        DepartmentInfo info = new DepartmentInfo();
        info.setId(department.getId());
        info.setName(department.getName());
        info.setLocation(department.getLocation());
        return info;
    }

}
