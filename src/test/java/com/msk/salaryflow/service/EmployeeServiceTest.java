package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.Employee;
import com.msk.salaryflow.entity.Position;
import com.msk.salaryflow.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}