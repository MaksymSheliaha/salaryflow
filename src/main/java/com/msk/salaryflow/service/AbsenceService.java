package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.Absence;
import com.msk.salaryflow.entity.AbsenceType;
import com.msk.salaryflow.entity.Employee;
import com.msk.salaryflow.model.AbsenceResponse;
import com.msk.salaryflow.model.RestResponsePage; // Імпортуємо наш новий клас
import com.msk.salaryflow.repository.AbsenceRepository;
import com.msk.salaryflow.repository.EmployeeRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AbsenceService {

    private final AbsenceRepository absenceRepository;
    private final EmployeeRepository employeeRepository;

    // --- КЕШУВАННЯ СТОРІНОК ---
    @Cacheable(value = "absences_page", key = "{#searchTerm, #typeFilter, #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString()}")
    @Transactional(readOnly = true)
    public Page<AbsenceResponse> findAll(String searchTerm, AbsenceType typeFilter, Pageable pageable) {

        // 1. Виправляємо імена полів для сортування (DTO -> Entity)
        Pageable fixedPageable = mapSortFields(pageable);

        // 2. Будуємо SQL-фільтр
        Specification<Absence> spec = createSpecification(searchTerm, typeFilter);

        // 3. Виконуємо запит у БД
        Page<Absence> pageResult = absenceRepository.findAll(spec, fixedPageable);

        // 4. Перетворюємо Entity на DTO
        List<AbsenceResponse> responseList = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        // 5. Повертаємо RestResponsePage, щоб Redis міг це зберегти без помилок
        return new RestResponsePage<>(responseList, fixedPageable, pageResult.getTotalElements());
    }

    // --- КЕШУВАННЯ ОДНОГО ЗАПИСУ ---
    @Cacheable(value = "absence_response", key = "#id")
    @Transactional(readOnly = true)
    public AbsenceResponse findByIdResponse(UUID id) {
        return absenceRepository.findById(id)
                .map(this::mapToResponse)
                .orElse(null);
    }

    public Optional<Absence> findById(UUID id) {
        return absenceRepository.findById(id);
    }

    // --- ЗБЕРЕЖЕННЯ І ОЧИЩЕННЯ КЕШУ ---
    @Caching(evict = {
            @CacheEvict(value = "absence_response", key = "#absence.id"),
            @CacheEvict(value = "absences_page", allEntries = true)
    })
    @Transactional
    public Absence save(Absence absence) {
        return absenceRepository.save(absence);
    }

    @Caching(evict = {
            @CacheEvict(value = "absence_response", key = "#id"),
            @CacheEvict(value = "absences_page", allEntries = true)
    })
    @Transactional
    public void deleteById(UUID id) {
        absenceRepository.deleteById(id);
    }

    // --- ДОПОМІЖНІ МЕТОДИ ---

    /**
     * Конвертує поля сортування з DTO (які приходять з фронта) в поля Entity
     */
    private Pageable mapSortFields(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return pageable;
        }

        Sort newSort = Sort.unsorted();
        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();
            Sort.Direction direction = order.getDirection();

            if ("employeeFirstName".equals(property)) {
                property = "employee.firstName";
            } else if ("employeeLastName".equals(property)) {
                property = "employee.lastName";
            }

            newSort = newSort.and(Sort.by(direction, property));
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), newSort);
    }

    private Specification<Absence> createSpecification(String searchTerm, AbsenceType typeFilter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Оптимізація: fetch join, щоб завантажити Employee одним запитом (уникаємо N+1)
            // Робимо це тільки для основного запиту, а не для запиту count (який повертає Long)
            if (Long.class != query.getResultType()) {
                root.fetch("employee", JoinType.LEFT);
            }

            if (typeFilter != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), typeFilter));
            }

            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String likePattern = "%" + searchTerm.toLowerCase() + "%";
                Join<Absence, Employee> employeeJoin = root.join("employee", JoinType.LEFT);

                Predicate firstNameMatch = criteriaBuilder.like(criteriaBuilder.lower(employeeJoin.get("firstName")), likePattern);
                Predicate lastNameMatch = criteriaBuilder.like(criteriaBuilder.lower(employeeJoin.get("lastName")), likePattern);

                predicates.add(criteriaBuilder.or(firstNameMatch, lastNameMatch));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private AbsenceResponse mapToResponse(Absence absence) {
        AbsenceResponse response = new AbsenceResponse();
        response.setId(absence.getId());
        response.setType(absence.getType());
        response.setStartDate(absence.getStartDate());
        response.setEndDate(absence.getEndDate());
        response.setComment(absence.getComment());
        response.setSickPay(0.0);

        Employee employee = absence.getEmployee();

        // Fallback, якщо раптом employee не підтягнувся (хоча fetch має це виправити)
        if (employee == null && absence.getEmployeeId() != null) {
            employee = employeeRepository.findById(absence.getEmployeeId()).orElse(null);
        }

        if (employee != null) {
            response.setEmployeeFirstName(employee.getFirstName());
            response.setEmployeeLastName(employee.getLastName());

            if (absence.getType() == AbsenceType.SICK_LEAVE && employee.getHireDate() != null) {
                calculateSickPay(response, absence, employee);
            }
        } else {
            response.setEmployeeFirstName("Unknown");
            response.setEmployeeLastName("(ID not found)");
        }
        return response;
    }

    private void calculateSickPay(AbsenceResponse response, Absence absence, Employee employee) {
        LocalDate hireDate = LocalDate.ofInstant(employee.getHireDate(), ZoneId.systemDefault());

        if (hireDate.isAfter(LocalDate.now())) {
            response.setSickPay(0.0);
            return;
        }

        long yearsWorked = ChronoUnit.YEARS.between(hireDate, LocalDate.now());
        double percentage = 0.5;
        if (yearsWorked >= 2 && yearsWorked <= 4) {
            percentage = 0.8;
        } else if (yearsWorked > 4) {
            percentage = 1.0;
        }

        long days = ChronoUnit.DAYS.between(absence.getStartDate(), absence.getEndDate()) + 1;
        if (days <= 0) days = 1;

        double dailySalary = employee.getSalary() / 30.0;
        double calculatedPay = dailySalary * days * percentage;

        response.setSickPay(Math.round(calculatedPay * 100.0) / 100.0);
    }
}