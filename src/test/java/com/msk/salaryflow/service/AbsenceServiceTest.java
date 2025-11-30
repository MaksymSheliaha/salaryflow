package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.Absence;
import com.msk.salaryflow.entity.AbsenceType;
import com.msk.salaryflow.entity.Employee;
import com.msk.salaryflow.model.AbsenceResponse;
import com.msk.salaryflow.repository.AbsenceRepository;
import com.msk.salaryflow.repository.EmployeeRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
        UUID empId = UUID.randomUUID();
        Employee employee = new Employee();
        employee.setId(empId);
        employee.setFirstName("Test");
        employee.setLastName("User");
        employee.setSalary(3000.0);
        employee.setHireDate(LocalDate.now().minusYears(10).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Absence absence = new Absence();
        absence.setId(UUID.randomUUID());
        absence.setType(AbsenceType.SICK_LEAVE);
        absence.setStartDate(LocalDate.now());
        absence.setEndDate(LocalDate.now().plusDays(4));
        absence.setEmployee(employee);

        Page<Absence> page = new PageImpl<>(List.of(absence));

        when(absenceRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);


        Page<AbsenceResponse> result = absenceService.findAll(null, null, Pageable.unpaged());


        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        AbsenceResponse dto = result.getContent().get(0);
        assertEquals(500.0, dto.getSickPay());
    }

    @Test
    void findAll_ShouldReturnEmptyPage_WhenRepositoryReturnsEmpty() {
        when(absenceRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<AbsenceResponse> result = absenceService.findAll(null, null, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void findAll_ShouldMapSortFields_WhenSortByEmployeeNames() {
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        Absence absence = new Absence();
        absence.setId(UUID.randomUUID());
        absence.setEmployee(employee);

        when(absenceRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(absence)));

        Pageable pageable = PageRequest.of(0, 10, Sort.by("employeeFirstName").ascending()
                .and(Sort.by("employeeLastName").descending()));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        absenceService.findAll(null, null, pageable);

        verify(absenceRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable captured = pageableCaptor.getValue();

        assertNotNull(captured.getSort().getOrderFor("employee.firstName"));
        assertNotNull(captured.getSort().getOrderFor("employee.lastName"));
    }

    @Test
    void findAll_ShouldKeepSort_WhenUnsorted() {
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        Absence absence = new Absence();
        absence.setId(UUID.randomUUID());
        absence.setEmployee(employee);

        when(absenceRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(absence)));

        Pageable unpaged = Pageable.unpaged();
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        absenceService.findAll(null, null, unpaged);

        verify(absenceRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertTrue(pageableCaptor.getValue().getSort().isUnsorted());
    }

    @Test
    void findAll_ShouldFilterByType_WhenTypeFilterProvided() {
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        Absence absence = new Absence();
        absence.setId(UUID.randomUUID());
        absence.setType(AbsenceType.VACATION);
        absence.setEmployee(employee);

        when(absenceRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(absence)));

        Page<AbsenceResponse> result = absenceService.findAll(null, AbsenceType.VACATION, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findAll_ShouldIgnoreEmptySearchTerm() {
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        Absence absence = new Absence();
        absence.setId(UUID.randomUUID());
        absence.setEmployee(employee);

        when(absenceRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(absence)));

        Page<AbsenceResponse> result = absenceService.findAll("   ", null, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findAll_ShouldFilterBySearchTermOnEmployeeNames() {
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setFirstName("Test");
        employee.setLastName("User");

        Absence absence = new Absence();
        absence.setId(UUID.randomUUID());
        absence.setEmployee(employee);

        when(absenceRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(absence)));

        Page<AbsenceResponse> result = absenceService.findAll("tesT", null, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findAll_ShouldMapToResponseWithoutSickPayForNonSickTypes() {
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setFirstName("John");
        employee.setLastName("Doe");

        Absence absence = new Absence();
        absence.setId(UUID.randomUUID());
        absence.setType(AbsenceType.VACATION);
        absence.setStartDate(LocalDate.now());
        absence.setEndDate(LocalDate.now());
        absence.setEmployee(employee);

        when(absenceRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(absence)));

        Page<AbsenceResponse> result = absenceService.findAll(null, null, Pageable.unpaged());

        AbsenceResponse dto = result.getContent().get(0);
        assertEquals(0.0, dto.getSickPay());
    }

    @Test
    void findByIdResponse_ShouldReturnMappedResponse_WhenAbsenceFoundWithEmployee() {
        UUID id = UUID.randomUUID();
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setFirstName("Jane");
        employee.setLastName("Doe");

        Absence absence = new Absence();
        absence.setId(id);
        absence.setEmployee(employee);
        absence.setType(AbsenceType.VACATION);
        absence.setStartDate(LocalDate.now());
        absence.setEndDate(LocalDate.now());
        absence.setComment("Comment");

        when(absenceRepository.findById(id)).thenReturn(Optional.of(absence));

        AbsenceResponse response = absenceService.findByIdResponse(id);

        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals("Jane", response.getEmployeeFirstName());
        assertEquals("Doe", response.getEmployeeLastName());
        assertEquals(0.0, response.getSickPay());
    }

    @Test
    void findByIdResponse_ShouldReturnNull_WhenAbsenceNotFound() {
        UUID id = UUID.randomUUID();
        when(absenceRepository.findById(id)).thenReturn(Optional.empty());

        AbsenceResponse response = absenceService.findByIdResponse(id);

        assertNull(response);
    }

    @Test
    void findByIdResponse_ShouldLoadEmployeeFromRepository_WhenEmployeeIsNullButEmployeeIdPresent() {
        UUID id = UUID.randomUUID();
        UUID empId = UUID.randomUUID();

        Absence absence = new Absence();
        absence.setId(id);
        absence.setEmployeeId(empId);

        Employee employee = new Employee();
        employee.setId(empId);
        employee.setFirstName("Loaded");
        employee.setLastName("Employee");

        when(absenceRepository.findById(id)).thenReturn(Optional.of(absence));
        when(employeeRepository.findById(empId)).thenReturn(Optional.of(employee));

        AbsenceResponse response = absenceService.findByIdResponse(id);

        assertNotNull(response);
        assertEquals("Loaded", response.getEmployeeFirstName());
        assertEquals("Employee", response.getEmployeeLastName());
        verify(employeeRepository).findById(empId);
    }

    @Test
    void findByIdResponse_ShouldSetUnknownNames_WhenEmployeeAndEmployeeIdNotFound() {
        UUID id = UUID.randomUUID();
        UUID empId = UUID.randomUUID();

        Absence absence = new Absence();
        absence.setId(id);
        absence.setEmployeeId(empId);

        when(absenceRepository.findById(id)).thenReturn(Optional.of(absence));
        when(employeeRepository.findById(empId)).thenReturn(Optional.empty());

        AbsenceResponse response = absenceService.findByIdResponse(id);

        assertNotNull(response);
        assertEquals("Unknown", response.getEmployeeFirstName());
        assertEquals("(ID not found)", response.getEmployeeLastName());
    }

    @Test
    void findByIdResponse_ShouldSetUnknownNames_WhenNoEmployeeAndNoEmployeeId() {
        UUID id = UUID.randomUUID();
        Absence absence = new Absence();
        absence.setId(id);

        when(absenceRepository.findById(id)).thenReturn(Optional.of(absence));

        AbsenceResponse response = absenceService.findByIdResponse(id);

        assertNotNull(response);
        assertEquals("Unknown", response.getEmployeeFirstName());
        assertEquals("(ID not found)", response.getEmployeeLastName());
        verify(employeeRepository, never()).findById(any());
    }

    @Test
    void findById_ShouldReturnOptionalFromRepository() {
        UUID id = UUID.randomUUID();
        Absence absence = new Absence();
        absence.setId(id);

        when(absenceRepository.findById(id)).thenReturn(Optional.of(absence));

        Optional<Absence> result = absenceService.findById(id);

        assertTrue(result.isPresent());
        assertEquals(absence, result.get());
        verify(absenceRepository).findById(id);
    }

    @Test
    void save_ShouldDelegateToRepository() {
        Absence absence = new Absence();
        absence.setId(UUID.randomUUID());

        when(absenceRepository.save(absence)).thenReturn(absence);

        Absence saved = absenceService.save(absence);

        assertEquals(absence, saved);
        verify(absenceRepository).save(absence);
    }

    @Test
    void update_ShouldDelegateToRepository() {
        Absence absence = new Absence();
        absence.setId(UUID.randomUUID());

        when(absenceRepository.save(absence)).thenReturn(absence);

        Absence updated = absenceService.update(absence);

        assertEquals(absence, updated);
        verify(absenceRepository).save(absence);
    }

    @Test
    void deleteById_ShouldReturnDeletedEntity_WhenExists() {
        UUID id = UUID.randomUUID();
        Absence absence = new Absence();
        absence.setId(id);

        when(absenceRepository.findById(id)).thenReturn(Optional.of(absence));
        doNothing().when(absenceRepository).deleteById(id);

        Absence deleted = absenceService.deleteById(id);

        assertEquals(absence, deleted);
        verify(absenceRepository).findById(id);
        verify(absenceRepository).deleteById(id);
    }

    @Test
    void deleteById_ShouldReturnNull_WhenEntityNotFound() {
        UUID id = UUID.randomUUID();

        when(absenceRepository.findById(id)).thenReturn(Optional.empty());
        doNothing().when(absenceRepository).deleteById(id);

        Absence deleted = absenceService.deleteById(id);

        assertNull(deleted);
        verify(absenceRepository).findById(id);
        verify(absenceRepository).deleteById(id);
    }

    @Test
    void findAll_ShouldCalculateSickPay_WithShortExperience_LessOrEqualOneYear() {
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setFirstName("Short");
        employee.setLastName("Experience");
        employee.setSalary(3000.0);
        employee.setHireDate(LocalDate.now().minusMonths(6).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Absence absence = new Absence();
        absence.setId(UUID.randomUUID());
        absence.setType(AbsenceType.SICK_LEAVE);
        absence.setStartDate(LocalDate.now());
        absence.setEndDate(LocalDate.now().plusDays(1)); // 2 дні
        absence.setEmployee(employee);

        when(absenceRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(absence)));

        Page<AbsenceResponse> result = absenceService.findAll(null, null, Pageable.unpaged());

        AbsenceResponse dto = result.getContent().get(0);
        assertEquals(100.0, dto.getSickPay());
    }

    @Test
    void findAll_ShouldCalculateSickPay_WithMediumExperience_BetweenTwoAndFourYears() {
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setFirstName("Medium");
        employee.setLastName("Experience");
        employee.setSalary(3000.0);
        employee.setHireDate(LocalDate.now().minusYears(3).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Absence absence = new Absence();
        absence.setId(UUID.randomUUID());
        absence.setType(AbsenceType.SICK_LEAVE);
        absence.setStartDate(LocalDate.now());
        absence.setEndDate(LocalDate.now().plusDays(2)); // 3 дні
        absence.setEmployee(employee);

        when(absenceRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(absence)));

        Page<AbsenceResponse> result = absenceService.findAll(null, null, Pageable.unpaged());

        AbsenceResponse dto = result.getContent().get(0);
        assertEquals(240.0, dto.getSickPay());
    }

    @Test
    void findAll_ShouldSetSickPayToZero_WhenHireDateInFuture() {
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setFirstName("Future");
        employee.setLastName("Hire");
        employee.setSalary(3000.0);
        employee.setHireDate(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Absence absence = new Absence();
        absence.setId(UUID.randomUUID());
        absence.setType(AbsenceType.SICK_LEAVE);
        absence.setStartDate(LocalDate.now());
        absence.setEndDate(LocalDate.now().plusDays(3));
        absence.setEmployee(employee);

        when(absenceRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(absence)));

        Page<AbsenceResponse> result = absenceService.findAll(null, null, Pageable.unpaged());

        AbsenceResponse dto = result.getContent().get(0);
        assertEquals(0.0, dto.getSickPay());
    }

    @Test
    void findAll_ShouldTreatNegativeOrZeroDaysAsOneDay() {
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setFirstName("Edge");
        employee.setLastName("Days");
        employee.setSalary(3000.0);
        employee.setHireDate(LocalDate.now().minusYears(10).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Absence absence = new Absence();
        absence.setId(UUID.randomUUID());
        absence.setType(AbsenceType.SICK_LEAVE);
        absence.setStartDate(LocalDate.now());
        absence.setEndDate(LocalDate.now().minusDays(1)); // негативна кількість днів
        absence.setEmployee(employee);

        when(absenceRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(absence)));

        Page<AbsenceResponse> result = absenceService.findAll(null, null, Pageable.unpaged());

        AbsenceResponse dto = result.getContent().get(0);
        assertEquals(100.0, dto.getSickPay());
    }

    @Test
    void createSpecification_ShouldAddTypeFilterPredicate_WhenTypeFilterProvided() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<Absence> query = mock(CriteriaQuery.class);
        Root<Absence> root = mock(Root.class);
        Predicate predicate = mock(Predicate.class);

        when(query.getResultType()).thenReturn(Absence.class);
        when(cb.and(any())).thenReturn(predicate);

        @SuppressWarnings("unchecked")
        Specification<Absence> spec = ReflectionTestUtils.invokeMethod(
                absenceService,
                "createSpecification",
                null,
                AbsenceType.VACATION
        );

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(root).get("type");
    }

}

