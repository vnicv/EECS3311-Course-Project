package com.nicole.newsagent.models;

import java.util.*;

public class Timeline {
    private List<TimelineEvent> events = new ArrayList<>();
    
    public void addEvent(TimelineEvent e) { 
        events.add(e); 
    }

    public List<TimelineEvent> getEvents() {
        events.sort(Comparator.comparing(TimelineEvent::getDate));
        return events;
    }
}
