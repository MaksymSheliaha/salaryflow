package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.Employee;
import com.msk.salaryflow.entity.Position;
import com.msk.salaryflow.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Вмикаємо Mockito
class EmployeeServiceTest {

    @Mock // Створюємо імітацію репозиторія (не справжній)
    private EmployeeRepository employeeRepository;

    @InjectMocks // Вставляємо цей мок у справжній сервіс
    private EmployeeService employeeService;

    @Test
    void save_ShouldReturnSavedEmployee() {
        // 1. Arrange (Підготовка)
        Employee employee = new Employee();
        employee.setFirstName("John");
        employee.setEmail("test@test.com");

        // Кажемо моку: "Коли тебе попросять зберегти будь-що, поверни цей об'єкт"
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // 2. Act (Дія)
        Employee saved = employeeService.save(employee);

        // 3. Assert (Перевірка)
        assertNotNull(saved);
        assertEquals("John", saved.getFirstName());

        // Перевіряємо, що метод save у репозиторія викликався рівно 1 раз
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void findById_WhenExists_ShouldReturnEmployee() {
        // Arrange
        UUID id = UUID.randomUUID();
        Employee employee = new Employee();
        employee.setId(id);

        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));

        // Act
        Employee found = employeeService.findById(id);

        // Assert
        assertNotNull(found);
        assertEquals(id, found.getId());
    }

    @Test
    void findById_WhenNotExists_ShouldReturnNull() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Employee found = employeeService.findById(id);

        // Assert
        assertNull(found);
    }

    @Test
    void findByEmail_ShouldDelegateToRepository() {
        String email = "test@test.com";
        Employee employee = new Employee();
        employee.setEmail(email);

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));

        Optional<Employee> result = employeeService.findByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(employee, result.get());
        verify(employeeRepository).findByEmail(email);
    }

    @Test
    void findAll_WithNoFilters_ShouldReturnRestResponsePage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));
        Employee employee = new Employee();
        Page<Employee> page = new PageImpl<>(List.of(employee), pageable, 1);

        when(employeeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<Employee> result = employeeService.findAll(null, null, null, null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findAll_WithSearchTermAndFilters_ShouldBuildPredicates() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("firstName"));
        Employee employee = new Employee();
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john@test.com");
        employee.setBirthday(LocalDate.now().minusYears(65)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
        employee.setSalary(5000.0);
        employee.setPosition(Position.MANAGER);

        Page<Employee> page = new PageImpl<>(List.of(employee), pageable, 1);

        when(employeeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<Employee> result = employeeService.findAll(
                "john",
                UUID.randomUUID(),
                Position.MANAGER,
                true,
                3000.0,
                6000.0,
                pageable
        );

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void refineSorting_ShouldHandleFirstNameLastName() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("firstName")));

        Pageable refined = (Pageable) org.springframework.test.util.ReflectionTestUtils
                .invokeMethod(employeeService, "refineSorting", pageable);

        Sort sort = refined.getSort();
        assertNotNull(sort.getOrderFor("firstName"));
        assertNotNull(sort.getOrderFor("lastName"));
    }

    @Test
    void refineSorting_ShouldHandleDepartmentHireDateSalaryBirthday() {
        Pageable depSort = PageRequest.of(0, 10, Sort.by("department"));
        Pageable hireSort = PageRequest.of(0, 10, Sort.by("hireDate"));
        Pageable salarySort = PageRequest.of(0, 10, Sort.by("salary"));
        Pageable birthdaySort = PageRequest.of(0, 10, Sort.by("birthday"));

        Pageable depRefined = (Pageable) org.springframework.test.util.ReflectionTestUtils
                .invokeMethod(employeeService, "refineSorting", depSort);
        Pageable hireRefined = (Pageable) org.springframework.test.util.ReflectionTestUtils
                .invokeMethod(employeeService, "refineSorting", hireSort);
        Pageable salaryRefined = (Pageable) org.springframework.test.util.ReflectionTestUtils
                .invokeMethod(employeeService, "refineSorting", salarySort);
        Pageable birthdayRefined = (Pageable) org.springframework.test.util.ReflectionTestUtils
                .invokeMethod(employeeService, "refineSorting", birthdaySort);

        assertNotNull(depRefined.getSort().getOrderFor("department.name"));
        assertNotNull(hireRefined.getSort().getOrderFor("hireDate"));
        assertNotNull(salaryRefined.getSort().getOrderFor("salary"));
        assertNotNull(birthdayRefined.getSort().getOrderFor("birthday"));
    }

    @Test
    void update_ShouldSaveEmployee() {
        Employee employee = new Employee();
        when(employeeRepository.save(employee)).thenReturn(employee);

        Employee updated = employeeService.update(employee);

        assertEquals(employee, updated);
        verify(employeeRepository).save(employee);
    }

    @Test
    void deleteById_ShouldDeleteWhenExists() {
        UUID id = UUID.randomUUID();
        Employee employee = new Employee();
        employee.setId(id);

        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));

        Employee deleted = employeeService.deleteById(id);

        assertEquals(employee, deleted);
        verify(employeeRepository).deleteById(id);
    }

    @Test
    void deleteById_ShouldReturnNullWhenNotExists() {
        UUID id = UUID.randomUUID();
        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        Employee deleted = employeeService.deleteById(id);

        assertNull(deleted);
        verify(employeeRepository, never()).deleteById(id);
    }

    @Test
    void findAll_WithUnsortedPageable_ShouldNotChangePageableSorting() {
        Pageable unsorted = Pageable.unpaged();
        Employee employee = new Employee();
        Page<Employee> page = new PageImpl<>(List.of(employee), unsorted, 1);

        when(employeeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<Employee> result = employeeService.findAll(null, null, null, null, null, null, unsorted);

        assertEquals(1, result.getTotalElements());
        verify(employeeRepository).findAll(any(Specification.class), eq(unsorted));
    }

    @Test
    void refineSorting_ShouldKeepSortForUnknownProperty() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("unknownField"));

        Pageable refined = (Pageable) org.springframework.test.util.ReflectionTestUtils
                .invokeMethod(employeeService, "refineSorting", pageable);

        assertEquals(pageable.getSort(), refined.getSort());
    }
}