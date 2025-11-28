package com.msk.salaryflow.controller;

import com.msk.salaryflow.entity.Employee;
import com.msk.salaryflow.service.DepartmentService;
import com.msk.salaryflow.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// НОВИЙ ІМПОРТ ЗАМІСТЬ MockBean:
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ЗМІНЕНО: @MockBean -> @MockitoBean
    @MockitoBean
    private EmployeeService employeeService;

    // ЗМІНЕНО: @MockBean -> @MockitoBean
    // Це поле підсвічується жовтим (unused), бо ми його не викликаємо в тестах нижче,
    // АЛЕ воно обов'язкове, бо контролер не створиться без DepartmentService.
    // Тому залишаємо його як є.
    @MockitoBean
    private DepartmentService departmentService;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getEmployee_ShouldReturnInfoView() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        Employee employee = new Employee();
        employee.setId(id);
        employee.setFirstName("Alice");

        // Налаштовуємо поведінку сервісу
        when(employeeService.findById(id)).thenReturn(employee);

        // Act & Assert
        mockMvc.perform(get("/employees/{id}", id))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/employee-info"))
                .andExpect(model().attributeExists("employee"))
                .andExpect(model().attribute("employee", employee));
    }

    @Test
    void getEmployee_WhenNotFound_ShouldRedirect() throws Exception {
        UUID id = UUID.randomUUID();
        when(employeeService.findById(id)).thenReturn(null);

        mockMvc.perform(get("/employees/{id}", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/notFound"));
    }
}