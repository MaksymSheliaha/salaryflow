package com.msk.salaryflow.service;

import com.msk.salaryflow.aspect.annotation.LogEvent;
import com.msk.salaryflow.entity.Employee;
import com.msk.salaryflow.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

    // Переделанный метод поиска: по примеру department/absence — сначала получаем все подходящие id,
    // затем формируем страницу из подсписка id, загружаем сущности по этим id и возвращаем PageImpl,
    // сохраняя порядок согласно списку id.
    public Page<Employee> search(String term, Pageable pageable) {
        if (term == null || term.isBlank()) {
            return findAll(pageable);
        }

        List<UUID> ids = employeeRepository.findIdsBySearchTerm(term.trim());
        if (ids == null || ids.isEmpty()) {
            return Page.empty(pageable);
        }

        // paging over ids (как в Department): берем подсписок id для текущей страницы
        int total = ids.size();
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        int start = page * size;
        if (start >= total) {
            return Page.empty(pageable);
        }
        int end = Math.min(start + size, total);
        List<UUID> pageIds = ids.subList(start, end);

        // загрузить сущности по pageIds и упорядочить в соответствии с порядком в pageIds
        List<Employee> found = employeeRepository.findAllByIdIn(pageIds);
        Map<UUID, Employee> map = new HashMap<>();
        for (Employee e : found) {
            map.put(e.getId(), e);
        }
        List<Employee> ordered = new ArrayList<>();
        for (UUID id : pageIds) {
            Employee e = map.get(id);
            if (e != null) ordered.add(e);
        }

        return new PageImpl<>(ordered, pageable, total);
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