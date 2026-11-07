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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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

            int count = 0;
            for (SyndEntry entry : feed.getEntries()) {
                // Limit to 10 articles to avoid long fetch times
                if (count >= 10) break;

                String title = entry.getTitle() != null ? entry.getTitle() : "";
                // Only filter by query if it's NOT a known category (categories are already
                // handled by picking the dedicated BBC feed)
                if (query != null && !query.isEmpty() && !isKnownCategory(query) && !title.toLowerCase().contains(query.toLowerCase())) {
                    continue;
                }
                /* 
                if (query != null && !query.isEmpty() && !title.toLowerCase().contains(query.toLowerCase())) {
                    continue;
                }
                */

                String link = entry.getLink();
                String content = "";

                // Try to scrape the full article from the URL
                System.out.println("Scraping: " + link);
                content = scrapeArticle(link);

                // Fallback to description if scraping fails
                if (content == null || content.trim().length() < 100) {
                    content = entry.getDescription() != null ? entry.getDescription().getValue() : "";
                }

                Article article = new Article(
                        UUID.randomUUID().toString(),
                        title,
                        content,
                        link,
                        entry.getPublishedDate() != null ?
                                LocalDateTime.ofInstant(entry.getPublishedDate().toInstant(), ZoneId.systemDefault()) :
                                LocalDateTime.now()
                );
                articles.add(article);
                count++;
            }
            /* 
            for (SyndEntry entry : feed.getEntries()) {
                String title = entry.getTitle() != null ? entry.getTitle() : "";
                
                if (query != null && !query.isEmpty() && !title.toLowerCase().contains(query.toLowerCase())) {
                    continue;
                }

                // Try to get FULL content first (content:encoded), fall back to description
                String content = "";
                
                // Rome puts "content:encoded" into the "contents" foreign markup list
                if (entry.getContents() != null && !entry.getContents().isEmpty()) {
                    content = entry.getContents().get(0).getValue();
                }
                
                // If full content is empty or too short, combine with description
                if (content == null || content.trim().length() < 200) {
                    String description = entry.getDescription() != null ? entry.getDescription().getValue() : "";
                    content = (content != null ? content + "\n\n" : "") + description;
                }

                // Strip HTML tags so the LLM doesn't have to deal with markup
                content = stripHtml(content);

                Article article = new Article(
                        UUID.randomUUID().toString(),
                        title,
                        content,
                        entry.getLink(),
                        entry.getPublishedDate() != null ?
                                LocalDateTime.ofInstant(entry.getPublishedDate().toInstant(), ZoneId.systemDefault()) :
                                LocalDateTime.now()
                );
                articles.add(article);
            }
            */

            /* 
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
            */
        } catch (Exception e) {
            System.out.println("Error fetching RSS feed: " + e.getMessage());
            e.printStackTrace();
        }

        return articles;
    }

    private String scrapeArticle(String url) {
        try {
            Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (compatible; NewsAgent/1.0)").timeout(5000).get();

            // Try common article containers across news sites
            String[] selectors = {"article", "[class*=article-body]", "[class*=story-body]", "main", "[role=main]"};
            for (String selector : selectors) {
                if (!doc.select(selector).isEmpty()) {
                    String text = doc.select(selector).first().text();
                    if (text.length() > 300) {
                        return truncate(text);
                    }
                }
            }

            // Fallback: extract all paragraph tags
            return doc.select("p").text();
        } catch (Exception e) {
            System.err.println("Could not scrape " + url + ": " + e.getMessage());
            return "";
        }
    }

    // Truncate the scraped content to 3000 chars for faster LLM processing
    private String truncate(String text) {
        if (text == null) return "";
        if (text.length() > 3000) {
            return text.substring(0, 3000) + "...";
        }
        return text;
    }

    private boolean isKnownCategory(String query) {
        String q = query.toLowerCase();
        return q.contains("sport") || q.contains("business") || q.contains("tech") || q.contains("world") || q.contains("science") || q.contains("health") || q.contains("politic") || q.contains("entertain");
    }

    /* 
    // Simple HTML stripper: removes <tag> ... </tag> and replaces with text
    private String stripHtml(String html) {
        if (html == null) return "";
        // Remove tags
        String noTags = html.replaceAll("<[^>]+>", " ");
        // Collapse whitespace
        return noTags.replaceAll("\\s+", " ").trim();
    }
    */
}
