package com.msk.salaryflow.repository;

import com.msk.salaryflow.entity.EventLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.UUID;

public interface EventLogRepository extends MongoRepository<EventLog, UUID> {

    @Query("{ 'timestamp': { $gte: ?0, $lt: ?1 }, " +
            "  'event': { $regex: '^?2' }, " +
            "  'entityName': { $regex: '^?3' } }")
    Page<EventLog> searchLogs(Instant from, Instant to, String eventPrefix, String entityPrefix, Pageable pageable);

    @Query(value = "{ 'timestamp': { $gte: ?0, $lt: ?1 }, " +
            "  'event': { $regex: '^?2' }, " +
            "  'entityName': { $regex: '^?3' } }", delete = true)
    long deleteByFilter(Instant from, Instant to, String eventPrefix, String entityPrefix);
}