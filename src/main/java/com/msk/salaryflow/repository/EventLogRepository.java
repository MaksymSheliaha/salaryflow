package com.msk.salaryflow.repository;

import com.msk.salaryflow.entity.EventLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.UUID;

public interface EventLogRepository extends MongoRepository<EventLog, UUID> {
    Page<EventLog> findByTimestampBetween(Instant from, Instant to, Pageable pageable);
    long deleteByTimestampBetween(Instant from, Instant to);
}
