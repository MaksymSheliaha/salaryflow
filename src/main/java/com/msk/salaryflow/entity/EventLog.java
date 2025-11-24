package com.msk.salaryflow.entity;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Data
@Document(collection = "event_logs")
public class EventLog {
    @Id
    private UUID id;
    private String event;
    private Instant timestamp;
    private String entityName;
    private String author;
    private Object details;

    public EventLog(String event, String entityName, String username, Object details) {
        this.event = event;
        this.entityName = entityName;
        this.author = username;
        this.details = details;
        this.timestamp = Instant.now();
    }
}
