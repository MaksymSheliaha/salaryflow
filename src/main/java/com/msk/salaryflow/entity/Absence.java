package com.msk.salaryflow.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@Document(collection = "absences")
public class Absence {

    @Id
    private UUID id = UUID.randomUUID();

    @NotBlank(message = "Employee ID must not be empty")
    private String employeeId;

    @NotNull(message = "Absence type must not be null")
    private AbsenceType type;

    @NotNull(message = "Start date must not be null")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate = LocalDate.now();

    @NotNull(message = "End date must not be null")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String comment; // необов'язкове
}
