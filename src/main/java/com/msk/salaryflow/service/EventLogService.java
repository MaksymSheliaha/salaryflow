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

    // Оновлена сигнатура: додали String entity
    public Page<EventLog> getEventLogs(Pageable pageable, String from, String to, String eventType, String entity) {
        Instant fromInstant;
        Instant toInstant;

        if (StringUtils.hasText(from)) {
            LocalDate fromDate = LocalDate.parse(from);
            fromInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        } else {
            fromInstant = Instant.EPOCH;
        }

        if (StringUtils.hasText(to)) {
            LocalDate toDate = LocalDate.parse(to);
            toInstant = toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        } else {
            toInstant = Instant.now();
        }

        // Підготовка фільтрів для Regex
        // Якщо null або пусто -> передаємо порожній рядок (знайде все)
        String eventPrefix = (eventType != null) ? eventType : "";
        String entityPrefix = (entity != null) ? entity : "";

        return eventLogRepository.searchLogs(fromInstant, toInstant, eventPrefix, entityPrefix, pageable);
    }

    public void deleteById(UUID id) {
        eventLogRepository.deleteById(id);
    }

    public long deleteByDateRange(String from, String to) {
        Instant fromInstant = (StringUtils.hasText(from)) ? LocalDate.parse(from).atStartOfDay(ZoneId.systemDefault()).toInstant() : Instant.EPOCH;
        Instant toInstant = (StringUtils.hasText(to)) ? LocalDate.parse(to).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant() : Instant.now();
        return eventLogRepository.deleteByTimestampBetween(fromInstant, toInstant);
    }

    public void deleteAll() {
        eventLogRepository.deleteAll();
    }

    public EventLog findById(UUID id) {
        return eventLogRepository.findById(id).orElse(null);
    }
}