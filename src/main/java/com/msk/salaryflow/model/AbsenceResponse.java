package com.msk.salaryflow.model;

import com.msk.salaryflow.entity.AbsenceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsenceResponse {
    private UUID id;
    private String employeeFirstName;
    private String employeeLastName;
    private AbsenceType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String comment;
    private Double sickPay;
}