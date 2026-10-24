package com.nicole.newsagent.models;

public class Claim {
    private String id;
    private String text;
    private double confidence;

    public Claim(String id, String text, double confidence) {
        this.id = id;
        this.text = text;
        this.confidence = confidence;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public double getConfidence() {
        return confidence;
    }
    
}
