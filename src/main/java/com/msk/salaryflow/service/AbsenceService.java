package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.Absence;
import com.msk.salaryflow.entity.AbsenceType;
import com.msk.salaryflow.entity.Employee;
import com.msk.salaryflow.model.AbsenceResponse;
import com.msk.salaryflow.repository.AbsenceRepository;
import com.msk.salaryflow.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AbsenceService {

    private final AbsenceRepository absenceRepository;
    private final EmployeeRepository employeeRepository;

    public Page<AbsenceResponse> findAll(String searchTerm, AbsenceType typeFilter, Pageable pageable) {
        // 1. Дістаємо всі записи
        List<Absence> allAbsences;
        if (typeFilter != null) {
            allAbsences = absenceRepository.findAll();
        } else {
            allAbsences = absenceRepository.findAll();
        }

        // 2. Маппінг та Фільтрація
        List<AbsenceResponse> fullList = allAbsences.stream()
                .map(this::mapToResponse) // Тут відбувається розрахунок sickPay
                .filter(dto -> {
                    if (typeFilter != null && dto.getType() != typeFilter) return false;
                    if (searchTerm != null && !searchTerm.isEmpty()) {
                        String fullName = (dto.getEmployeeFirstName() + " " + dto.getEmployeeLastName());
                        if (fullName == null) return false;
                        return fullName.toLowerCase().contains(searchTerm.toLowerCase());
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // 3. Сортування
        String sortProperty = "startDate";
        Sort.Direction sortDirection = Sort.Direction.DESC;

        if (pageable.getSort().isSorted()) {
            Sort.Order order = pageable.getSort().iterator().next();
            sortProperty = order.getProperty();
            sortDirection = order.getDirection();
        }

        Comparator<AbsenceResponse> comparator;
        switch (sortProperty) {
            case "employeeFirstName":
                comparator = Comparator.comparing(AbsenceResponse::getEmployeeFirstName, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "sickPay": // Сортування по сумі лікарняних
                comparator = Comparator.comparing(AbsenceResponse::getSickPay, Comparator.nullsLast(Double::compareTo));
                break;
            case "type":
                comparator = Comparator.comparing(AbsenceResponse::getType);
                break;
            case "endDate":
                comparator = Comparator.comparing(AbsenceResponse::getEndDate);
                break;
            case "startDate":
            default:
                comparator = Comparator.comparing(AbsenceResponse::getStartDate);
                break;
        }

        if (sortDirection == Sort.Direction.DESC) {
            comparator = comparator.reversed();
        }

        fullList.sort(comparator);

        // 4. Пагінація
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), fullList.size());

        List<AbsenceResponse> pagedList;
        if (start > fullList.size()) {
            pagedList = Collections.emptyList();
        } else {
            pagedList = fullList.subList(start, end);
        }

        return new PageImpl<>(pagedList, pageable, fullList.size());
    }

    // ... методи findById, save, delete без змін ...

    public AbsenceResponse findByIdResponse(UUID id) {
        return absenceRepository.findById(id).map(this::mapToResponse).orElse(null);
    }

    public Optional<Absence> findById(UUID id) {
        return absenceRepository.findById(id);
    }

    public Absence save(Absence absence) {
        if (absence.getId() == null) {
            absence.setId(UUID.randomUUID());
        }
        return absenceRepository.save(absence);
    }

    public void deleteById(UUID id) {
        absenceRepository.deleteById(id);
    }

    // --- Логіка розрахунку ---
    private AbsenceResponse mapToResponse(Absence absence) {
        AbsenceResponse response = new AbsenceResponse();
        response.setId(absence.getId());
        response.setType(absence.getType());
        response.setStartDate(absence.getStartDate());
        response.setEndDate(absence.getEndDate());
        response.setComment(absence.getComment());
        response.setSickPay(0.0); // За замовчуванням 0

        if (absence.getEmployeeId() != null) {
            try {
                Employee employee = employeeRepository.findById(UUID.fromString(absence.getEmployeeId())).orElse(null);

                if (employee != null) {
                    response.setEmployeeFirstName(employee.getFirstName());
                    response.setEmployeeLastName(employee.getLastName());

                    // РОЗРАХУНОК ЛІКАРНЯНИХ
                    if (absence.getType() == AbsenceType.SICK_LEAVE && employee.getHireDate() != null) {
                        // 1. Рахуємо стаж у роках
                        LocalDate hireDate = LocalDate.ofInstant(employee.getHireDate(), ZoneId.systemDefault());
                        long yearsWorked = ChronoUnit.YEARS.between(hireDate, LocalDate.now());

                        // 2. Визначаємо відсоток (50%, 80%, 100%)
                        double percentage = 0.5;
                        if (yearsWorked >= 2 && yearsWorked <= 4) {
                            percentage = 0.8;
                        } else if (yearsWorked > 4) {
                            percentage = 1.0;
                        }

                        // 3. Рахуємо тривалість відсутності (днів)
                        // +1 тому що inclusive (з 1 по 5 число = 5 днів)
                        long days = ChronoUnit.DAYS.between(absence.getStartDate(), absence.getEndDate()) + 1;
                        if (days < 0) days = 0;

                        // 4. Рахуємо суму (Зарплата / 30 днів * кількість днів * відсоток)
                        double dailySalary = employee.getSalary() / 30.0;
                        double calculatedPay = dailySalary * days * percentage;

                        // Округлення до 2 знаків
                        response.setSickPay(Math.round(calculatedPay * 100.0) / 100.0);
                    }

                } else {
                    response.setEmployeeFirstName("Unknown");
                    response.setEmployeeLastName("(ID not found)");
                }
            } catch (IllegalArgumentException e) {
                response.setEmployeeFirstName("Invalid");
                response.setEmployeeLastName("UUID");
            }
        } else {
            response.setEmployeeFirstName("-");
            response.setEmployeeLastName("-");
        }
        return response;
    }
}