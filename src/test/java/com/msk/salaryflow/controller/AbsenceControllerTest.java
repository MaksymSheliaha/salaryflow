package com.msk.salaryflow.controller;

import com.msk.salaryflow.model.AbsenceResponse;
import com.msk.salaryflow.service.AbsenceService;
import com.msk.salaryflow.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AbsenceController.class)
@AutoConfigureMockMvc(addFilters = false)
class AbsenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AbsenceService absenceService;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    @WithMockUser
    void list_ShouldReturnListView() throws Exception {
        Page<AbsenceResponse> page = new PageImpl<>(Collections.emptyList());
        when(absenceService.findAll(any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/absences"))
                .andExpect(status().isOk())
                .andExpect(view().name("absences/absence-list"))
                .andExpect(model().attributeExists("absences"));
    }
}