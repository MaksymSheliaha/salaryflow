package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.EventLog;
import com.msk.salaryflow.repository.EventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventLogService {
    private final EventLogRepository eventLogRepository;

    public Page<EventLog> getEventLogs(Pageable pageable, String from, String to, String eventType, String entity) {
        Instant fromInstant = parseFrom(from);
        Instant toInstant = parseTo(to);

        String eventPrefix = (eventType != null) ? eventType : "";
        String entityPrefix = (entity != null) ? entity : "";

        return eventLogRepository.searchLogs(fromInstant, toInstant, eventPrefix, entityPrefix, pageable);
    }

    public void deleteById(UUID id) {
        eventLogRepository.deleteById(id);
    }

    public long deleteByFilter(String from, String to, String eventType, String entity) {
        Instant fromInstant = parseFrom(from);
        Instant toInstant = parseTo(to);

        String eventPrefix = (eventType != null) ? eventType : "";
        String entityPrefix = (entity != null) ? entity : "";

        return eventLogRepository.deleteByFilter(fromInstant, toInstant, eventPrefix, entityPrefix);
    }

    public void deleteAll() {
        eventLogRepository.deleteAll();
    }

    public EventLog findById(UUID id) {
        return eventLogRepository.findById(id).orElse(null);
    }

    private Instant parseFrom(String from) {
        if (StringUtils.hasText(from)) {
            return LocalDate.parse(from).atStartOfDay(ZoneId.systemDefault()).toInstant();
        }
        return Instant.EPOCH;
    }

    private Instant parseTo(String to) {
        if (StringUtils.hasText(to)) {
            return LocalDate.parse(to).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        }
        return Instant.now();
    }
}