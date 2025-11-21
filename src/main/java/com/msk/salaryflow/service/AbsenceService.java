package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.Absence;
import com.msk.salaryflow.entity.AbsenceType;
import com.msk.salaryflow.model.AbsenceResponse;
import com.msk.salaryflow.repository.AbsenceRepository;
import com.msk.salaryflow.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Collections; // Додано імпорт
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

    // Основний метод для таблиці з In-Memory сортуванням
    public Page<AbsenceResponse> findAll(String searchTerm, AbsenceType typeFilter, Pageable pageable) {
        // 1. Дістаємо ВСІ записи з MongoDB
        List<Absence> allAbsences;

        if (typeFilter != null) {
            // Тут можна було б використати метод репозиторію, але для простоти беремо все
            allAbsences = absenceRepository.findAll();
        } else {
            allAbsences = absenceRepository.findAll();
        }

        // 2. Конвертуємо ВСЕ в DTO, підтягуємо імена і фільтруємо
        List<AbsenceResponse> fullList = allAbsences.stream()
                .map(this::mapToResponse)
                .filter(dto -> {
                    // Фільтрація по Типу
                    if (typeFilter != null && dto.getType() != typeFilter) return false;

                    // Фільтрація по Пошуку (Ім'я/Прізвище)
                    if (searchTerm != null && !searchTerm.isEmpty()) {
                        String fullName = (dto.getEmployeeFirstName() + " " + dto.getEmployeeLastName());
                        if (fullName == null) return false;
                        return fullName.toLowerCase().contains(searchTerm.toLowerCase());
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // 3. Сортування в Java (ВРУЧНУ)
        String sortProperty = "startDate"; // за замовчуванням
        Sort.Direction sortDirection = Sort.Direction.DESC;

        if (pageable.getSort().isSorted()) {
            Sort.Order order = pageable.getSort().iterator().next();
            sortProperty = order.getProperty();
            sortDirection = order.getDirection();
        }

        // Компаратор для сортування
        Comparator<AbsenceResponse> comparator;

        switch (sortProperty) {
            case "employeeFirstName":
                comparator = Comparator.comparing(AbsenceResponse::getEmployeeFirstName, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "employeeLastName":
                comparator = Comparator.comparing(AbsenceResponse::getEmployeeLastName, Comparator.nullsLast(String::compareToIgnoreCase));
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

        // Розвертаємо, якщо DESC
        if (sortDirection == Sort.Direction.DESC) {
            comparator = comparator.reversed();
        }

        fullList.sort(comparator);

        // 4. Пагінація (ріжемо список)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), fullList.size());

        List<AbsenceResponse> pagedList;
        if (start > fullList.size()) {
            pagedList = Collections.emptyList();
        } else {
            pagedList = fullList.subList(start, end);
        }

        // Повертаємо об'єкт Page
        return new PageImpl<>(pagedList, pageable, fullList.size());
    }

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

    // Приватний метод для перетворення
    private AbsenceResponse mapToResponse(Absence absence) {
        AbsenceResponse response = new AbsenceResponse();
        response.setId(absence.getId());
        response.setType(absence.getType());
        response.setStartDate(absence.getStartDate());
        response.setEndDate(absence.getEndDate());
        response.setComment(absence.getComment());

        if (absence.getEmployeeId() != null) {
            try {
                employeeRepository.findById(UUID.fromString(absence.getEmployeeId()))
                        .ifPresentOrElse(employee -> {
                            response.setEmployeeFirstName(employee.getFirstName());
                            response.setEmployeeLastName(employee.getLastName());
                        }, () -> {
                            response.setEmployeeFirstName("Unknown");
                            response.setEmployeeLastName("(ID not found)");
                        });
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