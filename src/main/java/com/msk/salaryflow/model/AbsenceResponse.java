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
    // Коментар тут є, але в таблиці ми його показувати не будемо, тільки в View
    private String comment;
}