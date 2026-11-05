package com.nicole.newsagent.repositories;

import com.nicole.newsagent.models.Topic;
import java.util.ArrayList;
import java.util.List;

public class TopicRepository {
    private final List<Topic> topics = new ArrayList<>();

    public void save(Topic topic) {
        topics.add(topic);
    }

    public List<Topic> findAll() {
        return new ArrayList<>(topics);
    }

    public Topic findById(String id) {
        return topics.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
    }

    public Topic findByName(String name) {
        return topics.stream().filter(t -> t.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public boolean delete(String id) {
        return topics.removeIf(t -> t.getId().equals(id));
    }
}
