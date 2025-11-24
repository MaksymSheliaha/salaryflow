package com.msk.salaryflow.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "department_stats")
public class DepartmentInfo {
    @Id
    private UUID id;

    private String name;
    private String location;
    private Long employees;
    private Double salary;
    private Double age;
    private Double experience;

}
