package com.nicole.newsagent.models;

import java.time.LocalDateTime;

public class Article {
    private String id;
    private String title;
    private String content;
    private String sourceURL;
    private LocalDateTime publishedDate;
    private String category; // F03 -- Article Categorization
    private boolean saved; // F14 -- Saved Articles

    public Article(String id, String title, String content, String sourceURL, LocalDateTime publishedDate) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.sourceURL = sourceURL;
        this.publishedDate = publishedDate;
        this.saved = false;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getSourceURL() {
        return sourceURL;
    }

    public LocalDateTime getPublishedDate() {
        return publishedDate;
    }

    public String getCategory() {
        return category;
    }

    public boolean isSaved() {
        return saved;
    }

    // Setters
    public void setCategory(String category) {
        this.category = category;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

}
