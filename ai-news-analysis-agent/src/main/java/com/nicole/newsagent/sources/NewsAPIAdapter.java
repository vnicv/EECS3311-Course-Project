package com.nicole.newsagent.sources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nicole.newsagent.models.Article;
import io.github.cdimascio.dotenv.Dotenv;

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
            String url = "https://newsapi.org/v2/everything?q=" + encodedQuery + "&language=en&sortBy=publishedAt&pageSize=10&apiKey=" + apiKey;

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
                    String description = node.path("description").asText();
                    String content = node.path("content").asText();
                    String urlLink = node.path("url").asText();
                    String publishedAt = node.path("publishedAt").asText();

                    // Skip removed articles
                    if (title.equals("[Removed]")) continue;

                    // Combine description and content for a fuller summary input
                    String fullText = (description != null ? description : "") + "\n" + (content != null ? content : "");

                    LocalDateTime pubDate = LocalDateTime.now();
                    if (!publishedAt.isEmpty()) {
                        try {
                            pubDate = LocalDateTime.parse(publishedAt, DateTimeFormatter.ISO_DATE_TIME);
                        } catch (Exception ignored) {}
                    }

                    Article article = new Article(
                            UUID.randomUUID().toString(),
                            title,
                            fullText.trim(),
                            urlLink,
                            pubDate
                    );
                    articles.add(article);
                }
            }

        } catch (Exception e) {
            System.err.println("Error fetching from NewsAPI: " + e.getMessage());
        }
        
        return articles;
    }
}
