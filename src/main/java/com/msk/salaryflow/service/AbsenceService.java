package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.Absence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface AbsenceService {
    Page<Absence> findAll(Pageable pageable);
    Optional<Absence> findById(UUID id);
    Absence save(Absence absence);
    void deleteById(UUID id);
}

