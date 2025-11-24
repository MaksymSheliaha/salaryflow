package com.msk.salaryflow.service;

import com.msk.salaryflow.repository.EventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventLogService {
    private final EventLogRepository eventLogRepository;
}
