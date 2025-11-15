package com.msk.salaryflow.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "department")
public class Department {
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private String location;
}
