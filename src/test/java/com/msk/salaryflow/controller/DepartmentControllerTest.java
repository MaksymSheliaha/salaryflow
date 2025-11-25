package com.msk.salaryflow.controller;

import com.msk.salaryflow.entity.Department;
import com.msk.salaryflow.service.DepartmentService;
import com.msk.salaryflow.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepartmentService departmentService;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    @WithMockUser
    void getDepartment_ShouldReturnInfoPage() throws Exception {
        UUID id = UUID.randomUUID();
        Department dept = new Department();
        dept.setId(id);
        dept.setName("Sales");

        when(departmentService.findById(id)).thenReturn(dept);

        mockMvc.perform(get("/departments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/department-info"))
                .andExpect(model().attributeExists("department"));
    }

    @Test
    void showFormForAdd_ShouldReturnForm() throws Exception {
        mockMvc.perform(get("/departments/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/department-form"))
                .andExpect(model().attributeExists("department"));
    }
}