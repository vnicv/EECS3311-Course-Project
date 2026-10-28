package com.nicole.newsagent.sources;

public class NewsSourceFactory {
    public static NewsSourceAdapter createAdapter(String type) {
        if (type.equalsIgnoreCase("RSS")) {
            return new RSSAdapter("https://feeds.bbci.co.uk/news/technology/rss.xml");
        } else if (type.equalsIgnoreCase("NEWS_API")) {
            return new NewsAPIAdapter(); // no argument, loads from .env
        }
        throw new IllegalArgumentException("Unknown news source type: " + type);
    }
}
