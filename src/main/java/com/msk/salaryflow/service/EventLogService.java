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
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventLogService {
    private final EventLogRepository eventLogRepository;

    // Оновлена сигнатура: додали String eventType
    public Page<EventLog> getEventLogs(Pageable pageable, String from, String to, String eventType) {
        Instant fromInstant = null;
        Instant toInstant = null;

        // Логіка дат (як і була)
        if (StringUtils.hasText(from)) {
            LocalDate fromDate = LocalDate.parse(from);
            fromInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        } else {
            fromInstant = Instant.EPOCH; // Початок часів
        }

        if (StringUtils.hasText(to)) {
            LocalDate toDate = LocalDate.parse(to);
            toInstant = toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        } else {
            toInstant = Instant.now();
        }

        // Логіка вибору методу репозиторія
        if (StringUtils.hasText(eventType)) {
            // Якщо вибрано фільтр (наприклад "UPDATE"), шукаємо по датах І по типу
            return eventLogRepository.findByTimestampBetweenAndEventStartingWith(fromInstant, toInstant, eventType, pageable);
        } else {
            // Якщо фільтр пустий - просто по датах
            return eventLogRepository.findByTimestampBetween(fromInstant, toInstant, pageable);
        }
    }

    public void deleteById(UUID id) {
        eventLogRepository.deleteById(id);
    }

    // ... інші методи delete залишаються без змін ...
    public long deleteByDateRange(String from, String to) {
        // (Твій код видалення залишається тут без змін)
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