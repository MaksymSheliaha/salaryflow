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
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
}