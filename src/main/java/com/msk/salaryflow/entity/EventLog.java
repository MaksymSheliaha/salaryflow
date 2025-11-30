package com.msk.salaryflow.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@Document(collection = "event_logs")
public class EventLog {
    @Id
    private UUID id;
    private String event;
    private Instant timestamp;
    private String entityName;
    private String author;
    private String targetId;

    private String targetName;
    private Map<String, String> changeDetails;

    public EventLog(String event, String entityName, String author, String targetId, String targetName) {
        this.id = UUID.randomUUID();
        this.event = event;
        this.entityName = entityName;
        this.author = author;
        this.targetId = targetId;
        this.targetName = targetName;
        this.timestamp = Instant.now();
    }

    public EventLog(String event, String entityName, String author, String targetId, String targetName, Map<String, String> changeDetails) {
        this(event, entityName, author, targetId, targetName);
        this.changeDetails = changeDetails;
    }
}