package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.Absence;
import com.msk.salaryflow.entity.AbsenceType;
import com.msk.salaryflow.entity.Employee;
import com.msk.salaryflow.model.AbsenceResponse;
import com.msk.salaryflow.repository.AbsenceRepository;
import com.msk.salaryflow.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbsenceServiceTest {

    @Mock
    private AbsenceRepository absenceRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AbsenceService absenceService;

    @Test
    void findAll_ShouldCalculateSickPay_ForLongExperience() {
        // ARRANGE
        UUID empId = UUID.randomUUID();
        Employee employee = new Employee();
        employee.setId(empId);
        employee.setFirstName("Test");
        employee.setLastName("User");
        employee.setSalary(3000.0); // 100 грн в день (3000 / 30)
        // Найнятий 10 років тому (стаж > 4 років -> 100% виплати)
        employee.setHireDate(LocalDate.now().minusYears(10).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Absence absence = new Absence();
        absence.setId(UUID.randomUUID());
        absence.setType(AbsenceType.SICK_LEAVE);
        absence.setStartDate(LocalDate.now());
        absence.setEndDate(LocalDate.now().plusDays(4)); // 5 днів (0..4)
        absence.setEmployee(employee); // Важливо встановити зв'язок

        Page<Absence> page = new PageImpl<>(List.of(absence));

        when(absenceRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // ACT
        Page<AbsenceResponse> result = absenceService.findAll(null, null, Pageable.unpaged());

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        AbsenceResponse dto = result.getContent().get(0);
        // Розрахунок: (3000 / 30) * 5 днів * 1.0 (100%) = 100 * 5 = 500.0
        assertEquals(500.0, dto.getSickPay());
    }
}