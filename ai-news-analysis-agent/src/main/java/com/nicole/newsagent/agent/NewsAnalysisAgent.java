package com.nicole.newsagent.agent;

import com.nicole.newsagent.ai.LLMClient;
import com.nicole.newsagent.ai.strategies.AnalysisStrategy;
import com.nicole.newsagent.ai.strategies.Summarization;
import com.nicole.newsagent.models.Article;
import com.nicole.newsagent.repositories.ArticleRepository;
import com.nicole.newsagent.sources.NewsSourceAdapter;
import com.nicole.newsagent.sources.NewsSourceFactory;
import java.util.List;

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
