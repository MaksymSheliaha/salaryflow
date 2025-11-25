package com.msk.salaryflow.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;      // Додали
import jakarta.validation.constraints.NotBlank; // Додали
import jakarta.validation.constraints.NotNull;  // Додали
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Entity
@Table(name = "employee")
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Gender is required")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "Salary is required")
    @Min(value = 0, message = "Salary must be positive")
    private Double salary;

    private Instant hireDate;
    private Instant birthday;

    @NotNull(message = "Position is required")
    @Enumerated(EnumType.STRING)
    private Position position;

    @NotNull(message = "Department is required") // Обов'язково вибрати департамент
    @ManyToOne
    @JoinColumn(name="department_id")
    private Department department;
}