package com.nicole.newsagent.agent;

import com.nicole.newsagent.ai.LLMClient;
//import com.nicole.newsagent.ai.strategies.AnalysisStrategy;
//import com.nicole.newsagent.ai.strategies.Summarization;
import com.nicole.newsagent.ai.strategies.*;
import com.nicole.newsagent.models.Article;
import com.nicole.newsagent.repositories.ArticleRepository;
import com.nicole.newsagent.sources.NewsSourceAdapter;
import com.nicole.newsagent.sources.NewsSourceFactory;
import java.util.List;

// Construct the agent, and wire each strategy to the shared LLM client
public class NewsAnalysisAgent {
    private final LLMClient llmClient;
    private final ArticleRepository articleRepository;
    
    private final Summarization summarization;
    private final Categorization categorization;
    private final EntityExtraction entityExtraction;
    private final ClaimExtraction claimExtraction;
    private final Comparison comparison;
    private final Clustering clustering;

    public NewsAnalysisAgent(LLMClient llmClient, ArticleRepository articleRepository) {
        this.llmClient = llmClient;
        this.articleRepository = articleRepository;
        this.summarization = new Summarization(llmClient);
        this.categorization = new Categorization(llmClient);
        this.entityExtraction = new EntityExtraction(llmClient);
        this.claimExtraction = new ClaimExtraction(llmClient);
        this.comparison = new Comparison(llmClient);
        this.clustering = new Clustering();
    }

    public List<Article> retrieveArticles(String sourceType, String query) {
        // Give the correct adapter for the given source type
        NewsSourceAdapter adapter = NewsSourceFactory.createAdapter(sourceType);
        List<Article> articles = adapter.fetchArticles(query);
        // Save article to repository
        for (Article article : articles) {
            articleRepository.save(article);
        }
        return articles;
    }

    // --- News Analysis Strategies ---

    public String summarize(Article article) { 
        return summarization.analyze(article.getContent()); 
    }

    public String categorize(Article article) { 
        return categorization.analyze(article.getContent()); 
    }

    public String extractEntities(Article article) { 
        return entityExtraction.analyze(article.getContent()); 
    }

    public String extractClaims(Article article) { 
        return claimExtraction.analyze(article.getContent()); 
    }

    // Compare a set of articles by concatenating each URL and content into one prompt
    // Then ask LLM to analyze them together
    public String compare(List<Article> articles) {
        // Build a single combined prompt with each article's source and content
        StringBuilder sb = new StringBuilder();
        for (Article a : articles) {
            sb.append("Source: ").append(a.getSourceUrl()).append("\n").append(a.getContent()).append("\n\n");
        }
        return comparison.analyze(sb.toString());
    }

    // Group related articles into clusters based on content similarity
    public List<List<Article>> clusterArticles(List<Article> articles) { 
        return clustering.analyze(articles); 
    }
}

/*
public class NewsAnalysisAgent {
    private final LLMClient llmClient;
    private final ArticleRepository articleRepository;
    private final AnalysisStrategy<String, String> summarizationStrategy;

    public NewsAnalysisAgent(LLMClient llmClient, ArticleRepository articleRepository) {
        this.llmClient = llmClient;
        this.articleRepository = articleRepository;
        this.summarizationStrategy = new Summarization(llmClient);
    }

    public List<Article> retrieveArticles(String sourceType, String query) {
        NewsSourceAdapter adapter = NewsSourceFactory.createAdapter(sourceType);
        List<Article> articles = adapter.fetchArticles(query);
        
        // Save to repository
        for (Article article : articles) {
            articleRepository.save(article);
        }
        return articles;
    }

    public String summarizeArticle(Article article) {
        return summarizationStrategy.analyze(article.getContent());
    }
}
*/
