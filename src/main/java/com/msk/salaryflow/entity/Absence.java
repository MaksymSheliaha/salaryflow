package com.msk.salaryflow.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "absence")
public class Absence {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;

    @NotNull(message = "Employee ID must not be empty")
    @Column(name = "employee_id")
    private UUID employeeId;

    @NotNull(message = "Absence type must not be null")
    @Enumerated(EnumType.STRING)
    private AbsenceType type;

    @NotNull(message = "Start date must not be null")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate = LocalDate.now();

    @NotNull(message = "End date must not be null")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String comment;

    public Absence(UUID id) {
        this.id = id;
    }
}