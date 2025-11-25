package com.msk.salaryflow.repository;

import com.msk.salaryflow.entity.EventLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataMongoTest
class EventLogRepositoryTest {

    @Autowired
    private EventLogRepository eventLogRepository;

    @Test
    void searchLogs_ShouldReturnOnlyMatchingEvents() {
        // очистити колекцію перед тестом
        eventLogRepository.deleteAll();

        Instant now = Instant.now();

        EventLog log1 = new EventLog();
        log1.setId(UUID.randomUUID());
        log1.setTimestamp(now.minusSeconds(3600));
        log1.setEvent("LOGIN_SUCCESS");
        log1.setEntityName("User");

        EventLog log2 = new EventLog();
        log2.setId(UUID.randomUUID());
        log2.setTimestamp(now.minusSeconds(1800));
        log2.setEvent("LOGIN_FAILURE");
        log2.setEntityName("UserSession");

        EventLog log3 = new EventLog();
        log3.setId(UUID.randomUUID());
        log3.setTimestamp(now.minusSeconds(900));
        log3.setEvent("OTHER_EVENT");
        log3.setEntityName("Other");

        eventLogRepository.saveAll(List.of(log1, log2, log3));

        Instant from = now.minusSeconds(4000);
        Instant to = now;
        Pageable pageable = PageRequest.of(0, 10);

        var page = eventLogRepository.searchLogs(from, to, "LOGIN", "User", pageable);

        assertEquals(2, page.getTotalElements());
        assertTrue(page.getContent().stream().allMatch(e -> e.getEvent().startsWith("LOGIN")));
        assertTrue(page.getContent().stream().allMatch(e -> e.getEntityName().startsWith("User")));
    }

    @Test
    void deleteByFilter_ShouldDeleteMatchingEventsAndReturnCount() {
        eventLogRepository.deleteAll();

        Instant now = Instant.now();

        EventLog log1 = new EventLog();
        log1.setId(UUID.randomUUID());
        log1.setTimestamp(now.minusSeconds(3600));
        log1.setEvent("AUDIT_CREATED");
        log1.setEntityName("Order");

        EventLog log2 = new EventLog();
        log2.setId(UUID.randomUUID());
        log2.setTimestamp(now.minusSeconds(1800));
        log2.setEvent("AUDIT_UPDATED");
        log2.setEntityName("OrderItem");

        EventLog log3 = new EventLog();
        log3.setId(UUID.randomUUID());
        log3.setTimestamp(now.minusSeconds(900));
        log3.setEvent("OTHER_EVENT");
        log3.setEntityName("Other");

        eventLogRepository.saveAll(List.of(log1, log2, log3));

        Instant from = now.minusSeconds(4000);
        Instant to = now;

        long deleted = eventLogRepository.deleteByFilter(from, to, "AUDIT", "Order");

        assertEquals(2L, deleted);

        var remaining = eventLogRepository.findAll();
        assertEquals(1, remaining.size());
        assertEquals("OTHER_EVENT", remaining.get(0).getEvent());
    }
}
