package com.msk.salaryflow.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "department")
public class Department {
    @Id
    private UUID id;
    private String name;
    private String location;
}
