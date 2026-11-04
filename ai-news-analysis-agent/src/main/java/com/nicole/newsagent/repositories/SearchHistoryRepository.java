package com.nicole.newsagent.repositories;

import java.util.ArrayList;
import java.util.List;

public class SearchHistoryRepository {
    private final List<String> history = new ArrayList<>();

    public void save(String query) {
        if (query != null && !query.trim().isEmpty()) {
            // Avoid duplicates of the most recent query
            if (history.isEmpty() || !history.get(history.size() - 1).equals(query)) {
                history.add(query);
            }
        }
    }
    
    public List<String> findAll() {
        return new ArrayList<>(history);
    }

    public void clear() {
        history.clear();
    }
}
