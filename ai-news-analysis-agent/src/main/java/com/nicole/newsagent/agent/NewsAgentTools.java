package com.nicole.newsagent.agent;

import com.nicole.newsagent.models.Article;
import com.nicole.newsagent.repositories.ArticleRepository;
import com.nicole.newsagent.sources.NewsSourceAdapter;
import com.nicole.newsagent.sources.NewsSourceFactory;
import dev.langchain4j.agent.tool.Tool;

import java.util.List;
import java.util.stream.Collectors;

public class NewsAgentTools {
    private final ArticleRepository articleRepository;

    public NewsAgentTools(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Tool("Search for fresh news articles from NewsAPI about a given topic. Use this when the user asks about current events or topics not already in memory.")
    public String searchNews(String topic) {
        NewsSourceAdapter adapter = NewsSourceFactory.createAdapter("NEWS_API");
        List<Article> articles = adapter.fetchArticles(topic);
        for (Article a : articles) {
            articleRepository.save(a);
        }
        if (articles.isEmpty()) return "No articles found for: " + topic;

        return "Retrieved " + articles.size() + " articles:\n" + articles.stream().map(a -> "- " + a.getTitle() + " (" + a.getSourceUrl() + ")").collect(Collectors.joining("\n"));
    }

    @Tool("Search for technology news from BBC RSS. Use this as a free fallback if NewsAPI fails or returns nothing.")
    public String searchTechNewsRSS() {
        NewsSourceAdapter adapter = NewsSourceFactory.createAdapter("RSS");
        List<Article> articles = adapter.fetchArticles("");
        for (Article a : articles) {
            articleRepository.save(a);
        }
        if (articles.isEmpty()) return "No tech articles available.";

        return "Retrieved " + articles.size() + " tech articles:\n" +
                articles.stream()
                        .map(a -> "- " + a.getTitle())
                        .collect(Collectors.joining("\n"));
    }

    @Tool("Get a list of all articles already loaded in the system's memory, including their titles, content previews, and source URLs.")
    public String getStoredArticles() {
        List<Article> articles = articleRepository.findAll();
        if (articles.isEmpty()) return "No articles in memory. Use searchNews first.";

        return articles.stream()
                .limit(10)
                .map(a -> "Title: " + a.getTitle() + "\nURL: " + a.getSourceUrl() +
                        "\nPreview: " + a.getContent().substring(0, Math.min(300, a.getContent().length())) + "...")
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    @Tool("Count how many articles are currently stored in memory.")
    public String countStoredArticles() {
        return "There are " + articleRepository.findAll().size() + " articles in memory.";
    }
}
