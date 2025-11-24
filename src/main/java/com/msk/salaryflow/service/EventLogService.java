package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.EventLog;
import com.msk.salaryflow.repository.EventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public Page<EventLog> getEventLogs(Pageable pageable, String from, String to) {
        Instant fromInstant = null;
        Instant toInstant = null;

        if (StringUtils.hasText(from)) {
            LocalDate fromDate = LocalDate.parse(from);
            fromInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        }

        if (StringUtils.hasText(to)) {
            LocalDate toDate = LocalDate.parse(to);
            toInstant = toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        }

        if (fromInstant != null && toInstant != null) {
            return eventLogRepository.findByTimestampBetween(fromInstant, toInstant, pageable);
        } else if (fromInstant != null) {
            return eventLogRepository.findByTimestampBetween(fromInstant, Instant.now(), pageable);
        } else if (toInstant != null) {
            return eventLogRepository.findByTimestampBetween(Instant.EPOCH, toInstant, pageable);
        } else {
            return eventLogRepository.findAll(pageable);
        }
    }

    public void deleteById(UUID id) {
        eventLogRepository.deleteById(id);
    }

    public long deleteByDateRange(String from, String to) {
        Instant fromInstant = null;
        Instant toInstant = null;

        if (StringUtils.hasText(from)) {
            LocalDate fromDate = LocalDate.parse(from);
            fromInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        }

        if (StringUtils.hasText(to)) {
            LocalDate toDate = LocalDate.parse(to);
            toInstant = toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        }

        if (fromInstant == null && toInstant == null) {
            long count = eventLogRepository.count();
            eventLogRepository.deleteAll();
            return count;
        }

        if (fromInstant == null) {
            fromInstant = Instant.EPOCH;
        }

        if (toInstant == null) {
            toInstant = Instant.now();
        }

        return eventLogRepository.deleteByTimestampBetween(fromInstant, toInstant);
    }

    public void deleteAll() {
        eventLogRepository.deleteAll();
    }
}
