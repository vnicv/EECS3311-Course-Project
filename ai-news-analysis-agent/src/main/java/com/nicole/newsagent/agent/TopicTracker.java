package com.nicole.newsagent.agent;

import java.util.ArrayList;
import java.util.List;

public class TopicTracker {
    public interface Observer {
        void onNewArticles(String topic, int count);
    }

    private final List<String> trackedTopics = new ArrayList<>();
    private final List<Observer> observers = new ArrayList<>();

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void trackTopic(String topic) {
        if (!trackedTopics.contains(topic)) {
            trackedTopics.add(topic);
        }
    }

    public void untrackTopic(String topic) {
        trackedTopics.remove(topic);
    }

    public List<String> getTrackedTopics() {
        return new ArrayList<>(trackedTopics);
    }

    public void notifyObservers(String topic, int count) {
        for (Observer observer : observers) {
            observer.onNewArticles(topic, count);
        }
    }
}
