package com.nicole.newsagent.models;

import java.util.List;
import java.util.ArrayList;

public class Topic {
    private String id;
    private String name;
    private List<String> keywords;
    private boolean tracked; // F12 -- Topic Tracking

    public Topic(String id, String name) {
        this.id = id;
        this.name = name;
        this.keywords = new ArrayList<>();
        this.tracked = false;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public boolean isTracked() {
        return tracked;
    }

    // Setters
    public void addKeyword(String keyword) {
        this.keywords.add(keyword);
    }

    public void setTracked(boolean tracked) {
        this.tracked = tracked;
    }
}
