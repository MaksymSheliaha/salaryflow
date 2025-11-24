package com.msk.salaryflow.service;

import com.msk.salaryflow.aspect.annotation.LogEvent;
import com.msk.salaryflow.entity.Employee;
import com.msk.salaryflow.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @CacheEvict(value = {"employee", "employee_pages"}, allEntries = true)
    @LogEvent(action = "UPDATE_EMPLOYEE")
    public Employee update(Employee employee) {
        return employeeRepository.save(employee);
    }

    @CacheEvict(value = {"employee", "employee_pages"}, allEntries = true)
    @LogEvent(action = "CREATE_EMPLOYEE")
    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    public Page<Employee> findAll(Pageable pageable){
        return employeeRepository.findAll(pageable);
    }

    @CacheEvict(value = {"employee", "employee_pages"}, allEntries = true)
    @LogEvent(action = "DELETE_EMPLOYEE")
    public Employee deleteById(UUID id){
        Employee toDelete = employeeRepository.findById(id).orElse(null);
        employeeRepository.deleteById(id);
        return toDelete;
    }

    @Cacheable(value = "employee", key = "#id")
    public Employee findById(UUID id){
        return employeeRepository.findById(id).orElse(null);
    }
}