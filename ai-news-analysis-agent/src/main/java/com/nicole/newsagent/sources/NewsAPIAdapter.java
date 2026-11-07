package com.nicole.newsagent.sources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nicole.newsagent.models.Article;
import io.github.cdimascio.dotenv.Dotenv;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NewsAPIAdapter implements NewsSourceAdapter {
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public NewsAPIAdapter() {
        // Load my news api key from .env file (never commited)
        Dotenv dotenv = Dotenv.configure().directory("./ai-news-analysis-agent").ignoreIfMissing().load();
        this.apiKey = dotenv.get("NEWS_API_KEY");
    
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            System.err.println("WARNING: NEWS_API_KEY not found.");
            System.err.println("Working dir: " + System.getProperty("user.dir"));
        }

        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<Article> fetchArticles(String query) {
        List<Article> articles = new ArrayList<>();
        
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Cannot fetch from NewsAPI: Missing API Key.");
            return articles;
        } try {
            // Build the request URL safely
            String encodedQuery = URLEncoder.encode(query.isEmpty() ? "technology" : query, StandardCharsets.UTF_8);
            //String url = "https://newsapi.org/v2/everything?q=" + encodedQuery + "&language=en&sortBy=publishedAt&pageSize=10&apiKey=" + apiKey;
            String url = "https://newsapi.org/v2/everything?q=" + encodedQuery + "&language=en&sortBy=publishedAt&pageSize=10" + "&excludeDomains=wsj.com,nytimes.com,bloomberg.com,ft.com" + "&apiKey=" + apiKey;

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Accept", "application/json").GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("NewsAPI Error: HTTP " + response.statusCode() + " - " + response.body());
                return articles;
            }

            // Parse the JSON using Jackson
            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode articlesNode = rootNode.get("articles");

            if (articlesNode != null && articlesNode.isArray()) {
                for (JsonNode node : articlesNode) {
                    String title = node.path("title").asText();
                    //String description = node.path("description").asText();
                    //String content = node.path("content").asText();
                    String urlLink = node.path("url").asText();
                    String publishedAt = node.path("publishedAt").asText();

                    // Skip removed articles
                    if (title.equals("[Removed]") || urlLink.isEmpty()) continue;

                    // Try to scrape full article content from the URL
                    System.out.println("Scraping full article: " + urlLink);
                    String content = scrapeArticle(urlLink);

                    // Fallback: combine description + content if scraping fails
                    if (content == null || content.trim().length() < 200) {
                        String description = node.path("description").asText();
                        String shortContent = node.path("content").asText();
                        content = (description + "\n\n" + shortContent).trim();
                    }

                    // Combine description and content for a fuller summary input
                    //String fullText = (description != null ? description : "") + "\n" + (content != null ? content : "");

                    LocalDateTime pubDate = LocalDateTime.now();
                    if (!publishedAt.isEmpty()) {
                        try {
                            pubDate = LocalDateTime.parse(publishedAt, DateTimeFormatter.ISO_DATE_TIME);
                        } catch (Exception ignored) {}
                    }

                    Article article = new Article(UUID.randomUUID().toString(), title, content, urlLink, pubDate);
                    //Article article = new Article(UUID.randomUUID().toString(), title, fullText.trim(), urlLink, pubDate);
                    articles.add(article);
                }
            }

        } catch (Exception e) {
            System.err.println("Error fetching from NewsAPI: " + e.getMessage());
        }
        
        return articles;
    }

    private String scrapeArticle(String url) {
        try {
            Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (compatible; NewsAgent/1.0)").timeout(7000).get();

            // Try common article containers across news sites
            String[] selectors = {"article", "[class*=article-body]", "[class*=story-body]", "[class*=article-content]", "[class*=post-content]", "main", "[role=main]"};

            String extractedText = "";
            for (String selector : selectors) {
                if (!doc.select(selector).isEmpty()) {
                    String text = doc.select(selector).first().text();
                    if (text.length() > 300) {
                        //return text;
                        extractedText = text;
                        break;
                    }
                }
            }
            // Fallback: extract all paragraph tags
            if (extractedText.isEmpty()) {
                extractedText = doc.select("p").text();
            }

            // Truncate to 3000 chars for faster LLM processing
            if (extractedText.length() > 3000) {
                extractedText = extractedText.substring(0, 3000) + "...";
            }

            return extractedText;
        } catch (Exception e) {
            System.err.println("Could not scrape " + url + ": " + e.getMessage());
            return "";
        }
    }
}
