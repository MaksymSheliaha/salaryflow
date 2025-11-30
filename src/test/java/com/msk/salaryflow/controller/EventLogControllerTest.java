package com.msk.salaryflow.controller;

import com.msk.salaryflow.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(EventLogController.class)
@AutoConfigureMockMvc(addFilters = false)
class EventLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private EventLogService eventLogService;

    @MockitoBean private EmployeeService employeeService;
    @MockitoBean private DepartmentService departmentService;
    @MockitoBean private AbsenceService absenceService;
    @MockitoBean private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void listEvents_ShouldReturnPage() throws Exception {
        when(eventLogService.getEventLogs(any(), any(), any(), any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/event-log-list"));
    }
}