package com.nicole.newsagent.models;

import java.time.LocalDateTime;

public class TimelineEvent {
    private String id, description;
    private LocalDateTime date;

    public TimelineEvent(String id, String description, LocalDateTime date) {
        this.id = id; 
        this.description = description; 
        this.date = date;
    }

    public String getDescription() { 
        return description; 
    }

    public LocalDateTime getDate() { 
        return date; 
    }
}
