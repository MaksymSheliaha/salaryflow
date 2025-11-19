package com.msk.salaryflow.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

@AllArgsConstructor
@Getter
public class DepartmentSearchRequest {
    private Pageable pageable;
    private String searchTerm;
    private boolean employeeInfo;
}
