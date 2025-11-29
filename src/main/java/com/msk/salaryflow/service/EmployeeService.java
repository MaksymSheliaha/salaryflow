package com.msk.salaryflow.service;

import com.msk.salaryflow.aspect.annotation.LogEvent;
import com.msk.salaryflow.entity.Employee;
import com.msk.salaryflow.entity.Position;
import com.msk.salaryflow.model.RestResponsePage;
import com.msk.salaryflow.repository.EmployeeRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private static final int PENSION_AGE = 60;

    public Optional<Employee> findByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    @Cacheable(value = "employee_pages", key = "{#searchTerm, #departmentId, " +
            "#position, #pensioners," +
            " #minSalary, #maxSalary," +
            " #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString()}")
    public Page<Employee> findAll(String searchTerm, UUID departmentId, Position position,
                                  Boolean pensioners, Double minSalary, Double maxSalary,
                                  Pageable pageable) {

        Pageable sortedPageable = refineSorting(pageable);

        Specification<Employee> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(searchTerm)) {
                String likePattern = "%" + searchTerm.toLowerCase() + "%";
                Predicate firstName = cb.like(cb.lower(root.get("firstName")), likePattern);
                Predicate lastName = cb.like(cb.lower(root.get("lastName")), likePattern);
                Predicate email = cb.like(cb.lower(root.get("email")), likePattern);
                predicates.add(cb.or(firstName, lastName, email));
            }

            if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("id"), departmentId));
            }

            if (position != null) {
                predicates.add(cb.equal(root.get("position"), position));
            }

            if (Boolean.TRUE.equals(pensioners)) {
                LocalDate cutoffDate = LocalDate.now().minusYears(PENSION_AGE);
                predicates.add(cb.lessThanOrEqualTo(root.get("birthday"), cutoffDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }

            if (minSalary != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salary"), minSalary));
            }

            if (maxSalary != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salary"), maxSalary));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Employee> page = employeeRepository.findAll(spec, sortedPageable);
        return new RestResponsePage<>(page.getContent(), page.getPageable(), page.getTotalElements());
    }

    private Pageable refineSorting(Pageable pageable) {
        Sort sort = pageable.getSort();
        if (sort.isUnsorted()) return pageable;

        Sort.Order order = sort.stream().findFirst().orElse(null);
        if (order == null) return pageable;

        String property = order.getProperty();
        Sort.Direction direction = order.getDirection();

        Sort newSort = sort;

        if ("firstName".equals(property)) {
            newSort = Sort.by(direction, "firstName").and(Sort.by(direction, "lastName"));
        } else if ("lastName".equals(property)) {
            newSort = Sort.by(direction, "lastName").and(Sort.by(direction, "firstName"));
        } else if ("department".equals(property)) {
            newSort = Sort.by(direction, "department.name");
        } else if ("hireDate".equals(property)) {
            newSort = Sort.by(direction, "hireDate").and(Sort.by(Sort.Direction.ASC, "lastName"));
        } else if ("salary".equals(property)) {
            newSort = Sort.by(direction, "salary").and(Sort.by(Sort.Direction.ASC, "lastName"));
        } else if ("birthday".equals(property)) {
            newSort = Sort.by(direction, "birthday").and(Sort.by(Sort.Direction.ASC, "lastName"));
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), newSort);
    }

    @Caching(evict = {
            @CacheEvict(value = {
                    "department", "department_pages",
                    "departmentsSearch", "employee",
                    "employee_pages" }, allEntries = true),
            @CacheEvict(value = "employee", key = "#employee.id")
    })
    @LogEvent(action = "UPDATE_EMPLOYEE")
    public Employee update(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Caching(evict = {
            @CacheEvict(value = {
                    "department", "department_pages",
                    "departmentsSearch", "employee", "employee_pages" }, allEntries = true)
    })
    @LogEvent(action = "CREATE_EMPLOYEE")
    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Caching(evict = {
            @CacheEvict(value = { "department", "department_pages", "departmentsSearch", "employee", "employee_pages" }, allEntries = true),
            @CacheEvict(value = "employee", key = "#id")
    })
    @LogEvent(action = "DELETE_EMPLOYEE")
    public Employee deleteById(UUID id){
        Employee toDelete = employeeRepository.findById(id).orElse(null);
        if (toDelete != null) {
            employeeRepository.deleteById(id);
        }
        return toDelete;
    }

    @Cacheable(value = "employee", key = "#id")
    public Employee findById(UUID id){
        return employeeRepository.findById(id).orElse(null);
    }
}