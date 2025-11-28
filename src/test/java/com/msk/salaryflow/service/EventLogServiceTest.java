package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.EventLog;
import com.msk.salaryflow.repository.EventLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventLogServiceTest {

    @Mock
    private EventLogRepository eventLogRepository;

    @InjectMocks
    private EventLogService eventLogService;

    @Test
    void getEventLogs_ShouldReturnPage() {
        Page<EventLog> page = new PageImpl<>(Collections.emptyList());

        // Мокаємо пошук (зверни увагу на anyString(), бо ми передаємо порожні рядки при null)
        when(eventLogRepository.searchLogs(any(Instant.class), any(Instant.class), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        Page<EventLog> result = eventLogService.getEventLogs(Pageable.unpaged(), null, null, null, null);

        assertNotNull(result);
        verify(eventLogRepository).searchLogs(any(), any(), eq(""), eq(""), any());
    }

    @Test
    void getEventLogs_WithFromAndTo_ShouldParseDatesAndUseRepository() {
        Page<EventLog> page = new PageImpl<>(Collections.emptyList());
        when(eventLogRepository.searchLogs(any(Instant.class), any(Instant.class), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        Page<EventLog> result = eventLogService.getEventLogs(PageRequest.of(0, 10), "2024-01-01", "2024-01-31", "EVENT", "ENTITY");

        assertNotNull(result);
        verify(eventLogRepository).searchLogs(any(Instant.class), any(Instant.class), eq("EVENT"), eq("ENTITY"), any(Pageable.class));
    }

    @Test
    void deleteByFilter_ShouldCallRepositoryWithParsedValues() {
        when(eventLogRepository.deleteByFilter(any(Instant.class), any(Instant.class), anyString(), anyString()))
                .thenReturn(5L);

        long deleted = eventLogService.deleteByFilter("2024-01-01", "2024-01-31", "LOGIN", "USER");

        assertEquals(5L, deleted);
        verify(eventLogRepository).deleteByFilter(any(Instant.class), any(Instant.class), eq("LOGIN"), eq("USER"));
    }

    @Test
    void deleteById_ShouldDelegateToRepository() {
        UUID id = UUID.randomUUID();
        eventLogService.deleteById(id);
        verify(eventLogRepository).deleteById(id);
    }

    @Test
    void deleteAll_ShouldDelegateToRepository() {
        eventLogService.deleteAll();
        verify(eventLogRepository).deleteAll();
    }

    @Test
    void findById_ShouldReturnEntityOrNull() {
        UUID id = UUID.randomUUID();
        EventLog log = new EventLog();

        when(eventLogRepository.findById(id)).thenReturn(Optional.of(log));
        assertEquals(log, eventLogService.findById(id));

        when(eventLogRepository.findById(id)).thenReturn(Optional.empty());
        assertNull(eventLogService.findById(id));
    }
}