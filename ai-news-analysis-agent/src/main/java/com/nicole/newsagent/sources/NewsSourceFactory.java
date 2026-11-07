package com.nicole.newsagent.sources;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class NewsSourceFactory {
    public static NewsSourceAdapter createAdapter(String type) {
        return createAdapter(type, "");
    }

    public static NewsSourceAdapter createAdapter(String type, String query) {
        if (type.equalsIgnoreCase("RSS")) {
            //return new RSSAdapter("https://feeds.bbci.co.uk/news/technology/rss.xml");
            return new RSSAdapter(pickFeedForQuery(query));
        } else if (type.equalsIgnoreCase("NEWS_API")) {
            return new NewsAPIAdapter(); // no argument, loads from .env
        }
        throw new IllegalArgumentException("Unknown news source type: " + type);
    }

    /*
     * Decide which RSS feed to use based on the user's query.
     *   - Known category  → dedicated BBC feed
     *   - Empty query     → BBC general news
     *   - Anything else   → Google News RSS search (still returns results!)
     */
    private static String pickFeedForQuery(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            return "https://feeds.bbci.co.uk/news/rss.xml";
        }
        if (q.contains("sport")) {    
            return "https://feeds.bbci.co.uk/sport/rss.xml";
        }
        if (q.contains("business")) {
            return "https://feeds.bbci.co.uk/news/business/rss.xml";
        }
        if (q.contains("tech")) {     
            return "https://feeds.bbci.co.uk/news/technology/rss.xml";
        }
        if (q.contains("world")) {    
            return "https://feeds.bbci.co.uk/news/world/rss.xml";
        }
        if (q.contains("science")) {
            return "https://feeds.bbci.co.uk/news/science_and_environment/rss.xml";
        }
        if (q.contains("health")) {   
            return "https://feeds.bbci.co.uk/news/health/rss.xml";
        }
        if (q.contains("politic")) {  
            return "https://feeds.bbci.co.uk/news/politics/rss.xml";
        }
        if (q.contains("entertain")) {
            return "https://feeds.bbci.co.uk/news/entertainment_and_arts/rss.xml";
        }

        // Not a known category → Google News RSS works for any query
        return "https://news.google.com/rss/search?q=" + URLEncoder.encode(query.trim(), StandardCharsets.UTF_8) + "&hl=en-US&gl=US&ceid=US:en";
    }

    // Show the list of supported BBC categories for error messages / UI hints
    public static String getCategoryHint() {
        return "Known categories: technology, business, sports, world, science, health, politics, entertainment.";
    }
}
