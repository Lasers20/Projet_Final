package com.strms.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TaskHistoryEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String action;
    private final String performedBy;
    private final LocalDateTime timestamp;
    private final String description;

    public TaskHistoryEntry(String action, String performedBy, String description) {
        this.action = action;
        this.performedBy = performedBy;
        this.timestamp = LocalDateTime.now();
        this.description = description;
    }

    public TaskHistoryEntry(String action, String performedBy,
                            String description, LocalDateTime timestamp) {
        this.action = action;
        this.performedBy = performedBy;
        this.timestamp = timestamp;
        this.description = description;
    }

    public String getAction()       { return action; }
    public String getPerformedBy()  { return performedBy; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDescription()  { return description; }

    @Override
    public String toString() {
        return String.format("[%s] %s by %s : %s",
                timestamp.format(FORMATTER), action, performedBy, description);
    }
}
