package com.msk.salaryflow.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "department")
public class Department {
    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank(message = "Department name is required")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;
}