package com.msk.salaryflow.repository;

import com.msk.salaryflow.entity.Absence;
import com.msk.salaryflow.entity.AbsenceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface AbsenceRepository extends MongoRepository<Absence, UUID> {
    // Фільтр по типу
    Page<Absence> findByType(AbsenceType type, Pageable pageable);

    // Пошук по списку ID працівників (якщо шукаємо по імені)
    Page<Absence> findByEmployeeIdIn(List<String> employeeIds, Pageable pageable);

    // Пошук і по типу, і по списку ID
    Page<Absence> findByTypeAndEmployeeIdIn(AbsenceType type, List<String> employeeIds, Pageable pageable);
}

