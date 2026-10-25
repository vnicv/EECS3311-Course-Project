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
}
