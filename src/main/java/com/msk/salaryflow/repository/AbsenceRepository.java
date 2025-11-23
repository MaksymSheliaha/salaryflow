package com.msk.salaryflow.repository;

import com.msk.salaryflow.entity.Absence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Додаємо це
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, UUID>, JpaSpecificationExecutor<Absence> {
    // JpaSpecificationExecutor дозволяє передавати criteria (фільтри) у findAll
}