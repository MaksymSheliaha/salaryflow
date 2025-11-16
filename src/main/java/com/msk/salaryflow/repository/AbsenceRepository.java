package com.msk.salaryflow.repository;

import com.msk.salaryflow.entity.Absence;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.UUID;

public interface AbsenceRepository extends MongoRepository<Absence, UUID> {
    // add custom queries if needed
}

