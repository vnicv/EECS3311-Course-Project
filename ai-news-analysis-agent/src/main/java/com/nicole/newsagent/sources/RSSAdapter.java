package com.nicole.newsagent.sources;

import com.nicole.newsagent.models.Article;

import java.io.InputStream;
//import java.net.URL;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Rome -- Java library for RSS feeds
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

public class RSSAdapter implements NewsSourceAdapter {
    private final String feedUrl;

    public RSSAdapter(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    // Fetch the articles from the configured RSS feed
    @Override 
    public List<Article> fetchArticles(String query) {
        List<Article> articles = new ArrayList<>();
        try {
            // Open the feed URL and build a SyndFeed object with ROME
            /*
            URL url = new URL(feedUrl);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(url)); // constructor XmlReader(url) depracated???
            */

            URI uri = URI.create(feedUrl);
            InputStream stream = uri.toURL().openStream();
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(stream)); // use uri and InputStream instead to avoid deprecated construtor

            // Iterate through every entry in the feed and convert to Article model
            for (SyndEntry entry : feed.getEntries()) {
                // Use entry description as content. If empty string missing, fall back
                String content = entry.getDescription() != null ? entry.getDescription().getValue() : "";

                // Filter ==> if query is provided, check if title contains query
                if (query != null && !query.isEmpty() && !entry.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    continue;
                }

                // Build the article
                Article article = new Article(UUID.randomUUID().toString(), entry.getTitle(), content, entry.getLink(), entry.getPublishedDate() != null ? LocalDateTime.ofInstant(entry.getPublishedDate().toInstant(), ZoneId.systemDefault()) : LocalDateTime.now());
                articles.add(article);
            }
        } catch (Exception e) {
            System.out.println("Error fetching RSS feed: " + e.getMessage());
        }

        return articles;
    }
}
