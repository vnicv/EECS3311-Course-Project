package com.nicole.newsagent.agent;

import com.nicole.newsagent.ai.LLMClient;
import com.nicole.newsagent.models.Article;
import com.nicole.newsagent.models.Timeline;
import com.nicole.newsagent.repositories.ArticleRepository;
import java.util.List;

public class AgentController {
    private final NewsAnalysisAgent agent;
    private final ArticleRepository articleRepository;

    public AgentController() {
        // Initialize the core system internally
        LLMClient llmClient = new LLMClient();
        this.articleRepository = new ArticleRepository();
        this.agent = new NewsAnalysisAgent(llmClient, articleRepository);
    }

    // --- Facade methods for the UI to call ---

    public List<Article> fetchNews(String sourceType, String query) {
        //System.out.println("Agent: Fetching articles from " + sourceType + " for '" + query + "'...");
        return agent.retrieveArticles(sourceType, query);
    }

    /*
    public String summarize(Article article) {
        System.out.println("Agent: Analyzing and summarizing article...");
        return agent.summarizeArticle(article);
    }
    */

    // --- Single Article Analysis
    public String summarize(Article article) { 
        return agent.summarize(article); 
    }

    public String categorize(Article article) { 
        return agent.categorize(article); 
    }

    public String extractEntities(Article article) { 
        return agent.extractEntities(article); 
    }

    public String extractClaims(Article article) { 
        return agent.extractClaims(article); 
    }

    // --- Multi-Article Analysis
    public String compareArticles(List<Article> articles) { 
        return agent.compare(articles); 
    }

    public List<List<Article>> clusterStories() { 
        return agent.clusterArticles(articleRepository.findAll()); 
    }

    // --- Timeline ---
    public Timeline generateTimeline() {
        return agent.generateTimeline(articleRepository.findAll());
    }

    // --- Article Storage ---
    public List<Article> getAllStoredArticles() {
        return articleRepository.findAll();
    }

    public void saveArticle(Article article) { 
        article.setSaved(true); 
    }

    public List<Article> getSavedArticles() {
        return articleRepository.findAll().stream().filter(Article::isSaved).toList();
    }

    // --- Search History ---
    public void recordSearch(String query) { 
        agent.recordSearch(query); 
    }

    public List<String> getSearchHistory() { 
        return agent.getSearchHistory(); 
    }

    public void clearSearchHistory() { 
        agent.clearSearchHistory(); 
    }

    // Natural Language Query
    public String answerQuery(String query) {
        return agent.answerQuery(query);
    }
}
