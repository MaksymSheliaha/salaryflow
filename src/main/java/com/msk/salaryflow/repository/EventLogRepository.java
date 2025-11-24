package com.msk.salaryflow.repository;

import com.msk.salaryflow.entity.EventLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface EventLogRepository extends MongoRepository<EventLog, UUID> {
}
