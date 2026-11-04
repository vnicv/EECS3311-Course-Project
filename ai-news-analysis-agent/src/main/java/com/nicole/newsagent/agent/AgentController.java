package com.nicole.newsagent.agent;

import com.nicole.newsagent.ai.LLMClient;
import com.nicole.newsagent.models.Article;
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

    public String compareArticles(List<Article> articles) { 
        return agent.compare(articles); 
    }

    public List<List<Article>> clusterStories() { 
        return agent.clusterArticles(articleRepository.findAll()); 
    }

    public List<Article> getAllStoredArticles() {
        return articleRepository.findAll();
    }

    public void saveArticle(Article article) { 
        article.setSaved(true); 
    }

    public List<Article> getSavedArticles() {
        return articleRepository.findAll().stream().filter(Article::isSaved).toList();
    }
}
