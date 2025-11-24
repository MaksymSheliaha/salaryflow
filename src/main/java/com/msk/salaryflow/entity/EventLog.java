package com.msk.salaryflow.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
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
    private String targetId;

    public EventLog(String event, String entityName, String author, String targetId) {
        this.id = UUID.randomUUID();
        this.event = event;
        this.entityName = entityName;
        this.author = author;
        this.targetId = targetId;
        this.timestamp = Instant.now();
    }
}
