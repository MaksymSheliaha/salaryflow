package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.Employee;
import com.msk.salaryflow.repository.EmployeeRepository;
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
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @CacheEvict(value = {"employee", "employee_pages"}, allEntries = true)
    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    public Page<Employee> findAll(Pageable pageable){
        return employeeRepository.findAll(pageable);
    }

    @CacheEvict(value = {"employee", "employee_pages"}, allEntries = true)
    public void deleteById(UUID id){
        employeeRepository.deleteById(id);
    }

    @Cacheable(value = "employee", key = "#id")
    public Employee findById(UUID id){
        return employeeRepository.findById(id).orElse(null);
    }
}