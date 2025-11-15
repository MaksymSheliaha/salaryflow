package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.Department;
import com.msk.salaryflow.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @CacheEvict(value = {"department", "department_pages"}, allEntries = true)
    public Department save(Department department) {
        return departmentRepository.save(department);
    }

    public Page<Department> findAll(Pageable pageable){
        return departmentRepository.findAll(pageable);
    }

    @CacheEvict(value = {"department", "department_pages"}, allEntries = true)
    public void deleteById(UUID id){
        departmentRepository.deleteById(id);
    }

    @Cacheable(value = "department", key = "#id")
    public Department findById(UUID id){
        return departmentRepository.findById(id).orElse(null);
    }

}
