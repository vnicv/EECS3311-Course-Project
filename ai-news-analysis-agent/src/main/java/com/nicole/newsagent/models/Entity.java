package com.nicole.newsagent.models;

public class Entity {
    private String id;
    private String name;
    private String type;

    public Entity(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String type() {
        return type;
    }
    
}
